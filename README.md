# KotlinUi

Declarative UI building for Android in Kotlin


### Binding

KViews can be bound to Kotlin properties in order to:
 1. Automatically refresh themselves when the property is changed
 2. Set the property value when their internal state changes.
 
#### bindTo

Method *bindTo* is used to bind a KView to a property, so that the KView is updated when the property's setter is invoked:

```kotlin
class MyView(context: Context) : KViewBox(context) {
var labelText: String? by state()

...

text(R.string.initial_text).bindTo(::labelText)

...
}
```

Notice the **::** before the property name - this is Kotlin's way of saying that we're passing a property, instead of its value.

Properties that KViews are bound to **must be delegated through the State class**. The easiest way to do so it by using the *state* method, available to all KViews and KViewBoxes.
* If the *state* method is invoked without arguments, the property is assumed to be nullable and its default value is null.
* You must pass an initial value (e.g,, *state("Something")*) for non-null properties.

Now, when the prop is updated, e.g
```kotlin
myViewInstance.labelText = "New text"
```

the text view will be updated automatically.

The *bindTo* method can take any number of properties, and all will be bound to the calling KView:

```kotlin
text("").bindTo(::someProp, ::otherProp)
```

##### Binding methods

Normally, update to property invokes the *update* method of its bound view, with the new value passed as its single parameter. Different KView widgets implement this method differently based on their expected behavior (e.g, a KText updates its text, while a KList refreshes its content).

You can also specify which method is to be updated, e.g:

```kotlin
class MyView(context: Context) : KViewBox(context) {
var showText = true

...

text(R.string.initial_text).bindTo(::showText, KText::visible)

...
}
```

Now, whenever the *showText* prop is updated, the UI element that's bound to it will have its *visible* method invoked with the prop value passed as param. In this particular case, setting the *showText* prop to true or false will render the KText visible or invisible, respectively.

There's an overload of the *bindTo* method that takes a vararg of property-method pairs, and then binds them all to the caller KView. In order to make this easier, you can use the following syntax:

```kotlin
text(R.string.initial_text).bindTo(::showText updates KText::visible, ::textContent updates ::text)
}
```

Note - bindTo on a *KStack* (*Column* or *Row*) will re-render its entire content.

##### State collections and lists

The KotlinUi lib provides two classes, *StateCollection* and *StateList*, that represent a mutable collection and list, respectively, that **updates its bound KViews when its content changes**. E.g, when a *StateList*'s *add* or *remove* methods are called, its bound KViews will be notified of the change.

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

Method *bind* is used to bind a property to a KView, so that the property's value is changed when the KView changes.

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

Most KView widget extension methods offer a variant that takes in a property, and then either binds it or binds to it. Which method is used is entirely up to the widget developer, but the general way of thinking divides the widgets into two groups: those that *display* the state, and those that *change* the state.

E.g, KText simply displays a string, and thus can be *bound to* a String prop. On the other hand, a KCheckBox responds to change events, and as such should *bind* a Boolean prop to reflect its state changes. Bear this distinction in mind when designing custom widgets and their convenience KView methods. 

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
 private class InfixTest(context: Context) : KViewBox(context) {

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
            }.id(klist)
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
