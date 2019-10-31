package net.globulus.kotlinui.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.bind
import net.globulus.kotlinui.traits.TextContainer
import java.util.concurrent.TimeUnit
import kotlin.reflect.KMutableProperty

private typealias KTextFieldBlock = (KTextField.() -> Unit)?
private const val DEFAULT_STYLE = android.R.style.Widget_EditText

class KTextField(
        context: Context,
        @StringRes resId: Int,
        text: String? = null,
        @StyleRes style: Int = DEFAULT_STYLE,
        block: KTextFieldBlock = null
) : KView<EditText>(context), TextContainer<KTextField> {

    private val changePublishSubject = PublishRelay.create<String>().apply {
        debounce(700, TimeUnit.MILLISECONDS)
        distinctUntilChanged()
        subscribeOn(Schedulers.io())
        observeOn(AndroidSchedulers.mainThread())
        subscribe({
            notifyWriteProperties(it)
        }, {
            throw it
        })
    }

    override val view = EditText(context, null, 0, style).apply {
        if (resId != 0) {
            setText(resId)
        } else {
            setText(text)
        }
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                changePublishSubject.accept(s?.toString()?.trim() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    init {
        block?.invoke(this)
    }

    override fun text(text: String?): KTextField {
        view.setText(text)
        return this
    }

    override fun textRes(resId: Int): KTextField {
        view.setText(resId)
        return this
    }

    override fun textSize(size: Float): KTextField {
        view.textSize = size
        return this
    }

    override fun textColor(color: Int): KTextField {
        view.setTextColor(color)
        return this
    }

    override fun <R> updateValue(r: R) {
        view.setText(r.toString())
    }

    fun inputType(type: Int): KTextField {
        view.inputType = type
        return this
    }
}

fun <T: KView<*>> T.textField(@StringRes resId: Int, block: KTextFieldBlock = null): KTextField {
    return add(KTextField(context, resId, null, DEFAULT_STYLE, block))
}

fun <T: KView<*>> T.textField(text: String? = null, block: KTextFieldBlock = null): KTextField {
    return add(KTextField(context, 0, text, DEFAULT_STYLE, block))
}

fun <T: KView<*>> T.textField(prop: KMutableProperty<String>, block: KTextFieldBlock = null): KTextField {
    return textField(prop.getter.call(), block).bind(prop)
}
