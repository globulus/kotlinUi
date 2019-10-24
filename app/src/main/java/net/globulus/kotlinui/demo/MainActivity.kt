package net.globulus.kotlinui.demo

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.globulus.kotlinui.demo.landmarks.Landmark
import net.globulus.kotlinui.demo.landmarks.LandmarkList
import net.globulus.kotlinui.setContentView

class MainActivity : AppCompatActivity() {

    lateinit var list: LandmarkList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        val landmarks: List<Landmark> = Gson().fromJson(resources.openRawResource(R.raw.landmark_data)
                .bufferedReader().use { it.readText() }, object : TypeToken<List<Landmark>>() { }.type)

        val list = LandmarkList(this, landmarks)
        setContentView(list)

        Handler().postDelayed({
            list.landmarks.removeIf { it.isFavorite }
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
}
