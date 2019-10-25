# KotlinUi

Declarative UI building for Android in Kotlin

### Quickstart

Install the lib.

Add this class to your app:

```kotlin
class BasicKotlinUi(context: Context) : KViewBox(context) {

    var input: String by state("Change me!")
    var buttonVisible: Boolean by state(true)
    var evenOnly: Boolean by state(false)

    override val root = rootColumn {
        textField(::input).margins(0, 0, 0, 10)
        checkBox("Button visible", ::buttonVisible)
        checkBox("Even only", ::evenOnly)
            .textSize(14F)
        button(::input) {
           Toast.makeText(context, "Tapped!", Toast.LENGTH_SHORT).show()
        }.bindTo(::buttonVisible updates KButton::visible)
        list(listOf(1, 2, 3, 4)) {
            if (evenOnly && it % 2 == 1) {
                emptyView()
            } else {
                text("$it")
            }
        }.bindTo(::evenOnly)
    }.padding(10)
}
```

Then, in your Activity, add this line:

```kotlin
setContentView(BasicKotlinUi(this))
```

Now run the app! You'll see a UI consisting of an EditText, two CheckBoxes, a Button and a ListView. Changing the text in EditText will also update the Button title, and changing the checked state of CheckBoxes will toggle UI-components as described in the class.

This simple example brings home basic KotlinUi principles:

