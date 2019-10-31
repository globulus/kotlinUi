package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.StatefulProducer
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KProperty


private typealias KTextBlock = (KText.() -> Unit)?
private const val DEFAULT_STYLE = 0

class KText(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        @StyleRes style: Int = 0,
        block: KTextBlock = null
) : KView<TextView>(context), TextContainer<KText> {

    override val view = TextView(context, null, 0, style).apply {
        if (resId != 0) {
            setText(resId)
        } else {
            this.text = text
        }
    }

    init {
        block?.invoke(this)
    }

    override fun <R> updateValue(r: R) {
        view.text = r.toString()
    }

    override fun text(text: String?): KText {
        view.text = text
        return this
    }

    override fun textRes(resId: Int): KText {
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

fun <T: KView<*>> T.text(@StringRes resId: Int, block: KTextBlock = null): KText {
    return add(KText(context, resId, null, DEFAULT_STYLE, block))
}

fun <T: KView<*>> T.text(text: String? = null, block: KTextBlock = null): KText {
    return add(KText(context, 0, text, DEFAULT_STYLE, block))
}

fun <T: KView<*>> T.text(prop: KProperty<String>, block: KTextBlock = null): KText {
    return text(prop.getter.call(), block).bindTo(prop)
}

fun <P: StatefulProducer, T: KView<*>> T.text(root: P, prop: KProperty<String>, block: KTextBlock = null): KText {
    return text(prop.getter.call(), block).bindTo(root, prop)
}
