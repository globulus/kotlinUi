package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.KViewBox
import net.globulus.kotlinui.R
import net.globulus.kotlinui.root
import net.globulus.kotlinui.traits.FabContainer
import net.globulus.kotlinui.traits.RootContainer

class KToolbarFlex(
    context: Context,
    invokeBlockNow: Boolean,
    private val block: KToolbarFlex.() -> Unit
) : KView<CoordinatorLayout>(context), RootContainer, FabContainer {

  val appBarLayout: AppBarLayout
  val toolbar: Toolbar
  private val constraintLayout: ConstraintLayout
  override val floatingActionButton: FloatingActionButton

  override val view = (LayoutInflater.from(context).inflate(R.layout.layout_toolbar_constraint, null) as CoordinatorLayout).apply {
    appBarLayout = findViewById(R.id.appBarLayout)
    toolbar = findViewById(R.id.toolbar)
    constraintLayout = findViewById(R.id.constraintLayout)
    floatingActionButton = findViewById(R.id.fab)
  }

  init {
    if (invokeBlockNow) {
      invokeBlock()
    }
  }

  override fun invokeBlock() {
    block()
  }

  override fun addView(v: View) {
    constraintLayout.addView(v)
  }

  override fun <R> updateValue(r: R) {
    removeAllChildren()
    constraintLayout.removeAllViews()
    invokeBlock()
  }

  fun constrain(vararg blocks: ConstraintSetBlock): KToolbarFlex {
    KFlex.constrain(constraintLayout, *blocks)
    return this
  }
}


fun <T: KView<*>> T.toolbarFlex(block: KToolbarFlex.() -> Unit): KToolbarFlex {
  return add(KToolbarFlex(context, true, block))
}

fun <T: KViewBox> T.rootToolbarFlex(block: KToolbarFlex.() -> Unit) = root(KToolbarFlex(context, false, block))