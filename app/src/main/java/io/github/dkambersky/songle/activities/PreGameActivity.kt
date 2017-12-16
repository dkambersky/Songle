package io.github.dkambersky.songle.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.text.Html
import android.widget.Button
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Difficulty
import io.github.dkambersky.songle.data.definitions.Song
import kotlinx.android.synthetic.main.activity_pre_game.*
import java.util.*

class PreGameActivity : BaseActivity() {

    private lateinit var diffButtons: List<Button>
    private var difficulty: Difficulty? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)

        b_easy.setOnClickListener { switchDifficulty(Difficulty.EASY, it as Button) }
        b_med.setOnClickListener { switchDifficulty(Difficulty.MEDIUM, it as Button) }
        b_hard.setOnClickListener { switchDifficulty(Difficulty.HARD, it as Button) }


        /* Disable 'solved songs' switch if no songs have yet been solved */
        if (songle.context.clearedSongs.isEmpty()) {
            b_includeSolved.isEnabled = false
            b_includeSolved.isChecked = false
        }

        diffButtons = listOf(b_easy, b_med, b_hard)

        /* Set default difficulty if specified */
        val defaultDifficulty = PreferenceManager.getDefaultSharedPreferences(songle)
                .getString("defaultDifficulty", "-1").toIntOrNull() ?: -1

        if (defaultDifficulty in 1..3) {
            switchDifficulty(when (defaultDifficulty) {
                1 -> {
                    Difficulty.EASY
                }
                2 -> {
                    Difficulty.MEDIUM
                }
                3 -> {
                    Difficulty.HARD
                }
                else -> {
                    Difficulty.MEDIUM
                }
            })
        }

        b_startGame.setOnClickListener { enterGame() }

    }

    private fun enterGame() {
        if (difficulty != null) {
            transition(
                    InGameActivity::class.java,
                    Pair("Difficulty", difficulty!!),
                    Pair("Song", getSong()))
        } else {
            showSnackbar("Please select a difficulty", Snackbar.LENGTH_SHORT)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun switchDifficulty(difficulty: Difficulty, button: Button? = null) {
        this.difficulty = difficulty
        difficultyDescription.text = Html.fromHtml(
                """Difficulty Selected: <b>$difficulty</b><br><br>
                        Map starts at level <b>${difficulty.startMapMode}</b>.<br>
                        Pick-up radius: <b>${difficulty.pickupRange}</b><br>
                        Bonus item factor: <b>${difficulty.bonusItemFactor}</b><br>
                        # of guesses: <b>${if (difficulty.guessAttempts > 0) "" + difficulty.guessAttempts else "Unlimited"}</b>""")


        /* Change button appearance */
        diffButtons.map { it.setTextColor(resources.getColor(R.color.blackOverlay, theme)) }
        button?.setTextColor(resources.getColor(R.color.white, theme))

    }

    private fun getSong(): Song {
        val presentSongs = songle.context.songs.filter { songle.context.maps.containsKey(it.num) }
        val candidates =
                if (b_includeSolved.isChecked)
                    presentSongs
                else
                    presentSongs.filter { !songle.context.clearedSongs.contains(it) }

        return candidates[Random().nextInt(candidates.size)]
    }


}


