package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import android.view.View
import net.globulus.kotlinui.*
import net.globulus.kotlinui.demo.R
import net.globulus.kotlinui.widgets.checkBox
import net.globulus.kotlinui.widgets.column
import net.globulus.kotlinui.widgets.list

class LandmarkList(context: Context, private val data: List<Landmark>) : KView(context) {
    
    var showFavorites: Boolean by state(false)

    override val view: View
        get() = kview.view

    private val kview get() =
        column {
            checkBox(R.string.show_favorites)
                    .bind(::showFavorites)
                    .margins(5, 10, 5, 10)
            list(data) {
                if (showFavorites && !it.isFavorite) {
                    emptyView()
                } else {
                    LandmarkRow(context, it)
                }
            }
//              recycledList(data) {
//                  LandmarkRecyclableRow(context)
//              }
                    .bindTo(::showFavorites)
        }
}
