package io.github.dkambersky.songle.activities

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.Difficulty
import io.github.dkambersky.songle.data.Placemark
import io.github.dkambersky.songle.data.Song
import io.github.dkambersky.songle.data.Style

class InGameActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    private fun generateMap(difficulty: Difficulty, song: Song) {
        println("Song $song, Difficulty $difficulty")

        val gameMap = songle.context.maps[song.num]
                ?.get(difficulty
                        .startMapMode.
                        toShort())!!


        for (point in gameMap) {
            addMarker(point)

        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        println("OnMapReady firing")
        /* Load dark mode */
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json))

        /* Default to Crichton St */
        val crichton = LatLng(55.944575, -3.187129)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(crichton))

        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            println("Security exception thrown [onMapReady]")
        }
        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true


        /* Generate the game map */
        generateMap(
                intent.extras["Difficulty"] as Difficulty,
                intent.extras["Song"] as Song)


    }


    private fun addMarker(loc: LatLng, title: String, style: Style) {

        mMap.addMarker(MarkerOptions().position(loc).title(title).icon(style.icon))
    }

    private fun addMarker(point: Placemark) {
        addMarker(point.loc, point.description, point.style)
    }
}
