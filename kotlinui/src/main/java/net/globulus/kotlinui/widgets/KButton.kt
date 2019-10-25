package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.Button
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.OnClickListener
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty

class KButton(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        @StyleRes style: Int = android.R.style.Widget_Button,
        l: OnClickListener<Button>?
) : KView<Button>(context), TextContainer<KButton> {

    override val view = Button(context, null, 0, style).apply {
        if (resId == 0) {
            this.text = text
        } else {
            setText(resId)
        }
        setOnClickListener {
            l?.invoke(this@KButton)
        }
    }

    override fun <R> updateValue(r: R) {
        view.text = r.toString()
    }

    override fun text(text: String?): KButton {
        view.text = text
        return this
    }

    override fun text(resId: Int): KButton {
        view.setText(resId)
        return this
    }

    override fun textSize(size: Float): KButton {
        view.textSize = size
        return this
    }

    override fun textColor(color: Int): KButton {
        view.setTextColor(color)
        return this
    }
}

fun <T: KView<*>> T.button(@StringRes resId: Int, l: OnClickListener<Button>?): KButton {
    return add(KButton(context, resId, null, 0, l))
}

fun <T: KView<*>> T. button(text: String?, l: OnClickListener<Button>?): KButton {
    return add(KButton(context, 0, text, 0, l))
}

fun <T: KView<*>> T. button(prop: KProperty<String>, l: OnClickListener<Button>?): KButton {
    return button(prop.getter.call(), l).bindTo(prop)
}
