package io.github.dkambersky.songle.activities

import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Difficulty
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.Song


class InGameActivity : MapActivity() {
    private lateinit var gameMap: MutableList<Placemark>
    private lateinit var difficulty: Difficulty
    lateinit var song: Song


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Load data from intent */
        difficulty = intent.extras["Difficulty"] as Difficulty
        song = intent.extras["Song"] as Song
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        /* Load dark mode */
        map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json))


        /* General map preferences */
        map.setMaxZoomPreference(22f)
        map.animateCamera(CameraUpdateFactory.zoomTo(18f))
        map.setMinZoomPreference(14f)
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)

        /* Generate the game map */
        generateMap(difficulty, song)

    }


    private fun generateMap(difficulty: Difficulty, song: Song) {
        gameMap = songle.context.maps[song.num]?.
                get(difficulty.startMapMode.toShort())!!.toMutableList()
        gameMap.forEach { addMarker(it) }

    }

    override fun onLocationChanged(current: Location?) {
        /* Don't process null locations, wait for map's initialization */
        if (current == null || !::gameMap.isInitialized) return

        map.animateCamera(CameraUpdateFactory.newLatLng(current.toLatLng()))

        gameMap.filter { current.distanceTo(it) < difficulty.pickupRange }.forEach { collect(it) }

    }


    private fun collect(placemark: Placemark) {
        println("Picked up ${placemark.name}!")
        placemark.marker?.remove()
        gameMap.remove(placemark)
    }

}