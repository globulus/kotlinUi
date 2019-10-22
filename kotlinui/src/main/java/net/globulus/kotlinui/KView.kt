package net.globulus.kotlinui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

abstract class KView<V: View>(val context: Context) {

    val observables = mutableMapOf<String, Observable<*>>()
    val observers = mutableMapOf<String, MutableList<Consumer<*>>>()
    val boundWriteProperties = mutableMapOf<String, MutableList<KMutableProperty<*>>>()

    abstract val view: V

    lateinit var id: String
        private set
    protected var viewCounter = 0
    protected val viewMap = mutableMapOf<String, KView<*>>()
    var parent: KView<*>? = null
        protected set
    val superParent: KView<*>?
        get() {
            var p = parent
            while (p?.parent != null) {
                p = p.parent
            }
            return p
        }

    protected open fun addView(v: View) { }

    fun <T: View> add(v: T): T {
        addView(v)
        return v
    }

    fun <T: KView<*>> add(v: T): T {
        addView(v.view)
        v.parent = this
        v.id = v::class.java.simpleName + viewCounter
        viewCounter += v.viewCounter + 1
        viewMap[v.id] = v
        viewMap.putAll(v.viewMap)
        return v
    }

    fun id(id: String): KView<V>  {
        val oldId = this.id
        this.id = id
        parent?.viewMap?.let {
            it.remove(oldId)
            it.put(id, this)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> triggerObserver(key: String, value: T) {
        observables[key]?.let { observable ->
            val bs = observable.behaviorSubject as BehaviorSubject<T>
            if (!observable.bound) {
                observable.bound = true
                bindChildren()
                observers[key]?.let { observers ->
                    for (c in observers) {
                        bs.subscribe(c as Consumer<T>)
                    }
                }
            }
            bs.onNext(value)
        }
    }

    fun bindChildren(kviews: Collection<KView<*>>? = null) {
        val children = kviews ?: viewMap.values
        for (child in children) {
            observers.putAll(child.observers)
            bindChildren(child.viewMap.values)
        }
    }

    protected inline fun <reified R> notifyWriteProperties(value: R) {
        boundWriteProperties[R::class.java.name]?.forEach {
            it.setter.call(value)
        }
    }

    open fun <R> updateValue(r: R) { }

    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )
}

fun <V: View> kview(context: Context, block: KView<V>.() -> KView<V>): KView<V> {
    return object : KView<V>(context) {
        override val view: V
            get() = block().view
    }
}

fun <V: View> kview_(context: Context, block: KView<V>.() -> KView<V>) = kview(context, block)

fun <V:View, T: KView<V>> T.applyOnView(block: V.() -> Unit): T {
    this.view.apply(block)
    return this
}

//@Suppress("UNCHECKED_CAST")
//inline fun <reified T: KView> T.bound(): T {
//    return Class.forName(T::class.java.name + FrameworkUtil.BOUND_SUFFIX)
//            .getConstructor(Context::class.java, T::class.java)
//            .newInstance(this.context, this) as T
//}

fun <P: KView<*>, T: KView<*>, R> T.bindTo(parent: P, field: KProperty<R>): T {
    val name = field.name
    with(parent) {
        if (observers[name] == null) {
            observers[name] = mutableListOf()
        }
        observers[name]?.add(Consumer<R> {
            updateValue(it)
        })
    }
    return this
}

fun <T: KView<*>, R> T.bindTo(field: KProperty<R>): T {
    return bindTo(this, field)
}

inline fun <T: KView<*>, reified R> T.bind(field: KMutableProperty<R>): T {
    val name = R::class.java.name
    if (!boundWriteProperties.containsKey(name)) {
        boundWriteProperties[name] = mutableListOf()
    }
    boundWriteProperties[name]?.add(field)
    return this
}

@Suppress("UNCHECKED_CAST")
fun <T: KView<*>> T.id(prop: KMutableProperty<T>): T {
    prop.setter.call(this)
    return id(prop.name) as T
}

fun <R: KView<*>, T: Any> R.state() = NonNullState<R, T>(this)
fun <R: KView<*>, T: Any> R.state(initialValue: T) = NonNullState(this, initialValue)
fun <R: KView<*>, T> R.optionalState() = NullableState<R, T>(this)
fun <D, R: KView<*>, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
        = StateList(this, field, property)

fun <T: KView<*>> T.frame(width: Int, height: Int): T {
    return apply {
        view.layoutParams = LinearLayout.LayoutParams(width, height)
    }
}

fun <T: KView<*>> T.padding(left: Int, top: Int, right: Int, bottom: Int): T {
    return apply {
        view.setPadding(left, top, right, bottom)
    }
}

fun <T: KView<*>> T.padding(p: Int): T {
    return padding(p, p, p, p)
}

fun <T: KView<*>> T.margins(left: Int, top: Int, right: Int, bottom: Int): T {
    return apply {
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            it.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }
}

fun <T: KView<*>> T.margins(m: Int): T {
    return margins(m, m, m, m)
}

fun <T: KView<*>> T.visible(visibility: Int): T {
    return apply {
        view.visibility = visibility
    }
}

fun <T: KView<*>> T.visible(visible: Boolean): T {
    return apply {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}

typealias OnClickListener = (View) -> Unit

fun <T: KView<*>> T.onClickListener(l: OnClickListener?): T {
    return apply {
        view.setOnClickListener(l)
    }
}

fun <T: KView<*>> T.emptyView() = object : KView<View>(context) {
    override val view = View(context)
}
