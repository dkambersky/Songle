package io.github.dkambersky.songle.data.definitions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.Serializable


/**
 * Defines a single Placemark on the map
 */
data class Placemark(val lyricPos: String,
                     val description: String,
                     val style: Style,
                     val loc: LatLng,
                     var marker: Marker? = null,
                     var text: String = "[missing]") : Serializable

