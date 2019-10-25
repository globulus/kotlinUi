package net.globulus.kotlinui.widgets

import android.content.Context
import android.widget.CheckBox
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bind
import net.globulus.kotlinui.traits.Checkable
import net.globulus.kotlinui.traits.OnCheckedChangeListener
import net.globulus.kotlinui.traits.TextContainer
import kotlin.reflect.KMutableProperty

class KCheckBox(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        state: Boolean = false,
        @StyleRes style: Int = android.R.style.Widget_CompoundButton_CheckBox
) : KView<CheckBox>(context), TextContainer<KCheckBox>, Checkable<KCheckBox> {

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    override val view = CheckBox(context, null, 0, style).apply {
        if (resId == 0) {
            this.text = text
        } else {
            setText(resId)
        }
        isChecked = state
        setOnCheckedChangeListener { compoundButton, isChecked ->
            onCheckedChangeListener?.invoke(compoundButton, isChecked)
            notifyWriteProperties(isChecked)
        }
    }

    override var isChecked: Boolean
        get() = view.isChecked
        set(value) {
            view.isChecked = value
        }

    override fun <R> updateValue(r: R) {
        when (r) {
            is String -> text(r)
            is Boolean -> isChecked = r
            else -> throw BIND_EXCEPTION
        }
    }

    override fun text(text: String?): KCheckBox {
        view.text = text
        return this
    }

    override fun text(resId: Int): KCheckBox {
        view.setText(resId)
        return this
    }

    override fun textSize(size: Float): KCheckBox {
        view.textSize = size
        return this
    }

    override fun textColor(color: Int): KCheckBox {
        view.setTextColor(color)
        return this
    }

    override fun onCheckedChangeListener(l: OnCheckedChangeListener?): KCheckBox {
        onCheckedChangeListener = l
        return this
    }

    companion object {
        val BIND_EXCEPTION = IllegalArgumentException("KCheckBox must be bound to either a Boolean or String prop!")
    }
}

fun <T: KView<*>> T.checkBox(@StringRes resId: Int,
                             state: Boolean = false,
                             l: OnCheckedChangeListener? = null): KCheckBox {
    return add(KCheckBox(context, resId, null, state).onCheckedChangeListener(l))
}

fun <T: KView<*>> T.checkBox(text: String?,
                             state: Boolean = false,
                             l: OnCheckedChangeListener? = null): KCheckBox {
    return add(KCheckBox(context, 0, text, state).onCheckedChangeListener(l))
}

fun <T: KView<*>> T.checkBox(prop: KMutableProperty<Boolean>): KCheckBox {
    return add(KCheckBox(context, 0, null, prop.getter.call())).bind(prop)
}

fun <T: KView<*>> T.checkBox(@StringRes resId: Int, prop: KMutableProperty<Boolean>): KCheckBox {
    return add(KCheckBox(context, resId, null, prop.getter.call())).bind(prop)
}

fun <T: KView<*>> T.checkBox(text: String, prop: KMutableProperty<Boolean>): KCheckBox {
    return add(KCheckBox(context, 0, text, prop.getter.call())).bind(prop)
}
