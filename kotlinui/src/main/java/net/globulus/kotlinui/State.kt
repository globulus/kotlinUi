package net.globulus.kotlinui

import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface State<R: KView, T> : ReadWriteProperty<R, T>, UpdatesObservable<R, T>

class NullableState<R: KView, T>(override val kview: R) : State<R, T?> {

    private var field: T? = null
    override var updatedObservable = false

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        updateObservable(property)
        field = value
        kview.triggerObserver(property.name, field)
    }
}

class NonNullState<R: KView, T: Any>(override val kview: R) : State<R, T> {

    private lateinit var field: T
    override var updatedObservable = false

    constructor(kview: R, initialValue: T) : this(kview) {
        field = initialValue
    }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        updateObservable(property)
        field = value
        kview.triggerObserver(property.name, field)
    }
}

interface UpdatesObservable<R: KView, T> {

    val kview: R
    var updatedObservable: Boolean

    fun updateObservable(property: KProperty<*>) {
        if (updatedObservable) {
            return
        }
        updatedObservable = true
        val name = property.name
        if (!kview.observables.containsKey(name)) {
            kview.observables[name] = KView.Observable(BehaviorSubject.create<T>())
        }
    }
}
