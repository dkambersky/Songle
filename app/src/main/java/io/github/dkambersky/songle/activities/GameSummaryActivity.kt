package io.github.dkambersky.songle.activities

import android.os.Bundle
import android.text.Html
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_game_summary.*

class GameSummaryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_summary)

        val state = intent.extras["state"] as Boolean
        val song = songle.context.songs[intent.extras["song"] as Int - 1]

        /* Register listeners */
        b_exit.setOnClickListener { System.exit(0) }
        b_playAgain.setOnClickListener { transition(PreGameActivity::class.java) }
        b_mainMenu.setOnClickListener { transition(MainScreenActivity::class.java) }


        /* Set up the view */
        t_title.text = if (state) "Victory!" else "Defeat"

        t_summary.text = if (state)
            Html.fromHtml("Congratulations!<br>You guessed correctly.<br><br>The song was <br><b>${song.artist} - ${song.title}</b>.<br><br><br>" +
                    "Want to go back, or play another game?")
        else
            Html.fromHtml("You ran out of guesses!<br>Better luck next time.<br>" +
                    "<br><br>The song was <br><b>${song.artist} </b> -<b> ${song.title}</b>.<br><br>" +
                    "Want to go back, or play another game?")

        val color = if (state) R.color.positive else R.color.negative
        t_title.setTextColor(resources.getColor(color, theme))
    }


}
