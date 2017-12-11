package io.github.dkambersky.songle.data.defs

import java.io.Serializable

/**
 *  Defines a song in the Songle database
 */
data class Song(val num: Short, val artist: String, val title: String, val link: String) : Serializable {
    fun id(): String = if (num < 10) "0" + num else num.toString()
    fun fileNames(): List<String> = (1..5).map { "${id()}-$it.map" }
    fun urls(): List<String> = (1..5).map { "${id()}/map$it.kml" }
}
