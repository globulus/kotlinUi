package net.globulus.kotlinui

import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class State<in R: KView, T>(private val kview: R) : ReadWriteProperty<R, T?> {

    private var field: T? = null

    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        updateObservable(property)
        return field
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        updateObservable(property)
        field = value
        kview.triggerObserver(property.name, field)
    }

    private fun updateObservable(property: KProperty<*>) {
        val name = property.name
        if (!kview.observables.containsKey(name)) {
            kview.observables[name] = BehaviorSubject.create<T>()
        }
    }
}