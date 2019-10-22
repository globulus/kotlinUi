package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import net.globulus.kotlinui.KView

open class KStack(context: Context, o: Int) : KView<LinearLayout>(context) {

    override val view= LinearLayout(context).apply {
        orientation = o
    }

    override fun addView(v: View) {
        view.addView(v)
    }
}

class Column(context: Context) : KStack(context, LinearLayout.VERTICAL)

class Row(context: Context) : KStack(context, LinearLayout.HORIZONTAL)

fun <T: KStack> T.space(): T {
    val width = if (Column::class.java.isAssignableFrom(this::class.java)) LinearLayout.LayoutParams.MATCH_PARENT else 0
    val height = if (Row::class.java.isAssignableFrom(this::class.java)) LinearLayout.LayoutParams.MATCH_PARENT else 0
    view.addView(Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(width, height, 1F)
    })
    return this
}

fun <T: KView<*>> T.column(block: Column.() -> Unit): Column {
    return add(Column(context).apply {
        block()
    })
}

fun <T: KView<*>> T.row(block: Row.() -> Unit): Row {
    return add(Row(context).apply {
        block()
    })
}
