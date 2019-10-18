package net.globulus.kotlinui.demolib

import android.util.Log
import net.globulus.kotlinui.annotation.FlavorInject

class FtueManagerImpl : FtueManager {
    @FlavorInject
    override fun signup(email: String, password: String, callback: Callback?) {
        Log.e(this::class.java.simpleName, "FtueManagerImpl called with $email $password")
    }
}
