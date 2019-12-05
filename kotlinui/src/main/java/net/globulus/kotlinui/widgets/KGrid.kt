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
import net.globulus.kotlinui.traits.Data
import net.globulus.kotlinui.traits.DataProducer
import kotlin.reflect.KProperty

typealias GridRenderer<D> = KView<GridView>.(D) -> KView<*>

class KGrid<D>(
    context: Context,
    private val dataProducer: DataProducer<D>,
    renderer: GridRenderer<D>
) : KView<GridView>(context) {

    private var data = dataProducer()

    override val view = (LayoutInflater.from(context).inflate(R.layout.view_grid, null) as GridView).apply {
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
        data = dataProducer()
        (view.adapter as? BaseAdapter)?.notifyDataSetChanged()
    }

    private class ViewHolder(val view: FrameLayout)
}

fun <T: KView<*>, D> T.grid(dataProducer: DataProducer<D>, renderer: GridRenderer<D>): KGrid<D> {
    return add(KGrid(context, dataProducer, renderer))
}

fun <T: KView<*>, D> T.grid(data: Data<D>, renderer: GridRenderer<D>) = grid({ data }, renderer)

fun <T: KView<*>, D> T.grid(bindTo: KProperty<List<D>>, renderer: GridRenderer<D>): KGrid<D> {
    return add(KGrid(context, { bindTo.getter.call() }, renderer)).bindTo(bindTo)
}
