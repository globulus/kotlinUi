package net.globulus.kotlinui.demo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.globulus.kotlinui.*
import net.globulus.kotlinui.demo.landmarks.Landmark
import net.globulus.kotlinui.demo.landmarks.LandmarkList
import net.globulus.kotlinui.widgets.*

class MainActivity : AppCompatActivity() {

    lateinit var list: LandmarkList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        val landmarks: List<Landmark> = Gson().fromJson(resources.openRawResource(R.raw.landmark_data)
                .bufferedReader().use { it.readText() }, object : TypeToken<List<Landmark>>() { }.type)

        val ls = landmarks + Landmark(1, "", "", ""
                , "", "", "", false, false, "HEADER")

        val list = LandmarkList(this, ls.shuffled())

//        setContentView(list)
        val statefulTest = StatefulTest()
//        setContentView(this) {
//            column {
//                add(CounterView(context, statefulTest))
//                add(InfixTest(context, statefulTest))
//            }
//        }

        tabs(R.string.app_name,
            arrayOf(R.string.button_1, R.string.label_1),
            listOf("AAAA", "BBBB")) {
                text(it)
            }

        Handler().postDelayed({
//            list.landmarks.removeIf { it.isFavorite }
//            list.list.visible(false)
        }, 4000)
    }

    class StatefulTest : StatefulProducer {
        var counter: Int by state(0)
        var input: String by state("Change me!")

        override val stateful = Stateful.default {
            Log.e("AAAAA", "Updated $this with $it")
        }
    }

    class CounterView(context: Context, statefulTest: StatefulTest) : KView<View>(context) {
        override val view = row {
            text("Counter is ${statefulTest.counter}")
            space()
            text("Input is ${statefulTest.input}")
        }.bindTo(statefulTest)
                .view
    }

    class InfixTest(context: Context, statefulTest: StatefulTest) : KViewBox(context) {

        var buttonVisible: Boolean by state(true)
        var evenOnly: Boolean by state(false)

        lateinit var chbEventOnly: KCheckBox

        override val root = rootColumn(Gravity.CENTER_HORIZONTAL) {
            textField(statefulTest::input).margins(0, 0, 0, 10)
            checkBox("Button visible", ::buttonVisible)
            chbEventOnly = checkBox("Even only", ::evenOnly)
//                    .bindTo(statefulTest, statefulTest::counter, wrap(KCheckBox::text) {
//                        "Even only counter $it"
//                    })
            button("Increment") {
                Toast.makeText(context, "Tapped!", Toast.LENGTH_SHORT).show()
                statefulTest.counter += 1
            }.widthWrapContent()
             .bindTo(::buttonVisible updates KButton::visible)
            list(listOf(1, 2, 3, 4)) {
                if (evenOnly && it % 2 == 1) {
                    emptyView()
                } else {
                    text("$it")
                }
            }.bindTo(::evenOnly)
        }.padding(10)

        init {
            statefulTest::counter of statefulTest triggers KCheckBox::text via { "Even only counter $it" } on chbEventOnly
        }
    }

//    private class InfixTest(context: Context) : KViewBox(context) {
//
//        var input: String by state("")
//        var buttonVisible: Boolean by state(true)
//        var evenOnly: Boolean by state(false)
//
////        lateinit var chbButtonVisible: KCheckBox
////        lateinit var chbEvenOnly: KCheckBox
////        lateinit var kbutton: KButton
////        lateinit var klist: KList<*>
//
//        override val root = rootColumn {
//            textField(::input)
//            ::input updates text()
//            checkBox("Button visible", buttonVisible) updates ::buttonVisible //.id(::chbButtonVisible)
//            checkBox("Even only", evenOnly) updates ::evenOnly //.id(::chbEvenOnly)
//            ::buttonVisible updates KButton::visible of button("Button") { } //.id(::kbutton)
//            ::evenOnly updates list(listOf(1, 2, 3, 4)) {
//                if (evenOnly && it % 2 == 1) {
//                    emptyView()
//                } else {
//                    text("$it")
//                }
//            } //.id(::klist)
//        }
//
////        init {
////            chbButtonVisible updates ::buttonVisible
////            ::buttonVisible updates KButton::visible of kbutton
////            chbEvenOnly updates ::evenOnly
////            ::evenOnly updates klist
////        }
//    }
}
