package net.globulus.kotlinui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import net.globulus.kotlinui.KView

class KImage(
        context: Context,
        @DrawableRes resId: Int,
        @StyleRes style: Int = 0
) : KView<ImageView>(context) {

    override val view = ImageView(context, null,0, style).apply {
        setImageResource(resId)
    }

    fun image(resId: Int): KImage {
        view.setImageResource(resId)
        return this
    }

    override fun <R> updateValue(r: R) {
        when (r) {
            is Int -> image(r)
            is Drawable -> view.setImageDrawable(r)
            else -> throw IllegalArgumentException("KImage must be bound to either an Int or Drawable prop!")
        }
    }
}

fun <T: KView<*>> T.image(@DrawableRes resId: Int = 0): KImage {
    return add(KImage(context, resId))
}
