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

  fun constrain(vararg blocks: ConstraintSetBlock): KFlex {
    val constraintSet = ConstraintSet()
    constraintSet.clone(view)
    for (block in blocks) {
      block(constraintSet)
    }
    constraintSet.applyTo(view)
    return this
  }
}

fun <T: KView<*>> T.flex(block: KFlex.() -> Unit): KFlex {
  return add(KFlex(context).apply(block))
}

private typealias ConstraintSetBlock = (ConstraintSet) -> Unit
private typealias KViewIntPair = Pair<KView<*>?, Int>

val KView<*>.end: KViewIntPair get() = this to ConstraintSet.END
val KView<*>.start: KViewIntPair get() = this to ConstraintSet.START
val KView<*>.left: KViewIntPair get() = this to ConstraintSet.LEFT
val KView<*>.right: KViewIntPair get() = this to ConstraintSet.RIGHT
val KView<*>.top: KViewIntPair get() = this to ConstraintSet.TOP
val KView<*>.bottom: KViewIntPair get() = this to ConstraintSet.BOTTOM
val KView<*>.baseline: KViewIntPair get() = this to ConstraintSet.BASELINE

infix fun <T: View> KView<T>?.sits(margin: Int): KViewIntPair = this to margin
infix fun KViewIntPair?.sits(margin: Int) = this to margin

infix fun KViewIntPair.alignsWith(other: KViewIntPair?) = this.first.connect(other?.first, this.second, other?.second)
infix fun Pair<KViewIntPair?, Int>.from(other: KViewIntPair?) = this.first?.first.connect(other?.first, this.first?.second, other?.second, this.second)

infix fun <T: View> KView<T>?.toLeftOf(second: KView<*>?) = this sits 0 toLeftOf second
infix fun KViewIntPair.toLeftOf(second: KView<*>?) = this.first?.end sits this.second from second?.start

infix fun <T: View> KView<T>?.toRightOf(second: KView<*>?) = this sits 0 toRightOf second
infix fun KViewIntPair.toRightOf(second: KView<*>?) = this.first?.start sits this.second from second?.end

infix fun <T: View> KView<T>?.centerIn(parent: KFlex) = listOf<ConstraintSetBlock>(
    connect(parent, ConstraintSet.TOP, ConstraintSet.TOP),
    connect(parent, ConstraintSet.BOTTOM, ConstraintSet.BOTTOM),
    connect(parent, ConstraintSet.LEFT, ConstraintSet.LEFT),
    connect(parent, ConstraintSet.RIGHT, ConstraintSet.RIGHT)
).toTypedArray()

private fun <T: View> KView<T>?.connect(second: KView<*>?,
                                       startSide: Int?,
                                       endSide: Int?,
                                       margin: Int = 0
): ConstraintSetBlock = {
  if (this != null && second != null && startSide != null && endSide != null) {
    it.connect(this.view.id, startSide, second.view.id, endSide, margin)
  }
}
