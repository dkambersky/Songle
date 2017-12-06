package io.github.dkambersky.songle.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.Html
import android.widget.Button
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.Difficulty
import io.github.dkambersky.songle.data.Song
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

        b_startGame.setOnClickListener { enterGame() }


    }

    private fun enterGame() {
        if (difficulty != null) {
            transition(
                    InGameActivity::class.java,
                    Pair("Difficulty", difficulty!!),
                    Pair("Song", getSong()))
        } else {
            snack("Please select a difficulty", Snackbar.LENGTH_SHORT)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun switchDifficulty(difficulty: Difficulty, button: Button) {
        this.difficulty = difficulty
        difficultyDescription.text = Html.fromHtml(
                """Difficulty Selected: <b>$difficulty</b><br><br>
                        Map starts at level <b>${difficulty.startMapMode}</b>.<br>
                        Pick-up radius: <b>${difficulty.pickupRange}</b><br>
                        Bonus item factor: <b>${difficulty.bonusItemFactor}</b><br>
                        # of guesses: <b>${if (difficulty.guessAttempts > 0) difficulty.guessAttempts else "Unlimited"}</b>""")


        /* Change button appearance */
        diffButtons.map { it.setTextColor(resources.getColor(R.color.blackOverlay, theme)) }
        button.setTextColor(resources.getColor(R.color.white, theme))


    }

    private fun getSong(): Song {
        val candidates =
                if (b_includeSolved.isChecked)
                    songle.context.songs
                else
                    songle.context.songs.filter { !songle.context.clearedSongs.contains(it) }

        return candidates[Random().nextInt(candidates.size)]
    }


}


