package io.github.dkambersky.songle.data.definitions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.Serializable


/**
 * Defines a single Placemark on the map
 */
data class Placemark(val name: String, val description: String, val style: Style, val loc: LatLng, var marker: Marker?=null) : Serializable

