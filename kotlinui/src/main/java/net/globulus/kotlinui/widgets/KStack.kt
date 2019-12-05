package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.KViewBox
import net.globulus.kotlinui.root
import net.globulus.kotlinui.traits.RootContainer

class KStack(context: Context,
             invokeBlockNow: Boolean,
             private val block: KStack.() -> Unit
) : KView<FrameLayout>(context), RootContainer {

  override val view: FrameLayout = FrameLayout(context)

  init {
    if (invokeBlockNow) {
      invokeBlock()
    }
  }

  override fun addView(v: View) {
    view.addView(v)
  }
  override fun <R> updateValue(r: R) {
    removeAllChildren()
    view.removeAllViews()
    invokeBlock()
  }

  override fun invokeBlock() {
    block()
  }
}

fun <T: KView<*>> T.stack(block: KStack.() -> Unit): KStack {
  return add(KStack(context, true, block))
}

fun <T: KViewBox> T.rootStack(block: KStack.() -> Unit) = root(KStack(context,false, block))
