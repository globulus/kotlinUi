package net.globulus.kotlinui

import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface State<R: KViewProducer, T> : ReadWriteProperty<R, T>, UpdatesObservable<R, T>

class NullableState<R: KViewProducer, T>(override val producer: R) : State<R, T?> {

    private var field: T? = null
    override var updatedObservable = false

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        updateObservable(property)
        field = value
        producer.kView?.triggerObserver(property.name, field)
    }
}

class NonNullState<R: KViewProducer, T: Any>(override val producer: R) : State<R, T> {

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
        producer.kView?.triggerObserver(property.name, field)
    }
}

interface UpdatesObservable<R: KViewProducer, T> {

    val producer: R
    var updatedObservable: Boolean

    fun updateObservable(property: KProperty<*>) {
        if (updatedObservable) {
            return
        }
        producer.kView?.let {
            updatedObservable = true
            val name = property.name
            if (!it.observables.containsKey(name)) {
                it.observables[name] = KView.Observable(BehaviorSubject.create<T>())
            }
        }
    }
}
