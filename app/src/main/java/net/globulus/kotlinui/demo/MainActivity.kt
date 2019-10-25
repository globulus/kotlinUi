package net.globulus.kotlinui.demo

import android.content.Context
import android.os.Bundle
import android.os.Handler
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
        setContentView(InfixTest(this))

        Handler().postDelayed({
//            list.landmarks.removeIf { it.isFavorite }
//            list.list.visible(false)
        }, 4000)


//        list = LandmarkList(this, landmarks)
//        val kv = Kv(this)
//        root.addView(
//                list.view
////                kv.view
////        kview(this) {
////            column {
////                text(R.string.label_1)
////                button(R.string.button_1) {
////                    Toast.makeText(this@MainActivity, getString(R.string.button_1), Toast.LENGTH_LONG).show()
////                }
////            }
////        }
//        )
    }

    class InfixTest(context: Context) : KViewBox(context) {

        var input: String by state("Change me!")
        var buttonVisible: Boolean by state(true)
        var evenOnly: Boolean by state(false)

        override val root = rootColumn {
            textField(::input)
            checkBox("Button visible", ::buttonVisible)
            checkBox("Even only", ::evenOnly)
            button(::input) {
                Toast.makeText(context, "Tapped!", Toast.LENGTH_SHORT).show()
            }.bindTo(::buttonVisible updates KButton::visible)
            list(listOf(1, 2, 3, 4)) {
                if (evenOnly && it % 2 == 1) {
                    emptyView()
                } else {
                    text("$it")
                }
            }.bindTo(::evenOnly)
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
