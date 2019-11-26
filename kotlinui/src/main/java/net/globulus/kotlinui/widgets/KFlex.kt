package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import net.globulus.kotlinui.KView

class KFlex(context: Context) : KView<ConstraintLayout>(context) {

  override val view: ConstraintLayout = ConstraintLayout(context).apply {
    id = ViewCompat.generateViewId()
  }

  override fun addView(v: View) {
    view.addView(v)
  }

  fun constrain(vararg blocks: ConstraintSetBlock) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(view)
    for (block in blocks) {
      block(constraintSet)
    }
    constraintSet.applyTo(view)
  }
}

fun <T: KView<*>> T.flex(block: KFlex.() -> Unit): KFlex {
  return add(KFlex(context).apply(block))
}

private typealias ConstraintSetBlock = (ConstraintSet) -> Unit
private typealias KViewMarginPair = Pair<KView<*>, Int>

infix fun <T: View> KView<T>.sits(margin: Int) = this to margin

infix fun <T: View> KView<T>.toLeftOf(second: KView<*>) = this sits 0 toLeftOf second
infix fun KViewMarginPair.toLeftOf(second: KView<*>) = this.first.connect(second, ConstraintSet.END, ConstraintSet.START, this.second)

infix fun <T: View> KView<T>.toRightOf(second: KView<*>) = this sits 0 toRightOf second
infix fun KViewMarginPair.toRightOf(second: KView<*>) = this.first.connect(second, ConstraintSet.START, ConstraintSet.END, this.second)

infix fun <T: View> KView<T>.centerIn(parent: KFlex) = listOf<ConstraintSetBlock>(
    connect(parent, ConstraintSet.TOP, ConstraintSet.TOP),
    connect(parent, ConstraintSet.BOTTOM, ConstraintSet.BOTTOM),
    connect(parent, ConstraintSet.LEFT, ConstraintSet.LEFT),
    connect(parent, ConstraintSet.RIGHT, ConstraintSet.RIGHT)
).toTypedArray()

private fun <T: View> KView<T>.connect(second: KView<*>,
                                       startSide: Int,
                                       endSide: Int,
                                       margin: Int = 0
): ConstraintSetBlock = {
  it.connect(this.view.id, startSide, second.view.id, endSide, margin)
}
