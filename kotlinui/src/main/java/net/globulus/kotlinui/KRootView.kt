package net.globulus.kotlinui

import android.view.View
import net.globulus.kotlinui.traits.RootContainer

class KRootView<out V: View>(private val initialView: KView<V>) : KView<V>(initialView.context) {

    override val view = initialView.view

    init {
        super.add(initialView)
        if (initialView is RootContainer) {
            initialView.invokeBlock()
        }
    }

    override fun <T : KView<*>> add(v: T): T {
        return initialView.add(v)
    }

    override fun <R> updateValue(r: R) {
        initialView.updateValue(r)
    }
}

fun <V: View> root(initialView: KView<V>) = KRootView(initialView)
