package net.globulus.kotlinui

import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter

typealias ObservablesMap = MutableMap<String, State.Observable<*>>
typealias ObserversMap = MutableMap<String, MutableList<State.Observer<*>>>
typealias BoundWritePropertiesMap = MutableMap<String, MutableList<KMutableProperty<*>>>

interface Stateful {
    val observables: ObservablesMap
    val observers: ObserversMap
    val boundWriteProperties: BoundWritePropertiesMap

    fun <R> updateValue(r: R)

    @Suppress("UNCHECKED_CAST")
    fun <T> triggerObserver(key: String, value: T) {
        observables[key]?.let { observable ->
            val bs = observable.behaviorSubject as BehaviorSubject<T>
            if (!observable.bound) {
                observable.bound = true
                observers[key]?.let { observers ->
                    for (c in observers) {
                        bs.subscribe(c.consumer as Consumer<T>)
                    }
                }
            }
            bs.onNext(value)
        }
    }

    abstract class Default : Stateful {
        override val observables: ObservablesMap = mutableMapOf()
        override val observers: ObserversMap = mutableMapOf()
        override val boundWriteProperties: BoundWritePropertiesMap = mutableMapOf()
    }

    companion object {
        fun default(update: ((Any) -> Unit)? = null): Stateful {
            return object : Default() {
                init {
                    observables[toString()] = State.Observable(BehaviorSubject.create<Any?>())
                }

                override fun <R> updateValue(r: R) {
                    update?.invoke(r as Any)
                }
            }
        }
    }
}

interface StatefulProducer {
    val stateful: Stateful?
}

interface AllowsSupplementarySet<T> {
    val supplementarySet: ((T) -> Unit)?
}

interface State<R: StatefulProducer, T> : ReadWriteProperty<R, T>,
    UpdatesObservable<R, T>, AllowsSupplementarySet<T> {

    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )

    data class Observer<R>(
            val sender: Stateful,
            val consumer: Consumer<R>
    )

    fun triggerObserver(property: KProperty<*>, value: T) {
        producer.stateful?.let {
            it.triggerObserver(property.name, value)
            it.triggerObserver(it.toString(), value)
        }
    }
}

class NullableState<R: StatefulProducer, T>(
    override val producer: R,
    override val supplementarySet: ((T?) -> Unit)? = null
) : State<R, T?> {
    private var field: T? = null
    override var updatedObservable = false

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        updateObservable(property)
        field = value
        triggerObserver(property, value)
        supplementarySet?.invoke(value)
    }
}

class NonNullState<R: StatefulProducer, T: Any>(
    override val producer: R,
    override val supplementarySet: ((T) -> Unit)? = null
) : State<R, T> {
    private lateinit var field: T
    override var updatedObservable = false

    constructor(producer: R,
                initialValue: T,
                supplementarySet: ((T) -> Unit)? = null
    ) : this(producer, supplementarySet) {
        field = initialValue
    }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        updateObservable(property)
        field = value
        triggerObserver(property, value)
        supplementarySet?.invoke(value)
    }
}

interface UpdatesObservable<R: StatefulProducer, T> {
    val producer: R
    var updatedObservable: Boolean

    fun updateObservable(property: KProperty<*>) {
        if (updatedObservable) {
            return
        }
        producer.stateful?.let {
            updatedObservable = true
            val name = property.name
            if (!it.observables.containsKey(name)) {
                it.observables[name] = State.Observable(BehaviorSubject.create<T>())
            }
        }
    }
}

fun <R: StatefulProducer, T: Any> R.state(supplementarySet: ((T) -> Unit)? = null)
    = NonNullState(this, supplementarySet)
fun <R: StatefulProducer, T: Any> R.state(initialValue: T, supplementarySet: ((T) -> Unit)? = null)
    = NonNullState(this, initialValue, supplementarySet)
fun <R: StatefulProducer, T> R.optionalState(supplementarySet: ((T?) -> Unit)? = null)
    = NullableState<R, T>(this, supplementarySet)

internal fun <P: StatefulProducer, T: Stateful, R> T.bindTo(
        root: P,
        name: String,
        callback: KCallable<T>? = null
): T {
    root.stateful?.let { stateful ->
        if (stateful.observers[name] == null) {
            stateful.observers[name] = mutableListOf()
        }
        stateful.observers[name]?.add(State.Observer(this, Consumer<R> {
            if (callback != null) {
                if (callback.instanceParameter == null && callback.extensionReceiverParameter == null) {
                    callback.call(it)
                } else {
                    callback.call(this, it)
                }
            } else {
                updateValue(it)
            }
        }))
        stateful.observables[name]?.bound = false
    }
    return this
}

fun <P: StatefulProducer, T: Stateful, R> T.bindTo(
        root: P,
        prop: KProperty<R>,
        callback: KCallable<T>? = null
) = bindTo<P, T, R>(root = root, name = prop.name, callback = callback)

fun <P: StatefulProducer, T: Stateful> T.bindTo(producer: P): T {
    val name = producer.stateful?.toString()
            ?: throw IllegalArgumentException("Attempt to bind to a null stateful!")
    return bindTo<P, T, Any>(producer, name)
}

inline fun <T: Stateful, reified R> T.bind(prop: KMutableProperty<R>): T {
    val name = R::class.java.name
    if (!boundWriteProperties.containsKey(name)) {
        boundWriteProperties[name] = mutableListOf()
    }
    boundWriteProperties[name]?.add(prop)
    return this
}
