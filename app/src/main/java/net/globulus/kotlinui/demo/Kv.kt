package net.globulus.kotlinui.demo

import android.content.Context
import android.view.View
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.annotation.KotlinUiConfig
import net.globulus.kotlinui.annotation.State

@KotlinUiConfig(sink = true)
open class Kv(context: Context) : KView(context) {

    @set:State
    open lateinit var buttonTitle: String

    override val view: View
        get() = column {
            text(R.string.label_1)
            button(R.string.button_1) {
            }
        }.view

}