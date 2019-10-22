package net.globulus.kotlinui.traits

import android.widget.CompoundButton
import net.globulus.kotlinui.KView

typealias OnCheckedChangeListener = (CompoundButton, Boolean) -> Unit

interface Checkable<T: KView<*>> {
    var isChecked: Boolean
    fun onCheckedChangeListener(l: OnCheckedChangeListener?): T
}
