package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty

class KText(
        context: Context,
        @StringRes resId: Int,
        text: String? = null
) : KView(context), TextContainer<KText> {

    private val tv = TextView(context).apply {
        if (resId != 0) {
            setText(resId)
        } else {
            this.text = text
        }
    }

    override val view: View
        get() = tv

    override fun <R> update(r: R) {
        tv.text = r.toString()
    }

    override fun text(text: String?): KText {
        tv.text = text
        return this
    }

    override fun text(resId: Int): KText {
        tv.setText(resId)
        return this
    }

    override fun textSize(size: Float): KText {
        tv.textSize = size
        return this
    }

    override fun textColor(color: Int): KText {
        tv.setTextColor(color)
        return this
    }
}

fun <T: KView> T.text(@StringRes resId: Int): KText {
    return add(KText(context, resId))
}

fun <T: KView> T.text(text: String? = null): KText {
    return add(KText(context, 0, text))
}

fun <T: KView> T.text(prop: KProperty<String>): KText {
    return text(prop.getter.call()).bindTo(prop)
}
