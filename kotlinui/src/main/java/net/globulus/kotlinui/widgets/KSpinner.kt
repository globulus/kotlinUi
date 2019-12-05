package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.ProgressBar
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.visible
import kotlin.reflect.KProperty

class KSpinner(
        context: Context,
        @StyleRes style: Int = 0
) : KView<ProgressBar>(context) {

    override val view = ProgressBar(context, null,0, style)

    override fun <R> updateValue(r: R) {
        when (r) {
            is Boolean -> visible(r)
            else -> throw IllegalArgumentException("KSpinner must be bound to a Boolean prop!")
        }
    }
}

fun <T: KView<*>> T.spinner(prop: KProperty<Boolean>, @StyleRes style: Int = 0): KSpinner {
    return add(KSpinner(context, style).bindTo(prop))
}
