package io.github.dkambersky.songle.activities

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MapStyleOptions
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Difficulty
import io.github.dkambersky.songle.data.definitions.GameState
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.Song
import kotlinx.android.synthetic.main.activity_in_game.*


class InGameActivity : MapActivity() {
    private lateinit var gameMap: MutableList<Placemark>
    private lateinit var allWords: List<Placemark>
    private lateinit var difficulty: Difficulty
    private lateinit var song: Song
    private lateinit var mapState: GameState
    private val mapElements = mutableMapOf<String, Any>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Load data from intent */
        difficulty = intent.extras["Difficulty"] as Difficulty
        song = intent.extras["Song"] as Song

        /* Register listeners */
        b_view_progress.setOnClickListener { transition(GameProgressActivity::class.java, Pair("marks", allWords.toTypedArray())) }
        b_guess.setOnClickListener { transition(GameProgressActivity::class.java) }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        /* Load dark mode */
        map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json))

        /* Game map look and feel */
        map.setMaxZoomPreference(22f)
        map.animateCamera(CameraUpdateFactory.zoomTo(18f))
        map.setMinZoomPreference(14f)
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = false


        /* Generate the game map */
        generateMap(difficulty, song)


    }


    private fun generateMap(difficulty: Difficulty, song: Song) {
        gameMap = songle.context.maps[song.num]?.
                get(difficulty.startMapMode)!!.toMutableList()
        gameMap.forEach { addMarker(it) }

        mapState = GameState(
                gameMap.size,
                difficulty.startMapMode,
                0,
                gameMap.size.div(5 - difficulty.startMapMode),
                gameMap.size.div(5 - difficulty.startMapMode)
        )


        allWords = gameMap.toList()
    }

    override fun onLocationChanged(current: Location?) {
        /* Don't process null locations, wait for map's initialization */
        if (current == null || !::gameMap.isInitialized) return

        map.animateCamera(CameraUpdateFactory.newLatLng(current.toLatLng()))


        /* Visualize pickup radius */
        val circle = mapElements["pickup"]
        if (circle is Circle) {
            circle.remove()
        }

        mapElements.put("pickup", map.addCircle(CircleOptions()
                .radius(difficulty.pickupRange.toDouble())
                .center(current.toLatLng())
                .fillColor(Color.CYAN)))

        gameMap.filter { current.distanceTo(it) < difficulty.pickupRange }.forEach { collect(it) }
        updateGameState()

    }

    private fun updateGameState() {
        /* Resolve map level upgrade */
        if (mapState.currentThreshold == mapState.pickedUpPlacemarks) {
            increaseLevel()
            mapState.currentThreshold = mapState.pickedUpPlacemarks
        }

        /* Update progress bars */
        progressBarMajor.progress =
                ((mapState.pickedUpPlacemarks.toFloat() /
                        mapState.maxPlacemarks.toFloat()) * 100).toInt()

        progressBarMinor.progress =
                (((mapState.pickedUpPlacemarks -
                        (mapState.step *
                                (mapState.pickedUpPlacemarks / mapState.step))).toFloat() /
                        mapState.currentThreshold.toFloat()) * 100).toInt()


        println("Updated state, major: ${progressBarMajor.progress}")

    }

    private fun increaseLevel() {
        println("Increasing level!")
    }

    private fun collect(placemark: Placemark) {
        println("Picked up ${placemark.lyricPos}, ${placemark.text(song.lyrics)}!")
        placemark.marker?.remove()
        gameMap.remove(placemark)

        mapState.pickedUpPlacemarks++

    }

}