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

class KList<T>(context: Context, data: List<T>, renderer: KView.(T) -> KView) : KView(context) {

    private val rv = RecyclerView(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutManager = LinearLayoutManager(context)
        adapter = object : RecyclerView.Adapter<ViewHolder>() {

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
                    val view = object : KView(context) {
                        override val view: View
                            get() = renderer(data[position]).view
                    }.view
//                    (view.parent as? ViewGroup)?.removeView(view)
                    addView(view)
                }
            }
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override val view: View
        get() = rv

    override fun <R> update(r: R) {
        rv.adapter?.notifyDataSetChanged()
    }
}

fun <T: KView, D> T.list(data: List<D>, renderer: KView.(D) -> KView): KList<D> {
    return add(KList(context, data, renderer))
}

fun <T: KView, D> T.list(prop: KProperty<List<D>>, renderer: KView.(D) -> KView): KList<D> {
    return add(KList(context, prop.getter.call(), renderer)).bindTo(prop)
}
