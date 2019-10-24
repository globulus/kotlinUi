package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import net.globulus.kotlinui.*
import net.globulus.kotlinui.widgets.*

class LandmarkRow(context: Context, private val landmark: Landmark) : KView<View>(context) {
    override val view = row {
            image(landmark.getImageResId(context))
                    .frame(150, 150)
            text(landmark.name)
            space()
            if (landmark.isFavorite) {
                image(android.R.drawable.star_big_on)
            }
        }.padding(5)
                .view
}

class LandmarkRecyclableRow(private val context: Context) : KList.Row<Landmark>() {

    private lateinit var image: KImage
    private lateinit var title: KText
    private lateinit var favorite: KImage

    override val kview = kview_<LinearLayout>(context) {
            row {
                image = image(0).frame(150, 150)
                title = text("")
                space()
                favorite = image(android.R.drawable.star_big_on)
            }.padding(5)
        }

    override fun bind(data: Landmark) {
        image.image(data.getImageResId(context))
        title.text(data.name)
        favorite.visible(data.isFavorite)
    }
}
