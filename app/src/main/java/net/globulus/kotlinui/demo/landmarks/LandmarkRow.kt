package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import android.view.View
import net.globulus.kotlinui.*
import net.globulus.kotlinui.widgets.*

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

class LandmarkRecyclableRow(private val context: Context) : KList.Row<Landmark>() {

    private lateinit var image: KImage
    private lateinit var title: KText
    private lateinit var favorite: KImage

    override val kview = kview_(context) {
            row {
                image = image(0).frame(150, 150)
                title = text("")
                space()
                favorite = image(android.R.drawable.star_big_on)
            }.padding(5)
        }

    override fun bind(data: Landmark) {
        image.imageRes = data.getImageResId(context)
        title.text(data.name)
        favorite.visible(data.isFavorite)
    }
}
