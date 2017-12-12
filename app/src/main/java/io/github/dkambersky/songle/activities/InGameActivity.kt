package io.github.dkambersky.songle.activities

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Difficulty
import io.github.dkambersky.songle.data.definitions.Song


class InGameActivity : MapActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Generate the game map */
        generateMap(
                intent.extras["Difficulty"] as Difficulty,
                intent.extras["Song"] as Song)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        /* Load dark mode */
        map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json))

        map.setMaxZoomPreference(22f)
        map.animateCamera(CameraUpdateFactory.zoomTo(18f))
        map.setMinZoomPreference(14f)

    }


    private fun generateMap(difficulty: Difficulty, song: Song) {
        val gameMap = songle.context.maps[song.num]?.get(difficulty.startMapMode.toShort())!!
        gameMap.forEach { addMarker(it) }

    }

}
