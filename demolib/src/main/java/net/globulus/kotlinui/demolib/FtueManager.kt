package net.globulus.kotlinui.demolib

import net.globulus.kotlinui.annotation.KotlinUiConfig
import net.globulus.kotlinui.annotation.Flavorable

@KotlinUiConfig(source = true)
@Flavorable
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
