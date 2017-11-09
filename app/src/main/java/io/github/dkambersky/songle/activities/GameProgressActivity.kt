package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_game_progress.*

class GameProgressActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_progress)
        setSupportActionBar(toolbar)
        backButton.setOnTouchListener({_,_ -> transition(InGameActivity::class.java)})
    }

}
