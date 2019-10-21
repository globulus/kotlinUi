package net.globulus.kotlinui

import android.content.Context
import android.view.View
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

abstract class KView(val context: Context) {

    val observables = mutableMapOf<String, Observable<*>>()
    val observers = mutableMapOf<String, MutableList<Consumer<*>>>()

    abstract val view: View

    lateinit var id: String
        private set
    protected var viewCounter = 0
    protected val viewMap = mutableMapOf<String, KView>()
    var parent: KView? = null
        protected set
    val superParent: KView?
        get() {
            var p = parent
            while (p?.parent != null) {
                p = p.parent
            }
            return p
        }

    protected open fun addView(v: View) { }

    fun <V: View> add(v: V): V {
        addView(v)
        return v
    }

    fun <V: KView> add(v: V): V {
        addView(v.view)
        v.parent = this
        v.id = v::class.java.simpleName + viewCounter
        viewCounter += v.viewCounter + 1
        viewMap[v.id] = v
        viewMap.putAll(v.viewMap)
        return v
    }

    fun id(id: String): KView  {
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

    fun bindChildren(kviews: Collection<KView>? = null) {
        val children = kviews ?: viewMap.values
        for (child in children) {
            observers.putAll(child.observers)
            bindChildren(child.viewMap.values)
        }
    }

    open fun <R> update(r: R) { }

    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )
}

fun kview(context: Context, block: KView.() -> KView): View {
    return object : KView(context) {
        override val view: View
            get() = block().view
    }.view
}

//@Suppress("UNCHECKED_CAST")
//inline fun <reified T: KView> T.bound(): T {
//    return Class.forName(T::class.java.name + FrameworkUtil.BOUND_SUFFIX)
//            .getConstructor(Context::class.java, T::class.java)
//            .newInstance(this.context, this) as T
//}

fun <P: KView, T: KView, R> T.bindTo(parent: P, field: KProperty<R>): T {
    val name = field.name
    with(parent) {
        if (observers[name] == null) {
            observers[name] = mutableListOf()
        }
        observers[name]?.add(Consumer<R> {
            update(it)
        })
    }
    return this
}

fun <T: KView, R> T.bindTo(field: KProperty<R>): T {
    return bindTo(this, field)
}

@Suppress("UNCHECKED_CAST")
fun <T: KView> T.id(prop: KMutableProperty<T>): T {
    prop.setter.call(this)
    return id(prop.name) as T
}

fun <R: KView, T: Any> R.state() = NonNullState<R, T>(this)
fun <R: KView, T> R.optionalState() = NullableState<R, T>(this)
fun <D, R: KView, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
        = StateList(this, field, property)
