package net.globulus.kotlinui

import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias ObservablesMap = MutableMap<String, State.Observable<*>>
typealias ObserversMap = MutableMap<String, MutableList<State.Observer<*>>>

interface Stateful {
    val observables: ObservablesMap
    val observers: ObserversMap

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
}

interface StatefulProducer {
    val stateful: Stateful?
}

interface State<R: StatefulProducer, T> : ReadWriteProperty<R, T>, UpdatesObservable<R, T> {
    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )

    data class Observer<R>(
            val originalKView: KView<*>,
            val consumer: Consumer<R>
    )
}

class NullableState<R: StatefulProducer, T>(override val producer: R) : State<R, T?> {
    private var field: T? = null
    override var updatedObservable = false

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        updateObservable(property)
        field = value
        producer.stateful?.triggerObserver(property.name, field)
    }
}

class NonNullState<R: StatefulProducer, T: Any>(override val producer: R) : State<R, T> {
    private lateinit var field: T
    override var updatedObservable = false

    constructor(producer: R, initialValue: T) : this(producer) {
        field = initialValue
    }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        updateObservable(property)
        field = value
        producer.stateful?.triggerObserver(property.name, field)
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

fun <R: StatefulProducer, T: Any> R.state() = NonNullState<R, T>(this)
fun <R: StatefulProducer, T: Any> R.state(initialValue: T) = NonNullState(this, initialValue)
fun <R: StatefulProducer, T> R.optionalState() = NullableState<R, T>(this)
fun <D, R: StatefulProducer, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
        = StateList(this, field, property)
