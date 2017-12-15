package io.github.dkambersky.songle.data.definitions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class Powerup(val loc: LatLng,
                   val style: Style,
                   var marker: Marker? = null,
                   val powerupType: PowerupType)

enum class PowerupType(val label: String) {
    FREE_WORD("Free Word"), MAP_UPGRADE("Map Upgrade")


}