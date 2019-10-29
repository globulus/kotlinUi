package net.globulus.kotlinui

import java.util.function.Predicate
import kotlin.reflect.KProperty

open class StateCollection<D, R: StatefulProducer, T: MutableCollection<D>>(
        override val producer: R,
        protected val field: T,
        private val property: KProperty<*>
) : MutableCollection<D>, UpdatesObservable<R, T> {

    override var updatedObservable = false

    override val size: Int
        get() = this.field.size

    override fun contains(element: D): Boolean {
        return field.contains(element)
    }

    override fun containsAll(elements: Collection<D>): Boolean {
        return field.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return field.isEmpty()
    }

    override fun add(element: D): Boolean {
        val result = field.add(element)
        notifyObservers()
        return result
    }

    override fun addAll(elements: Collection<D>): Boolean {
        val result = field.addAll(elements)
        notifyObservers()
        return result
    }

    override fun clear() {
        field.clear()
        notifyObservers()
    }

    override fun remove(element: D): Boolean {
        val result = field.remove(element)
        notifyObservers()
        return result
    }

    override fun removeAll(elements: Collection<D>): Boolean {
        val result = field.removeAll(elements)
        notifyObservers()
        return result
    }

    override fun removeIf(filter: Predicate<in D>): Boolean {
        val result = field.removeIf(filter)
        notifyObservers()
        return result
    }

    override fun retainAll(elements: Collection<D>): Boolean {
        val result = field.retainAll(elements)
        notifyObservers()
        return result
    }

    override fun iterator(): MutableIterator<D> {
        return field.iterator()
    }

    protected fun notifyObservers() {
        updateObservable(property)
        producer.stateful?.let {
            it.triggerObserver(property.name, field)
            it.triggerObserver(it.toString(), field)
        }
    }
}

class StateList<D, R: StatefulProducer, T: MutableList<D>>(
        kview: R,
        field: T,
        property: KProperty<*>
) : StateCollection<D, R, T>(kview, field, property), MutableList<D> {
    
    override fun get(index: Int): D {
        return field[index]
    }

    override fun indexOf(element: D): Int {
        return field.indexOf(element)
    }

    override fun lastIndexOf(element: D): Int {
        return field.lastIndexOf(element)
    }

    override fun add(index: Int, element: D) {
        field.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<D>): Boolean {
        return field.addAll(index, elements)
    }

    override fun listIterator(): MutableListIterator<D> {
        return field.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<D> {
       return field.listIterator(index)
    }

    override fun removeAt(index: Int): D {
        val result = field.removeAt(index)
        notifyObservers()
        return result
    }

    override fun set(index: Int, element: D): D {
        val result = field.set(index, element)
        notifyObservers()
        return result
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<D> {
        return field.subList(fromIndex, toIndex)
    }
}

fun <D, R: StatefulProducer, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
        = StateList(this, field, property)
