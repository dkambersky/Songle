package io.github.dkambersky.songle.data.definitions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.Serializable


/**
 * Defines a single Placemark on the map
 */
data class Placemark(val lyricPos: Pair<Int, Int>,
                     val description: String,
                     val style: Style,
                     val loc: LatLng,
                     var marker: Marker? = null) : Serializable {

    fun text(lyrics: Map<Int, List<String>>): String {
        return lyrics[lyricPos.first]!![lyricPos.second - 1]
    }
}

