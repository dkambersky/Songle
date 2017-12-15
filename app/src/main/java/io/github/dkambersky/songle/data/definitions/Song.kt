package io.github.dkambersky.songle.data.definitions

import java.io.Serializable

/**
 *  Defines a song in the Songle database
 */
data class Song(val num: Int,
                val artist: String,
                val title: String,
                val link: String,
                var lyrics: Map<Int, List<String>>) : Serializable {

    fun id(): String = if (num < 10) "0" + num else num.toString()
    fun fileNames(): List<String> = (1..5).map { "${id()}-$it.map" }
    fun urls(): List<String> = (1..5).map { "${id()}/map$it.kml" }
}
