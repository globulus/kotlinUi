package net.globulus.kotlinui.demo

import android.content.Context
import android.view.View
import net.globulus.kotlinui.*
import net.globulus.kotlinui.widgets.*
import java.util.*

class Kv(context: Context) : KView<View>(context) {

    lateinit var goButton: KButton
    var buttonTitle: String by state()
    val listItems = stateList(mutableListOf("A", "B", "C"), ::listItems)

    override val view: View
        get() = column {
                padding(10)
                row {
                    text("Current date is").textSize(22F)
                    space()
                    text(R.string.label_1).bindTo(::buttonTitle)
                }
                button(R.string.button_1) {
                    buttonTitle = Date().toString()
                    listItems.add(buttonTitle)
                }.id(::goButton)
                list(bindTo = ::listItems) {
                    text(it)
                }
            }.view
}
