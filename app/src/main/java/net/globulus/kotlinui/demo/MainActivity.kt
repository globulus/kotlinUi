package net.globulus.kotlinui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val kv = Kv(this)
        root.addView(kv.view
//        kview(this) {
//            column {
//                text(R.string.label_1)
//                button(R.string.button_1) {
//                    Toast.makeText(this@MainActivity, getString(R.string.button_1), Toast.LENGTH_LONG).show()
//                }
//            }
//        }
        )
        kv.buttonTitle = "Button title"
    }
}
