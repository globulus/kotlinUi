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
import net.globulus.kotlinui.processor.util.FrameworkUtil

abstract class KView(val context: Context) {

    abstract val view: View

    protected open fun addView(v: View) { }

    fun <V: View> add(v: V): V {
        addView(v)
        return v
    }

    fun <V: KView> add(v: V): V {
        addView(v.view)
        return v
    }

    fun text(@StringRes resId: Int): TextView {
        return add(TextView(context).apply {
            setText(resId)
        })
    }

    fun button(@StringRes resId: Int, l: ((View) -> Unit)?): Button {
        return add(Button(context).apply {
            setText(resId)
            setOnClickListener(l)
        })
    }

    fun image(@DrawableRes resId: Int): ImageView {
        return add(ImageView(context).apply {
            setImageResource(resId)
        })
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

    fun triggerObserver(s: String) {
        Log.e("AAAA", "Triggered observer $s")
    }
}

fun kview(context: Context, block: KView.() -> KView): View {
    return object : KView(context) {
        override val view: View
            get() = block().view
    }.view
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T: KView> T.bind(): T {
    return Class.forName(T::class.java.name + FrameworkUtil.BOUND_SUFFIX)
            .getConstructor(Context::class.java, T::class.java)
            .newInstance(this.context, this) as T
//    return Proxy.newProxyInstance(this::class.java.classLoader,
//            arrayOf(this::class.java),
//            StateInvocationHandler(this)
//    ) as T

//    val enhancer = Enhancer(context)
//    enhancer.setSuperclass(KView::class.java)
//
//    enhancer.setInterceptor(object : MethodInterceptor {
//        override fun intercept(proxy: Any?, args: Array<out Any>?, methodProxy: MethodProxy?): Any? {
//            val instance = this@bind
//            val method = methodProxy?.originalMethod
//            val annotation = method?.getAnnotation(State::class.java)
//            val result = method?.invoke(this, args)
//            annotation?.let {
//                instance.triggerObserver(method.name)
//            }
//            return if (result == instance)
//                proxy
//            else
//                result
//        }
//    })
//    return enhancer.create() as T
    return this
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
