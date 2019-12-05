package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.KViewBox
import net.globulus.kotlinui.root
import net.globulus.kotlinui.traits.RootContainer

abstract class KLinearLayout<T: KLinearLayout<T>> internal constructor(
        context: Context,
        o: Int,
        gravity: Int,
        invokeBlockNow: Boolean,
        protected val block: T.() -> Unit
) : KView<LinearLayout>(context), RootContainer {

    override val view: LinearLayout = LinearLayout(context).apply {
        orientation = o
        this.gravity = gravity
    }

    init {
        if (invokeBlockNow) {
            invokeBlock()
        }
    }

    override fun addView(v: View) {
        view.addView(v)
    }

    override fun <R> updateValue(r: R) {
        removeAllChildren()
        view.removeAllViews()
        invokeBlock()
    }
}

class Column(context: Context, gravity: Int, invokeBlockNow: Boolean, block: Column.() -> Unit)
    : KLinearLayout<Column>(context, LinearLayout.VERTICAL, gravity, invokeBlockNow, block) {
    override fun invokeBlock() {
        block()
    }
}

class Row(context: Context, gravity: Int, invokeBlockNow: Boolean, block: Row.() -> Unit)
    : KLinearLayout<Row>(context, LinearLayout.HORIZONTAL, gravity, invokeBlockNow, block) {
    override fun invokeBlock() {
        block()
    }
}

fun <T: KLinearLayout<*>> T.space(): T {
    val width = if (Column::class.java.isAssignableFrom(this::class.java)) LinearLayout.LayoutParams.MATCH_PARENT else 0
    val height = if (Row::class.java.isAssignableFrom(this::class.java)) LinearLayout.LayoutParams.MATCH_PARENT else 0
    view.addView(Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(width, height, 1F)
    })
    return this
}

fun <T: KLinearLayout<*>> T.gravity(gravity: Int): T {
    return apply {
        view.gravity = gravity
    }
}

fun <T: KView<*>> T.column(block: Column.() -> Unit): Column {
    return column(Gravity.NO_GRAVITY, block)
}

fun <T: KView<*>> T.column(gravity: Int, block: Column.() -> Unit): Column {
    return add(Column(context, gravity, true, block))
}

fun <T: KView<*>> T.row(block: Row.() -> Unit): Row {
    return row(Gravity.NO_GRAVITY, block)
}

fun <T: KView<*>> T.row(gravity: Int, block: Row.() -> Unit): Row {
    return add(Row(context, gravity, true, block))
}

fun <T: KViewBox> T.rootColumn(block: Column.() -> Unit) = rootColumn(Gravity.NO_GRAVITY, block)

fun <T: KViewBox> T.rootColumn(gravity: Int, block: Column.() -> Unit) = root(Column(context, gravity, false, block))

fun <T: KViewBox> T.rootRow(block: Row.() -> Unit) = rootRow(Gravity.NO_GRAVITY, block)

fun <T: KViewBox> T.rootRow(gravity: Int, block: Row.() -> Unit) = root(Row(context, gravity,false, block))
