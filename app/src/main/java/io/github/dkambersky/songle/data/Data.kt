package io.github.dkambersky.songle.data

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

/**
 * Defines a single Placemark on the map
 */
data class Placemark(val name: String, val description: String, val style: Style, val loc: LatLng) : Serializable


/**
 * Defines a style for placemarks
 */
data class Style(val id: String = "", val iconScale: Float = -1f, val icon: Bitmap? = null) : Serializable


/**
 *  Defines a song in the Songle database
 */
data class Song(val num: Short, val artist: String, val title: String, val link: String) : Serializable {
    fun id(): String = if (num < 10) "0" + num else num.toString()
}

/**
 * Carries application data for easy passing around
 */
data class SongleContext(val songs: MutableList<Song>,
                         val maps: MutableMap<Short, MutableMap<Short, List<Placemark>>>,
                         val styles: MutableMap<String, Style>,
                         @Transient val context: Context,
                         var clearedSongs: MutableSet<Song>,
                         var root: String) : Serializable