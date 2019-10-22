package net.globulus.kotlinui.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.R
import net.globulus.kotlinui.bindTo
import kotlin.reflect.KProperty

typealias ListRenderer<T> = KView<RecyclerView>.(T) -> KView<*>
typealias RowProducer<T> = () -> KList.Row<T>

class KList<T>(context: Context) : KView<RecyclerView>(context) {

    constructor(context: Context, data: List<T>, renderer: ListRenderer<T>) : this(context) {
        view.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(LayoutInflater.from(context)
                        .inflate(R.layout.item_list, parent, false))
            }

            override fun getItemCount(): Int {
                return data.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                with(holder.itemView as ViewGroup) {
                    removeAllViews()
                    val view = object : KView<View>(context) {
                        override val view = renderer(data[position]).view
                    }.view
//                    (view.parent as? ViewGroup)?.removeView(view)
                    addView(view)
                }
            }
        }
    }

    constructor(context: Context, data: List<T>, rowProducer: RowProducer<T>) : this(context) {
        view.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val row = rowProducer()
                return ViewHolder(row.kview.view, row)
            }

            override fun getItemCount(): Int {
                return data.size
            }

            @Suppress("UNCHECKED_CAST")
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                (holder.args[0] as Row<T>).bind(data[position])
                holder.itemView.forceLayout()
            }
        }
    }

    override val view = RecyclerView(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutManager = LinearLayoutManager(context)
    }

    override fun <R> updateValue(r: R) {
        view.adapter?.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, internal vararg val args: Any) : RecyclerView.ViewHolder(itemView)

    abstract class Row<D> {
       abstract val kview: KView<*>
       abstract fun bind(data: D)
    }
}

fun <T: KView<*>, D> T.list(data: List<D>, renderer: ListRenderer<D>): KList<D> {
    return add(KList(context, data, renderer))
}

fun <T: KView<*>, D> T.list(prop: KProperty<List<D>>, renderer: ListRenderer<D>): KList<D> {
    return add(KList(context, prop.getter.call(), renderer)).bindTo(prop)
}

fun <T: KView<*>, D> T.recycledList(data: List<D>, rowProducer: RowProducer<D>): KList<D> {
    return add(KList(context, data, rowProducer))
}

fun <T: KView<*>, D> T.recycledList(prop: KProperty<List<D>>, rowProducer: RowProducer<D>): KList<D> {
    return add(KList(context, prop.getter.call(), rowProducer)).bindTo(prop)
}
