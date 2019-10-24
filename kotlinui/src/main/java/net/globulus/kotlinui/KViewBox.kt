package net.globulus.kotlinui

import android.app.Activity
import android.content.Context
import android.view.View

abstract class KViewBox(val context: Context) : KViewProducer {

    abstract val root: KRootView<*>

    val view: View
        get() = root.view

    override val kView: KView<*>?
        get() = root
}

fun Activity.setContentView(box: KViewBox) {
    setContentView(box.view)
}
