package net.globulus.kotlinui

import kotlin.reflect.KProperty

open class StateCollection<D, R: KView<*>, T: MutableCollection<D>>(
        override val kview: R,
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
        kview.triggerObserver(property.name, field)
    }
}

class StateList<D, R: KView<*>, T: MutableList<D>>(
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
