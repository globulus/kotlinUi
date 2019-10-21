package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import net.globulus.kotlinui.KView

open class KStack(context: Context, o: Int) : KView(context) {
    private val ll = LinearLayout(context).apply {
        orientation = o
    }

    override val view: View
        get() = ll

    override fun addView(v: View) {
        ll.addView(v)
    }
}

class Column(context: Context) : KStack(context, LinearLayout.VERTICAL)

class Row(context: Context) : KStack(context, LinearLayout.HORIZONTAL)


fun <T: KView> T.column(block: Column.() -> Unit): Column {
    return add(Column(context).apply {
        block()
    })
}

fun <T: KView> T.row(block: Row.() -> Unit): Row {
    return add(Row(context).apply {
        block()
    })
}
