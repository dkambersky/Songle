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
import com.google.android.gms.maps.model.*
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.*
import kotlinx.android.synthetic.main.activity_in_game.*
import java.util.*
import kotlin.math.roundToInt


class InGameActivity : MapActivity() {

    /* Variables */
    private lateinit var gameMap: MutableList<Placemark>
    private lateinit var bonusMap: MutableList<Powerup>
    private lateinit var allWords: List<Placemark>
    private lateinit var collected: MutableMap<Int, MutableMap<Int, Boolean>>
    private lateinit var difficulty: Difficulty
    private lateinit var song: Song
    private lateinit var gameState: GameState
    private val freeWordBaseNum = 50
    private val mapUpgradeBaseNum = 25
    private val mapBounds = listOf(55.942617, 55.946233, -3.192473, -3.184319)
    private val mapElements = mutableMapOf<String, Any>()
    private var gameShown: Boolean = false

    /* Debug val */
    private val crichton = LatLng(55.944575, -3.187129)


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


    /* Main update loop */
    override fun onLocationChanged(current: Location?) {
        /* Don't process null locations, wait for map's initialization */
        if (current == null || !::gameMap.isInitialized) return

        /* Visualize pickup radius */
        val circle = mapElements["pickup"]
        if (circle is Circle) {
            circle.remove()
        }
        mapElements.put("pickup", map.addCircle(CircleOptions()
                .radius(difficulty.pickupRange.toDouble())
                .center(current.toLatLng())
                .fillColor(Color.CYAN)))

        /* Move camera */
        map.animateCamera(CameraUpdateFactory.newLatLng(
                current.toLatLng()
//                crichton
        ))


        println("a bonus is at ${bonusMap.first().loc} with marker at ${bonusMap.first().marker!!.position}")
        println("a marker is at ${gameMap.first().loc}")
        println("There are ${bonusMap.size} bonuses and ${gameMap.size} markers")


        /* Pick up placemarks in range */
        gameMap.filter { current.distanceTo(it) < difficulty.pickupRange }.forEach { collect(it) }


        /* Pick up power-ups in range */
        bonusMap.filter { current.distanceTo(it) < difficulty.pickupRange }.forEach { activatePowerup(it) }

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

            println("Showing game!")
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

        /* Generate powerups */
        bonusMap = mutableListOf()
        generatePowerups()


        /* Initialize the 'collected' map */
        collected = mutableMapOf()
        gameMap.forEach {
            collected.getOrPut(it.lyricPos.first, { mutableMapOf() }).put(it.lyricPos.second, false)
        }
    }

    private fun generatePowerups() {

        /* This used to be more complex, simplified for balance*/
        val (numFreeWords, numMapUpgrades) = Pair(
                (difficulty.bonusItemFactor * (freeWordBaseNum)).roundToInt(),
                (difficulty.bonusItemFactor * (mapUpgradeBaseNum)).roundToInt())


        val rng = Random()

        songle.context.styles.put("freeWord",
                Style("freeWord", 2.25f, hue = BitmapDescriptorFactory.HUE_AZURE))

        (1..numFreeWords).forEach {

            /* Get random location on map */
            val pos = LatLng(
                    rng.doubleBetween(mapBounds[0], mapBounds[1]),
                    rng.doubleBetween(mapBounds[2], mapBounds[3]))

            /* Create and register powerup */
            val powerup = Powerup(pos, songle.context.styles["freeWord"] ?: Style(),
                    powerupType = PowerupType.FREE_WORD)

            bonusMap.add(powerup)
            addMarker(powerup)
        }


        songle.context.styles.put("mapUpgrade",
                Style("mapUpgrade", 2.25f, hue = BitmapDescriptorFactory.HUE_ORANGE))

        (1..numMapUpgrades).forEach {
            /* Get random location on map */
            val pos = LatLng(
                    rng.doubleBetween(mapBounds[0], mapBounds[1]),
                    rng.doubleBetween(mapBounds[2], mapBounds[3]))

            /* Create and register powerup */
            val powerup = Powerup(pos, songle.context.styles["mapUpgrade"] ?:
                    Style(), powerupType = PowerupType.MAP_UPGRADE)

            bonusMap.add(powerup)
            addMarker(powerup)

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

    /* Defines powerup functionality */
    private fun activatePowerup(placemark: Powerup) {
        println("Activated powerup $placemark")
        bonusMap.remove(placemark)
        placemark.marker!!.remove()


        when (placemark.powerupType) {
            PowerupType.FREE_WORD -> {
                collect(gameMap[Random().nextInt(gameMap.size)])
            }
            PowerupType.MAP_UPGRADE -> {
                increaseLevel()
            }
        }
    }


    /* Win conditions / game flow */
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


    /* QoL extension functions */
    private fun Random.doubleBetween(lower: Double, upper: Double): Double {
        return nextDouble() * (upper - lower) + lower
    }
}