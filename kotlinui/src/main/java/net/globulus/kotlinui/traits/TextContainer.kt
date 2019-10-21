package net.globulus.kotlinui.traits

import androidx.annotation.StringRes
import net.globulus.kotlinui.KView

interface TextContainer<T: KView> {
    fun text(text: String?): T
    fun text(@StringRes resId: Int): T
    fun textSize(size: Float): T
    fun textColor(color: Int): T
}
