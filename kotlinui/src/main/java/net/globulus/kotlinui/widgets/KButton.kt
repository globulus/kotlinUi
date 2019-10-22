package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.OnClickListener
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty

class KButton(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        l: OnClickListener?
) : KView(context), TextContainer<KButton> {
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

    override fun <R> updateValue(r: R) {
        b.text = r.toString()
    }

    override fun text(text: String?): KButton {
        b.text = text
        return this
    }

    override fun text(resId: Int): KButton {
        b.setText(resId)
        return this
    }

    override fun textSize(size: Float): KButton {
        b.textSize = size
        return this
    }

    override fun textColor(color: Int): KButton {
        b.setTextColor(color)
        return this
    }
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
