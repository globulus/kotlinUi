package net.globulus.kotlinui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter

private typealias ObservablesMap = MutableMap<String, KView.Observable<*>>
private typealias ObserversMap = MutableMap<String, MutableList<KView.Observer<*>>>
private typealias BoundWritePropertiesMap = MutableMap<String, MutableList<KMutableProperty<*>>>

abstract class KView<out V: View>(val context: Context) : KViewProducer {

    val observables: ObservablesMap = mutableMapOf()
    val observers: ObserversMap = mutableMapOf()
    val boundWriteProperties: BoundWritePropertiesMap = mutableMapOf()

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
    val rootSuperParent: KRootView<*>?
        get() {
            var v: KView<*>? = this
            while (true) {
                if (v is KRootView) {
                    return v
                }
                v = v?.parent
                if (v == null) {
                    return null
                }
            }
        }

    override val kView
        get() = this

    protected open fun addView(v: View) { }

    fun <T: View> add(v: T): T {
        addView(v)
        return v
    }

    open fun <T: KView<*>> add(v: T): T {
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
//                bindChildren()
                observers[key]?.let { observers ->
                    for (c in observers) {
                        bs.subscribe(c.consumer as Consumer<T>)
                    }
                }
            }
            bs.onNext(value)
        }
    }

//    fun bindChildren(kviews: Collection<KView<*>>? = null) {
//        val children = kviews ?: viewMap.values
//        for (child in children) {
//            for ((k, v) in child.observers) {
//                if (observers.containsKey(k)) {
//                    observers[k]?.addAll(v)
//                } else {
//                    observers[k] = v
//                }
//            }
//            observers.putAll(child.observers)
//            bindChildren(child.viewMap.values)
//        }
//    }

    protected inline fun <reified R> notifyWriteProperties(value: R) {
        boundWriteProperties[R::class.java.name]?.forEach {
            it.setter.call(value)
        }
    }

    open fun <R> updateValue(r: R) { }

    protected fun removeAllChildren() {
        rootSuperParent?.let {
            for ((_, v) in it.observables) {
                v.bound = false
            }
            for ((_, v) in viewMap)
                for ((_, observers) in it.observers) {
                    observers.removeIf { o -> o.originalKView == v }
                }
        }
        viewCounter = 0
        viewMap.clear()
    }

    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )

    data class Observer<R>(
            val originalKView: KView<*>,
            val consumer: Consumer<R>
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

fun <P: KRootView<*>, T: KView<*>, R> T.bindTo(
        root: P,
        prop: KProperty<R>,
        callback: KFunction<T>? = null
): T {
    val name = prop.name
    if (root.observers[name] == null) {
        root.observers[name] = mutableListOf()
    }
    root.observers[name]?.add(KView.Observer(this, Consumer<R> {
        if (callback != null) {
            if (callback.extensionReceiverParameter == null) {
                callback.call(it)
            } else {
                callback.call(this, it)
            }
        } else {
            updateValue(it)
        }
    }))
    return this
}

fun <T: KView<*>, R> T.bindTo(prop: KProperty<R>, callback: KFunction<T>? = null): T {
    val root = rootSuperParent
            ?: throw IllegalStateException("${this} doesn\'t have a root super parent!")
    bindTo(root, prop, callback)
    return this
}

fun <T: KView<*>, R> T.bindTo(vararg props: KProperty<R>): T {
    val root = rootSuperParent
            ?: throw IllegalStateException("${this} doesn\'t have a root super parent!")
    for (prop in props) {
        bindTo(root, prop)
    }
    return this
}

fun <T: KView<*>, R> T.bindTo(vararg pairs: Pair<KProperty<R>, KFunction<T>>): T {
    val root = rootSuperParent
            ?: throw IllegalStateException("${this} doesn\'t have a root super parent!")
    for (pair in pairs) {
        bindTo(root, pair.first, pair.second)
    }
    return this
}

inline fun <T: KView<*>, reified R> T.bind(prop: KMutableProperty<R>): T {
    val name = R::class.java.name
    if (!boundWriteProperties.containsKey(name)) {
        boundWriteProperties[name] = mutableListOf()
    }
    boundWriteProperties[name]?.add(prop)
    return this
}

inline infix fun <T: KView<*>, reified R> T.updates(prop: KMutableProperty<R>): T {
    bind(prop)
    return this
}

infix fun <T: KView<*>, R> KProperty<R>.updates(kView: T): T {
    kView.bindTo(this)
    return kView
}

infix fun <T: KView<*>, R, A: KProperty<R>, B: KFunction<T>> A.updates(method: B): Pair<A, B> {
    return this to method
}

infix fun <T: KView<*>, R, A: KProperty<R>, B: KFunction<T>> Pair<A, B>.of(kView: T): T {
    kView.bindTo(this)
    return kView
}

@Suppress("UNCHECKED_CAST")
fun <T: KView<*>> T.id(prop: KMutableProperty<T>): T {
    prop.setter.call(this)
    return id(prop.name) as T
}

fun <R: KViewProducer, T: Any> R.state() = NonNullState<R, T>(this)
fun <R: KViewProducer, T: Any> R.state(initialValue: T) = NonNullState(this, initialValue)
fun <R: KViewProducer, T> R.optionalState() = NullableState<R, T>(this)
fun <D, R: KViewProducer, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
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

fun <T: KView<*>> T.visibility(visibility: Int): T {
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
