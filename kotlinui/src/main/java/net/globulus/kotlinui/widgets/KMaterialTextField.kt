package net.globulus.kotlinui.widgets

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import net.globulus.kotlinui.KView

private typealias KMaterialTextFieldBlock = KMaterialTextField.() -> KTextField

class KMaterialTextField(
    context: Context,
    block: KMaterialTextFieldBlock
) : KView<TextInputLayout>(context) {

  val textField: KTextField = block()

  override val view = TextInputLayout(context).apply {
    addView(textField.view)
  }

  fun hint(@StringRes resId: Int): KMaterialTextField {
    view.hint = if (resId == 0) null else context.getString(resId)
    return this
  }

  fun error(@StringRes resId: Int): KMaterialTextField {
    view.error = if (resId == 0) null else context.getString(resId)
    return this
  }
}

fun <T : KView<*>> T.materialTextField(block: KMaterialTextFieldBlock): KMaterialTextField {
  return add(KMaterialTextField(context, block))
}
