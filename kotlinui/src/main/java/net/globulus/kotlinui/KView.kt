package net.globulus.kotlinui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.reflect.KProperty

abstract class KView(val context: Context) {

    val observables = mutableMapOf<String, Observable<*>>()
    val observers = mutableMapOf<String, MutableList<Consumer<*>>>()

    abstract val view: View

    lateinit var id: String
        private set
    protected var viewCounter = 0
    protected val viewMap = mutableMapOf<String, KView>()
    var parent: KView? = null
        protected set
    val superParent: KView?
        get() {
            var p = parent
            while (p?.parent != null) {
                p = p.parent
            }
            return p
        }

    protected open fun addView(v: View) { }

    fun <V: View> add(v: V): V {
        addView(v)
        return v
    }

    fun <V: KView> add(v: V): V {
        addView(v.view)
        v.parent = this
        v.id = v::class.java.simpleName + viewCounter
        viewCounter += v.viewCounter + 1
        viewMap[v.id] = v
        viewMap.putAll(v.viewMap)
        return v
    }

    fun text(@StringRes resId: Int): Text {
        return add(Text(context, resId))
    }

    fun text(text: String? = null): Text {
        return add(Text(context, 0, text))
    }

    fun button(@StringRes resId: Int, l: ((View) -> Unit)?): KButton {
        return add(KButton(context, resId, l))
    }

    fun image(@DrawableRes resId: Int): Image {
        return add(Image(context, resId))
    }

    fun column(block: Column.() -> Unit): Column {
        return add(Column(context).apply {
            block()
        })
    }

    fun row(block: Row.() -> Unit): Row {
        return add(Row(context).apply {
            block()
        })
    }

    fun <T> list(data: List<T>, renderer: KView.(T) -> KView): KList<T> {
        return add(KList(context, data, renderer))
    }

    fun <T> list(prop: KProperty<List<T>>, renderer: KView.(T) -> KView): KList<T> {
        return add(KList(context, prop.getter.call(), renderer)).bindTo(prop)
    }

    fun <T> triggerObserver(key: String, value: T) {
        Log.e("AAAA", "Triggered observer $key with $value")
        observables[key]?.let { observable ->
            val bs = observable.behaviorSubject as BehaviorSubject<T>
            if (!observable.bound) {
                observable.bound = true
                bindChildren()
                observers[key]?.let { observers ->
                    for (c in observers) {
                        bs.subscribe(c as Consumer<T>)
                    }
                }
            }
            bs.onNext(value)
        }
    }

    fun bindChildren(kviews: Collection<KView>? = null) {
        val children = kviews ?: viewMap.values
        for (child in children) {
            observers.putAll(child.observers)
            bindChildren(child.viewMap.values)
        }
    }

    open fun <R> update(r: R) {
        Log.e("AAAA", "UPDATE on $this with $r")
    }

    data class Observable<T>(
            val behaviorSubject: BehaviorSubject<T>,
            var bound: Boolean = false
    )
}

fun kview(context: Context, block: KView.() -> KView): View {
    return object : KView(context) {
        override val view: View
            get() = block().view
    }.view
}

//@Suppress("UNCHECKED_CAST")
//inline fun <reified T: KView> T.bound(): T {
//    return Class.forName(T::class.java.name + FrameworkUtil.BOUND_SUFFIX)
//            .getConstructor(Context::class.java, T::class.java)
//            .newInstance(this.context, this) as T
//}

fun <P: KView, T: KView, R> T.bindTo(parent: P, field: KProperty<R>): T {
    val name = field.name
    with(parent) {
        if (observers[name] == null) {
            observers[name] = mutableListOf()
        }
        observers[name]?.add(Consumer<R> {
            update(it)
        })
    }
    return this
}

fun <T: KView, R> T.bindTo(field: KProperty<R>): T {
    return bindTo(this, field)
}

fun <R: KView, T: Any> R.state() = NonNullState<R, T>(this)
fun <R: KView, T> R.optionalState() = NullableState<R, T>(this)
fun <D, R: KView, T: MutableList<D>> R.stateList(field: T, property: KProperty<*>)
        = StateList(this, field, property)

class Text(context: Context, @StringRes resId: Int, text: String? = null) : KView(context) {
    private val tv = TextView(context).apply {
        if (resId != 0) {
            setText(resId)
        } else {
            this.text = text
        }
    }

    override val view: View
        get() = tv

    override fun <R> update(r: R) {
        tv.text = r.toString()
    }
}

class KButton(context: Context, @StringRes resId: Int, l: ((View) -> Unit)?) : KView(context) {
    private val b = Button(context).apply {
        setText(resId)
        setOnClickListener(l)
    }

    override val view: View
        get() = b
}

class Image(context: Context, @DrawableRes resId: Int) : KView(context) {
    private val i = ImageView(context).apply {
        setImageResource(resId)
    }

    override val view: View
        get() = i
}

open class KLinearLayout(
        context: Context,
        o: Int
) : KView(context) {
    private val ll = LinearLayout(context).apply {
        orientation = o
    }

    override val view: View
        get() = ll

    override fun addView(v: View) {
        ll.addView(v)
    }
}

class Column(context: Context) : KLinearLayout(context, LinearLayout.VERTICAL)

class Row(context: Context) : KLinearLayout(context, LinearLayout.HORIZONTAL)

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
