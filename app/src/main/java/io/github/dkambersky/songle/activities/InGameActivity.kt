package io.github.dkambersky.songle.activities

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
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
    private lateinit var collected: MutableMap<Int, MutableMap<Int, Boolean>>
    private lateinit var difficulty: Difficulty
    private lateinit var song: Song
    private lateinit var mapState: GameState
    private val mapElements = mutableMapOf<String, Any>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /* UI settings */
        revievView.movementMethod = ScrollingMovementMethod()

        /* Load data from intent */
        difficulty = intent.extras["Difficulty"] as Difficulty
        song = intent.extras["Song"] as Song

        /* Register listeners */
        b_view_progress.setOnClickListener { updateReview(); toggleReview() }
        b_guess.setOnClickListener { }


    }

    private fun updateReview() {
        val builder = StringBuilder()

        for (iLine in 0 until song.lyrics.size) {
            val line = song.lyrics[iLine] ?: continue
            for (iWord in 0 until line.size) {

                if (collected[iLine]?.get(iWord) == true) {
                    builder.append(line[iWord])
                } else {
                    builder.append("?")
                }
                builder.append(" ")
            }
            builder.append("\n")
        }

        revievView.text = builder.toString()
    }

    private fun toggleReview() {
        revievView.visibility = if (revievView.visibility == View.GONE) View.VISIBLE else View.GONE
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


        /* Initialize the 'collected' map */
        collected = mutableMapOf()
        gameMap.forEach {
            collected.getOrPut(it.lyricPos.first, { mutableMapOf() }).put(it.lyricPos.second, false)
        }
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

        /* Pick up objects in range */
        gameMap.filter { current.distanceTo(it) < difficulty.pickupRange }.forEach { collect(it) }

        updateGameState()

    }

    private fun updateGameState() {
        /* Update review on the fly if open */
        if (revievView.visibility == View.VISIBLE) {
            updateReview()
        }


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

        collected[placemark.lyricPos.first]!!.put(placemark.lyricPos.second, true)
        mapState.pickedUpPlacemarks++

    }

}