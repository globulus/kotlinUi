package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import net.globulus.kotlinui.*
import net.globulus.kotlinui.demo.R
import net.globulus.kotlinui.widgets.checkBox
import net.globulus.kotlinui.widgets.list
import net.globulus.kotlinui.widgets.rootColumn

class LandmarkList(context: Context, private val data: List<Landmark>) : KViewBox(context) {

    var showFavorites: Boolean by state(false)
    var showList: Boolean by state(true)

    override val root =
        rootColumn {
            checkBox("Show list", showList)
                    .bind(::showList)
            checkBox(R.string.show_favorites, showFavorites)
                    .bind(::showFavorites)
                    .margins(5, 10, 5, 10)
            if (showList) {
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
        }.bindTo(::showList)
}
