package net.globulus.kotlinui.traits

import android.annotation.SuppressLint
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface FabContainer {
  val floatingActionButton: FloatingActionButton

  @SuppressLint("RestrictedApi")
  fun fab(onClickListener: (View) -> Unit): FloatingActionButton {
    floatingActionButton.visibility = View.VISIBLE
    floatingActionButton.setOnClickListener(onClickListener)
    return floatingActionButton
  }
}
