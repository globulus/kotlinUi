package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.KViewBox
import net.globulus.kotlinui.R
import net.globulus.kotlinui.root
import net.globulus.kotlinui.traits.FabContainer
import net.globulus.kotlinui.traits.RootContainer

class KToolbarColumn(
    context: Context,
    gravity: Int,
    invokeBlockNow: Boolean,
    private val block: KToolbarColumn.() -> Unit
) : KView<CoordinatorLayout>(context), RootContainer, FabContainer {

  val appBarLayout: AppBarLayout
  val toolbar: Toolbar
  private val linearLayout: LinearLayout
  override val floatingActionButton: FloatingActionButton

  override val view = (LayoutInflater.from(context).inflate(R.layout.layout_toolbar_linear, null) as CoordinatorLayout).apply {
    appBarLayout = findViewById(R.id.appBarLayout)
    toolbar = findViewById(R.id.toolbar)
    linearLayout = findViewById<LinearLayout>(R.id.linearLayout).apply {
      this.gravity = gravity
    }
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
    linearLayout.addView(v)
  }

  override fun <R> updateValue(r: R) {
    removeAllChildren()
    linearLayout.removeAllViews()
    invokeBlock()
  }
}

fun <T: KView<*>> T.toolbarColumn(block: KToolbarColumn.() -> Unit): KToolbarColumn {
  return toolbarColumn(Gravity.NO_GRAVITY, block)
}

fun <T: KView<*>> T.toolbarColumn(gravity: Int, block: KToolbarColumn.() -> Unit): KToolbarColumn {
  return add(KToolbarColumn(context, gravity, true, block))
}

fun <T: KViewBox> T.rootToolbarColumn(block: KToolbarColumn.() -> Unit) = rootToolbarColumn(Gravity.NO_GRAVITY, block)

fun <T: KViewBox> T.rootToolbarColumn(gravity: Int, block: KToolbarColumn.() -> Unit)
    = root(KToolbarColumn(context, gravity, false, block))
