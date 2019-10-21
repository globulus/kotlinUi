package net.globulus.kotlinui.demo

import android.content.Context
import android.view.View
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.state
import net.globulus.kotlinui.stateList
import java.util.*

class Kv(context: Context) : KView(context) {

    var buttonTitle: String by state()
    val listItems = stateList(mutableListOf("A", "B", "C"), ::listItems)

    override val view: View
        get() = column {
                text(R.string.label_1).bindTo(::buttonTitle)
                button(R.string.button_1) {
                    buttonTitle = Date().toString()
                    listItems.add(buttonTitle)
                }
                list(::listItems) {
                    text(it)
                }
            }.view
}
