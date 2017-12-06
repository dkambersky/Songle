package io.github.dkambersky.songle.data

import android.content.Context
import java.io.Serializable

/**
 * Defines a single Placemark on the map
 */
data class Placemark(val name: String, val description: String, val style: Style, val loc: Point2D) : Serializable

/**
 * Defines coordinates in the map
 */
data class Point2D(val x: Float, val y: Float) : Serializable


/**
 * Defines a style for placemarks
 */
data class Style(val id: String = "", val iconScale: Float = -1f, val icon: String = "") : Serializable


/**
 *  Defines a song in the Songle database
 */
data class Song(val num: Short, val artist: String, val title: String, val link: String) : Serializable

/**
 * Carries application data for easy passing around
 */
data class SongleContext(val songs: MutableList<Song>,
                         val maps: MutableMap<Short, List<Placemark>>,
                         val styles: MutableMap<String, Style>,
                         @Transient val context: Context,
                         var clearedSongs: MutableSet<Song>,
                         var root: String) : Serializable