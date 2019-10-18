package net.globulus.kotlinui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.globulus.kotlinui.bind
import net.globulus.kotlinui.kview

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root.addView(
        kview(this) {
            column {
                text(R.string.label_1)
                button(R.string.button_1) {
                    Toast.makeText(this@MainActivity, getString(R.string.button_1), Toast.LENGTH_LONG).show()
                }
            }
        }
        )

        val kv = Kv(this).bind()
        kv.buttonTitle = "Button title"
    }
}
