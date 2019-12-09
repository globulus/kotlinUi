package net.globulus.kotlinui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.R
import net.globulus.kotlinui.bindTo
import net.globulus.kotlinui.traits.Data
import net.globulus.kotlinui.traits.DataProducer
import kotlin.reflect.KProperty

typealias ListRenderer<D> = KView<RecyclerViewEmptySupport>.(D) -> KView<*>
typealias RowProducer<D> = () -> KList.Row<D>

class KList<D>(context: Context, private val dataProducer: DataProducer<D>) : KView<RecyclerViewEmptySupport>(context) {

    private var data = dataProducer()

    constructor(context: Context, dataProducer: DataProducer<D>, renderer: ListRenderer<D>) : this(context, dataProducer) {
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

    constructor(context: Context, dataProducer: DataProducer<D>, rowProducer: RowProducer<D>) : this(context, dataProducer) {
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
                (holder.args[0] as Row<D>).bind(data[position])
                holder.itemView.forceLayout()
            }
        }
    }

    override val view = RecyclerViewEmptySupport(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutManager = LinearLayoutManager(context)
    }

    override fun <R> updateValue(r: R) {
        data = dataProducer()
        view.adapter?.notifyDataSetChanged()
        view.emptyObserver.onChanged()
    }

    fun whenEmptyShow(kView: KView<*>) {
        view.emptyView = kView.view
        view.emptyObserver.onChanged()
    }

    class ViewHolder(itemView: View, internal vararg val args: Any) : RecyclerView.ViewHolder(itemView)

    abstract class Row<D> {
       abstract val kview: KView<*>
       abstract fun bind(data: D)
    }
}

fun <T: KView<*>, D> T.list(dataProducer: DataProducer<D>, renderer: ListRenderer<D>): KList<D> {
    return add(KList(context, dataProducer, renderer))
}

fun <T: KView<*>, D> T.list(data: Data<D>, renderer: ListRenderer<D>) = list({ data }, renderer)

fun <T: KView<*>, D> T.list(bindTo: KProperty<List<D>>, renderer: ListRenderer<D>): KList<D> {
    return add(KList(context, { bindTo.getter.call() }, renderer)).bindTo(bindTo)
}

fun <T: KView<*>, D> T.recycledList(dataProducer: DataProducer<D>, rowProducer: RowProducer<D>): KList<D> {
    return add(KList(context, dataProducer, rowProducer))
}

fun <T: KView<*>, D> T.recycledList(data: Data<D>, rowProducer: RowProducer<D>) = recycledList({ data }, rowProducer)

fun <T: KView<*>, D> T.recycledList(bindTo: KProperty<List<D>>, rowProducer: RowProducer<D>): KList<D> {
    return add(KList(context, { bindTo.getter.call() }, rowProducer)).bindTo(bindTo)
}

class RecyclerViewEmptySupport @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var emptyView: View? = null

    internal val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            val adapter = adapter
            if (adapter != null && emptyView != null) {
                if (adapter.itemCount == 0) {
                    emptyView!!.visibility = View.VISIBLE
                    this@RecyclerViewEmptySupport.visibility = View.GONE
                } else {
                    emptyView!!.visibility = View.GONE
                    this@RecyclerViewEmptySupport.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }
}
