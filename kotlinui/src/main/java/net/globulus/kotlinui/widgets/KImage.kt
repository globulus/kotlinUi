package net.globulus.kotlinui.widgets

import android.content.Context
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

    var imageRes: Int = resId
        set(value) {
            field = value
            view.setImageResource(value)
        }
}

fun <T: KView<*>> T.image(@DrawableRes resId: Int): KImage {
    return add(KImage(context, resId))
}
