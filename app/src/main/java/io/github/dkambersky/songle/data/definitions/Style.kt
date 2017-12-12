package io.github.dkambersky.songle.data.definitions

import android.graphics.Bitmap
import java.io.Serializable

/**
 * Defines a style for placemarks
 */
data class Style(val id: String = "", val iconScale: Float = -1f, val icon: Bitmap? = null) : Serializable
