package io.github.dkambersky.songle.storage

import android.content.Context

/**
 * Defines a single Placemark on the map
 */
data class Placemark(val name: String, val description: String, val styleUrl: String, val loc: Point2D)

/**
 * Defines coordinates in the map
 */
data class Point2D(val x: Float, val y: Float)


/**
 * Defines a style for placemarks
 */
data class Style(val id: String, val iconScale: Float, val icon: String)

/**
 *  Defines a song in the Songle database
 */
data class Song(val num: Short, val artist: String, val title:String, val link: String)

/**
 * Carries application context data for easy passing around
 */
data class SongleContext(val songs: MutableList<Song>,
                         val places: MutableList<Placemark>,
                         val context: Context)