package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty

class KText(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        @StyleRes style: Int = 0
) : KView<TextView>(context), TextContainer<KText> {

    override val view = TextView(context, null, 0, style).apply {
        if (resId != 0) {
            setText(resId)
        } else {
            this.text = text
        }
    }

    override fun <R> updateValue(r: R) {
        view.text = r.toString()
    }

    override fun text(text: String?): KText {
        view.text = text
        return this
    }

    override fun text(resId: Int): KText {
        view.setText(resId)
        return this
    }

    override fun textSize(size: Float): KText {
        view.textSize = size
        return this
    }

    override fun textColor(color: Int): KText {
        view.setTextColor(color)
        return this
    }
}

fun <T: KView<*>> T.text(@StringRes resId: Int): KText {
    return add(KText(context, resId))
}

fun <T: KView<*>> T.text(text: String? = null): KText {
    return add(KText(context, 0, text))
}

fun <T: KView<*>> T.text(prop: KProperty<String>): KText {
    return text(prop.getter.call()).bindTo(prop)
}
