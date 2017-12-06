package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.storage.Song
import io.github.dkambersky.songle.storage.SongleContext
import kotlinx.android.synthetic.main.activity_pre_game.*

class PreGameActivity : BaseActivity() {
    lateinit var context: SongleContext
    private var difficulty = Difficulty.MEDIUM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)

        b_easy.setOnClickListener { switchDifficulty(Difficulty.EASY) }


        b_startGame.setOnClickListener {
            println("Difficulty is " + difficulty)
            transition(InGameActivity::class.java) }


    }

    private fun switchDifficulty(difficulty: Difficulty) {
        this.difficulty = difficulty


    }


    private enum class Difficulty {
        EASY, MEDIUM, HARD
    }
}


