package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import net.globulus.kotlinui.KView

class KImage(context: Context, @DrawableRes resId: Int) : KView(context) {

    private val i = ImageView(context).apply {
        setImageResource(resId)
    }

    override val view: View
        get() = i

    var imageRes: Int = resId
        set(value) {
            field = value
            i.setImageResource(value)
        }
}

fun <T: KView> T.image(@DrawableRes resId: Int): KImage {
    return add(KImage(context, resId))
}
