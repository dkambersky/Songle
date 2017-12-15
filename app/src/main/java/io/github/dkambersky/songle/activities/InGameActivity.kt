package io.github.dkambersky.songle.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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

    /* Variables */
    private lateinit var gameMap: MutableList<Placemark>
    private lateinit var allWords: List<Placemark>
    private lateinit var collected: MutableMap<Int, MutableMap<Int, Boolean>>
    private lateinit var difficulty: Difficulty
    private lateinit var song: Song
    private lateinit var gameState: GameState
    private val mapElements = mutableMapOf<String, Any>()
    private var gameShown: Boolean = false

    /* Map functionality */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /* UI settings */
        revievView.movementMethod = ScrollingMovementMethod()

        /* Hide everything except nag message until map ready*/
        for (iChild in 0 until MainGameLayout.childCount) {
            MainGameLayout.getChildAt(iChild).visibility = View.GONE
        }
        nagView.visibility = View.VISIBLE

        /* Load data from intent */
        difficulty = intent.extras["Difficulty"] as Difficulty
        song = intent.extras["Song"] as Song

        println("SONG NAME ${song.title}")

        /* Register listeners */
        b_view_progress.setOnClickListener { updateHintView(); toggleVisibility(revievView) }
        nameInputField.setOnKeyListener { _, keycode, event -> handleGuessDialogInput(keycode, event) }
        b_guess.setOnClickListener {
            toggleVisibility(nameInputField)
            toggleVisibility(t_guessInfo)
            revievView.visibility = View.GONE
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        /* Load dark mode */
        map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json))

        /* Game map look and feel */
        map.setMaxZoomPreference(18f)
        map.animateCamera(CameraUpdateFactory.zoomTo(18f))
        map.setMinZoomPreference(14f)
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = false


        /* Generate the game map */
        generateMap(difficulty, song)


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


        /* On first run, show game */
        if (!gameShown) {

            for (iChild in 0 until MainGameLayout.childCount) {
                MainGameLayout.getChildAt(iChild).visibility = View.VISIBLE
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(current.toLatLng(), 18f))

            /* 'Pull down the curtain' */
            nagView.visibility = View.GONE
            revievView.visibility = View.GONE
            nameInputField.visibility = View.GONE
            t_guessInfo.visibility = View.GONE

            updateGuessCounter()

            gameShown = true
        }


    }


    /* Game functionality */
    private fun generateMap(difficulty: Difficulty, song: Song) {
        gameMap = songle.context.maps[song.num]?.
                get(difficulty.startMapMode)!!.toMutableList()
        gameMap.forEach { addMarker(it) }

        gameState = GameState(
                gameMap.size,
                difficulty.startMapMode,
                0,
                gameMap.size.div(5 - difficulty.startMapMode),
                gameMap.size.div(5 - difficulty.startMapMode),
                difficulty.guessAttempts
        )


        allWords = gameMap.toList()


        /* Initialize the 'collected' map */
        collected = mutableMapOf()
        gameMap.forEach {
            collected.getOrPut(it.lyricPos.first, { mutableMapOf() }).put(it.lyricPos.second, false)
        }
    }


    private fun updateGameState() {
        /* Update review on the fly if open */
        if (revievView.visibility == View.VISIBLE) {
            updateHintView()
        }


        /* Resolve map level upgrade */
        if (gameState.currentThreshold == gameState.pickedUpPlacemarks) {
            increaseLevel()
            gameState.currentThreshold = gameState.pickedUpPlacemarks
        }

        /* Update progress bars */
        progressBarMajor.progress =
                ((gameState.pickedUpPlacemarks.toFloat() /
                        gameState.maxPlacemarks.toFloat()) * 100).toInt()

        progressBarMinor.progress =
                (((gameState.pickedUpPlacemarks -
                        (gameState.step *
                                (gameState.pickedUpPlacemarks / gameState.step))).toFloat() /
                        gameState.currentThreshold.toFloat()) * 100).toInt()


    }

    private fun increaseLevel() {
        println("Increasing level!")
    }

    private fun collect(placemark: Placemark) {
        println("Picked up ${placemark.lyricPos}, ${placemark.text(song.lyrics)}!")
        placemark.marker?.remove()
        gameMap.remove(placemark)

        collected[placemark.lyricPos.first]!!.put(placemark.lyricPos.second, true)
        gameState.pickedUpPlacemarks++

    }

    private fun makeGuess() {

        val guess = nameInputField.text.toString()

        /* Clean up */
        nameInputField.setText("")
        toggleVisibility(nameInputField)
        toggleVisibility(t_guessInfo)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(currentFocus.windowToken, 0)
        nameInputField.clearFocus()


        /* Guess */
        if (guess.equals(song.title, ignoreCase = true)) {
            endGameVictory()
        } else {
            if (gameState.guessesLeft > 0) {
                gameState.guessesLeft--
                if (gameState.guessesLeft == 0) {
                    endGameLoss()
                }
            }
        }

        /* Update guess counter */
        updateGuessCounter()
    }

    private fun endGameVictory() {
        transition(
                GameSummaryActivity::class.java,
                Pair("song", song.num),
                Pair("state", true)
        )
    }

    private fun endGameLoss() {
        transition(
                GameSummaryActivity::class.java,
                Pair("song", song.num),
                Pair("state", false)
        )
    }


    /* UI functionality */
    private fun handleGuessDialogInput(keyCode: Int, event: KeyEvent): Boolean {
        if ((event.action == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            makeGuess()
            return true
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun updateGuessCounter() {
        t_guessInfo.text = "Guesses left: ${if (gameState.guessesLeft == -1) "∞" else gameState.guessesLeft.toString()}"
    }

    private fun updateHintView() {
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


}