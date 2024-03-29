package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.Button
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.OnClickListener
import net.globulus.kotlinui.StatefulProducer
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty

private const val DEFAULT_STYLE = 0

class KButton(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        @StyleRes style: Int = DEFAULT_STYLE,
        l: OnClickListener<Button>?
) : KView<Button>(context), TextContainer<KButton> {

    override val view = (if (style == 0)
        Button(context)
    else
        Button(context, null, 0, style)
    ).apply {
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

    override fun textRes(resId: Int): KButton {
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

fun <T: KView<*>> T.button(@StringRes resId: Int, l: OnClickListener<Button>? = null): KButton {
    return add(KButton(context, resId, null, 0, l))
}

fun <T: KView<*>> T. button(text: String? = null, l: OnClickListener<Button>? = null): KButton {
    return add(KButton(context, 0, text, 0, l))
}

fun <T: KView<*>> T. button(prop: KProperty<String>, l: OnClickListener<Button>? = null): KButton {
    return button(prop.getter.call(), l).bindTo(prop)
}

fun <P: StatefulProducer, T: KView<*>> T. button(root: P,
                                                 prop: KProperty<String>,
                                                 l: OnClickListener<Button>? = null): KButton {
    return button(prop.getter.call(), l).bindTo(root, prop)
}
