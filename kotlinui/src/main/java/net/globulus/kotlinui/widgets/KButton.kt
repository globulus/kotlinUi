package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bindTo
import kotlin.reflect.KProperty

typealias OnClickListener = (View) -> Unit

class KButton(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        l: OnClickListener?
) : KView(context) {
    private val b = Button(context).apply {
        if (resId == 0) {
            this.text = text
        } else {
            setText(resId)
        }
        setOnClickListener(l)
    }

    override val view: View
        get() = b
}

fun <T: KView> T.button(@StringRes resId: Int, l: OnClickListener?): KButton {
    return add(KButton(context, resId, null, l))
}

fun <T: KView> T. button(text: String?, l: OnClickListener?): KButton {
    return add(KButton(context, 0, text, l))
}

fun <T: KView> T. button(prop: KProperty<String>, l: OnClickListener?): KButton {
    return button(prop.getter.call(), l).bindTo(prop)
}