1. Custom, stateful views embedded in [KViewBox](#kview-and-kviewbox).
2. Holding the view [state](#state) in properties.
3. Specifying the [root view](#krootview).
4. Declarative UI building by calling simple [widget functions](#widgets).
5. [Binding](#binding) of views to the state properties.


### *KView* and *KViewBox*

*KView* is an abstract class that wraps an *android.view.View*.
There's quite a bit more code in there, allowing for nested KViews, bindings, etc., but that's the essence of it. The KView class is ubiquitous in the KotlinUi lib, although the lib users needn't work with it directly, unless they're wrapping views in custom widgets.

*KViewBox* wraps a KRootView, allowing for a convenient placeholder for a view class that combines state properties and KViews.

#### KRootView

Your KViewBox must return an instance of KRootView, which is just a regular KView, except it allows the state binders to know when they've reached the top-most level. While you can specify your own root view, *rootColumn* and *rootRow* extension functions allow for seamless creation of a [Column](#kstack) and [Row](#kstack) root containers, respectively.

### Binding

KViews can be bound to Kotlin properties in order to:
 1. Automatically refresh themselves when the property is changed.
 2. Set the property value when their internal state changes.
 
#### bindTo

Method *bindTo* is used to bind a __KView__ to a __property__, so that the KView is updated when the property's setter is invoked:

```kotlin
class MyView(context: Context) : KViewBox(context) {
    var labelText: String? by state()
    
    ...
    text(R.string.initial_text).bindTo(::labelText)
    ...
}
```

Notice the **::** before the property name - this is Kotlin's way of saying that we're passing a property, instead of its value.


##### State

Properties that KViews are bound to **must be delegated through the State class**. The easiest way to do so it by using the *state* method, available to all KViews and KViewBoxes.
* If the *state* method is invoked without arguments, the property is assumed to be nullable and its default value is null.
* You must pass an initial value (e.g,, *state("Something")*) for non-null properties.

Now, when the prop is updated:
```kotlin
myViewInstance.labelText = "New text"
```

the text view will be updated automatically.

The *bindTo* method can take any number of properties, and the calling KView will be bound to all of them:

```kotlin
text("").bindTo(::someProp, ::otherProp)
```

##### Binding methods

Normally, updating a property invokes the **update** method of its bound KView, with the new value passed as its single parameter. Different KView widgets implement this method differently based on their expected behavior (e.g, a KText updates its text, while a KList refreshes its content).

You can also specify which method is to be invoked instead of the *update* method:

```kotlin
class MyView(context: Context) : KViewBox(context) {
    var showText = true
    
    ...
    text(R.string.initial_text).bindTo(::showText, KText::visible)
    ...
}
```

Now, whenever the *showText* prop is updated, the UI element that's bound to it will have its *visible* method invoked with the prop value passed as param. In this particular case, setting the *showText* prop to true or false will render the KText visible or invisible, respectively.

There's an overload of the *bindTo* method that takes a vararg of property-method pairs, and then binds them all to the caller KView. In order to make this easier to type, you can use the following syntax:

```kotlin
text(R.string.initial_text).bindTo(::showText updates KText::visible, ::textContent updates KText::text)
}
```

Note - bindTo on a *KStack* (*Column* or *Row*) will re-render its entire content.

##### State collections and lists

The KotlinUi lib provides two classes, *StateCollection* and *StateList*, that represent a mutable collection and list, respectively, that **updates its bound KViews when its content changes**. E.g, when a *StateList*'s *add* or *remove* methods are called (or any other mutating method), its bound KViews will be notified of the change.

```kotlin
val numbers = stateList(mutableListOf(1, 2, 3, 4), ::numbers)

...
list(::numbers) {
    text("$it")
}
...
```

The *stateList* function receives a mutable list it wraps, as well as the property reference that's used to trigger the KView observers. Note that *StateList*s and *StateCollection*s don't use the *State* delegate directly, but internally. Also note that such properties don't have to be mutable, like *State* delegates properties must.

#### bind

Method *bind* is used to bind a __property__ to a __KView__, so that the property's value is changed when the KView changes.

Here's an example of binding a Boolean to a KCheckbox, so that the prop changes when the checkbox is (un)checked:

```kotlin
var isChecked = false

...
checkBox(R.string.title, isChecked).bind(::isChecked)
...
```

Binding a String property to a KTextField makes it always reflect its text:

```kotlin
var text = ""

...
textField().bind(::text)
...
```

#### Convenience constructors - *bindTo* or *bind*

Most KView widget extension methods offer a variant that takes in a property, and then either binds it or binds to it. Which method is used is entirely up to the widget developer, but you can along the lines of dividing the widgets into two groups: those that *display* the state, and those that *change* the state.

E.g, a KText simply displays a text, and thus can be *bound to* a String prop. On the other hand, a KCheckBox responds to change events, and as such should *bind* a Boolean prop to reflect its state changes. Bear this distinction in mind when designing custom widgets and their convenience KView methods. 

Here's a quick reference of basic widgets and their convenience binder methods:


| Widget method | Binder | Type    |
| ------------- | ------ | ------- |
| button        | bindTo | String  |
| checkBox      | bind   | Boolean |
| list<D>       | bindTo | List<D> |
| text          | bindTo | String  |
| textField     | bind   | Boolean |

#### Infix methods

KotlinUi lib contains a number of infix functions that allow you to declare bindings in almost natural language. Consider this example:

```kotlin
 private class InfixDemo(context: Context) : KViewBox(context) {
    var buttonVisible: Boolean by state(true)
    var evenOnly: Boolean by state(false)

    lateinit var chbButtonVisible: KCheckBox
    lateinit var chbEvenOnly: KCheckBox
    lateinit var kbutton: KButton
    lateinit var klist: KList<*>

    override val root = rootColumn {
        checkBox("Button visible", buttonVisible).id(::chbButtonVisible)
        checkBox("Even only", evenOnly).id(::chbEvenOnly)
        button("Button") { }.id(::kbutton)
        list(listOf(1, 2, 3, 4)) {
            if (evenOnly && it % 2 == 1) {
                emptyView()
            } else {
                text("$it")
            }
        }.id(::klist)
    }

    init {
        chbButtonVisible updates ::buttonVisible
        ::buttonVisible updates KButton::visible of kbutton
        chbEvenOnly updates ::evenOnly
        ::evenOnly updates klist
    }
}
```

There are 3 variants of the *updates* infix function:

1. kView *updates* property = kView.bind(property)
2. property *updates* kView = kView.bindTo(property)
3. property *updates* method *of* kView = kView.bindTo(property, method)

You're, of course, free to use any syntax you find to be better, or even combine the two:

```kotlin
var input: String by state("")
var buttonVisible: Boolean by state(true)
var evenOnly: Boolean by state(false)

override val root = rootColumn {
    textField(::input)
    text(::input)
    checkBox("Button visible", ::buttonVisible)
    checkBox("Even only", ::evenOnly)
    ::buttonVisible updates KButton::visible of button("Button") { }
    ::evenOnly updates list(listOf(1, 2, 3, 4)) {
        if (evenOnly && it % 2 == 1) {
            emptyView()
        } else {
            text("$it")
        }
    }
}
```

### Widgets

Here's a table of currently available KView widgets that wrap common Android Views:

| Widget        | Wraps    | bindTo updates    | Common usage |
| ------------- | ------   | -------           | ------- |
| KText       | TextView   | TextView text       | text(::observedProperty) |
| KButton       | Button   | Button text       | button(R.string.button_title) { buttonClicked() } |
| KCheckBox     | Checkbox | text if value is String, isChecked if Boolean       | checkbox(R.string.checkbox_title, ::boundProperty) |
| KImage        | ImageView | image if value is @DrawableRes Int or a Drawable | image(R.drawable.image) |
| KTextField    | EditText | EditText text | textField(::boundProperty) |
| Column        | LinearLayout - VERTICAL | redraws all children | column { text(R.string.text) } |
| Row           | LinearLayout - HORIZONTAL | redraws all children | row { text(R.string.text) } |
| KList         | RecyclerView | adapter.notifyDataSetChanged() | [See below](#klist) |

#### KStack

KStack wraps a LinearLayout. Two subclasses are available for general use - *Column* and *Row*.

You can use *space()* function to add a flexible empty space between elements:

```kotlin
row {
    image(landmark.getImageResId(context))
            .frame(150, 150)
    text(landmark.name).margins(10, 0, 0, 0)
    space()
    image(android.R.drawable.star_big_on)
}
```

#### KList

KList is a wrapper around a RecyclerView, allowing for much simpler creation of a list view at some performance expense.

Normally, a KList takes in two parameters - a List of items to display, and a renderer that maps those items into KViews:

```kotlin
list(listOf(1, 2, 3, 4)) {
    text("$it")
}
```

Above we see a KList operating on a list of integers, rendering a single KText in a row for each one of them.

You can bind a KList to a *StateList* property and have the list content change whenever your state list changes:

```kotlin
val landmarks = stateList(data, ::landmarks)

list(::landmarks) {
    if (showFavorites && !it.isFavorite) {
        emptyView()
    } else {
        LandmarkRow(context, it)
    }
}
```

Naturally, you can bind a KList to any property to have it refresh once that property changes, regardless of its type.

Above you can see two more useful traits of lists:

1. If a certain item should be omitted from the list, have the renderer block return *emptyView()*.
2. You can create custom KViews for rows and pass and return them from a renderer. E.g, here's a sample LandmarkRow:

```kotlin
class LandmarkRow(context: Context, private val landmark: Landmark) : KView<View>(context) {
    override val view = row {
            image(landmark.getImageResId(context))
                    .frame(150, 150)
            text(landmark.name).margins(10, 0, 0, 0)
            space()
            if (landmark.isFavorite) {
                image(android.R.drawable.star_big_on)
            }
        }.padding(5)
                .view
}
```


