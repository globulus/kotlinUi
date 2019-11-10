package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.GridView
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.R
import net.globulus.kotlinui.bindTo
import kotlin.reflect.KProperty

typealias GridRenderer<D> = KView<GridView>.(D) -> KView<*>

class KGrid<D>(
    context: Context,
    private val data: List<D>,
    renderer: GridRenderer<D>
) : KView<GridView>(context) {

    override val view = GridView(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        numColumns = GridView.AUTO_FIT
        stretchMode = GridView.STRETCH_SPACING_UNIFORM
        adapter = object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val cv = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_list, parent, false).apply {
                    tag = ViewHolder(this as FrameLayout)
                }

                val viewHolder = cv.tag as ViewHolder
                with (viewHolder.view) {
                    removeAllViews()
                    val view = object : KView<View>(context) {
                        override val view = renderer(data[position]).view
                    }.view
                    addView(view)
                }

                return cv
            }

            override fun getItem(position: Int): Any? {
                return null
            }

            override fun getItemId(position: Int): Long {
                return 0L
            }

            override fun getCount(): Int {
                return data.size
            }
        }
    }

    override fun <R> updateValue(r: R) {
        (view.adapter as? BaseAdapter)?.notifyDataSetChanged()
    }

    private class ViewHolder(val view: FrameLayout)
}

fun <T: KView<*>, D> T.grid(data: List<D>, renderer: GridRenderer<D>): KGrid<D> {
    return add(KGrid(context, data, renderer))
}

fun <T: KView<*>, D> T.grid(prop: KProperty<List<D>>, renderer: GridRenderer<D>): KGrid<D> {
    return add(KGrid(context, prop.getter.call(), renderer)).bindTo(prop)
}
