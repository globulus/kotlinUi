package net.globulus.kotlinui

import android.app.Activity
import android.content.Context
import android.view.View

abstract class KViewBox(val context: Context) : StatefulProducer {

    abstract val root: KRootView<*>

    val view: View
        get() = root.view

    override val stateful: Stateful?
        get() = root
}

fun Activity.setContentView(box: KViewBox) {
    setContentView(box.view)
}
