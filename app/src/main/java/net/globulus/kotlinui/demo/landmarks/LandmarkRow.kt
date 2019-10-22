package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import android.view.View
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.frame
import net.globulus.kotlinui.padding
import net.globulus.kotlinui.widgets.image
import net.globulus.kotlinui.widgets.row
import net.globulus.kotlinui.widgets.space
import net.globulus.kotlinui.widgets.text

class LandmarkRow(context: Context, private val landmark: Landmark) : KView(context) {

    override val view: View
        get() = kview.view

    private val kview get() =
        row {
            image(landmark.getImageResId(context))
                    .frame(150, 150)
            text(landmark.name)
            space()
            if (landmark.isFavorite) {
                image(android.R.drawable.star_big_on)
            }
        }.padding(5)
}
