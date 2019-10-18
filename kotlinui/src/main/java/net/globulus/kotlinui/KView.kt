package net.globulus.kotlinui

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.reflect.KProperty

abstract class KView(val context: Context) {

    val observables = mutableMapOf<String, BehaviorSubject<*>>()
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

    fun <T> triggerObserver(key: String, value: T) {
        Log.e("AAAA", "Triggered observer $key with $value")
        (observables[key] as? BehaviorSubject<T>)?.let { observable ->
            observers[key]?.let { observers ->
                for (c in observers) {
                    observable.subscribe(c as Consumer<T>)
                }
            }
            observable.onNext(value)
        }
    }

    fun <R> update(r: R) {
        Log.e("AAAA", "UPDATE $r")
    }
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
    return bindTo(superParent ?: this, field)
}

fun <R: KView, T> R.state() = State<R, T>(this)

class Text(context: Context, @StringRes resId: Int) : KView(context) {
    private val tv = TextView(context).apply {
        setText(resId)
    }

    override val view: View
        get() = tv
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

class KList<T>(context: Context, data: List<T>, renderer: (T) -> KView) {

    private val rv = RecyclerView(context).apply {
//        adapter = object : RecyclerView.Adapter<ViewHolder>() {
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//                return ViewHolder(ViewStub(context))
//            }
//
//            override fun getItemCount(): Int {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private fun bind(item: T) {
//            itemView =
        }
    }
}
