package net.globulus.kotlinui.demo.landmarks

import android.content.Context

data class Landmark(
        val id: Int,
        val name: String,
        val category: String,
        val city: String,
        val state: String,
        val park: String,
        val imageName: String,
        val isFeatured: Boolean,
        val isFavorite: Boolean
)

fun Landmark.getImageResId(context: Context): Int {
    return context.resources.getIdentifier(imageName, "drawable", context.packageName)
}
