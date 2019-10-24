package net.globulus.kotlinui.demo.landmarks

import android.content.Context
import net.globulus.kotlinui.*
import net.globulus.kotlinui.demo.R
import net.globulus.kotlinui.widgets.checkBox
import net.globulus.kotlinui.widgets.list
import net.globulus.kotlinui.widgets.rootColumn
import net.globulus.kotlinui.widgets.textField

class LandmarkList(context: Context, data: List<Landmark>) : KViewBox(context) {

    val landmarks = stateList(data.toMutableList(), ::landmarks)
    var showFavorites: Boolean by state(false)
    var showList: Boolean by state(true)
    var textInput: String = "Initial"

    override val root =
        rootColumn {
            textField(::textInput)
            checkBox(::showList)
                    .text("Show list")
            checkBox(::showFavorites)
                    .text(R.string.show_favorites)
                    .margins(5, 10, 5, 10)
            if (showList) {
                list(landmarks) {
                    if (showFavorites && !it.isFavorite) {
                        emptyView()
                    } else {
                        LandmarkRow(context, it)
                    }
                }.bindTo(::showFavorites, ::landmarks)
            }
        }.bindTo(::showList)
}
