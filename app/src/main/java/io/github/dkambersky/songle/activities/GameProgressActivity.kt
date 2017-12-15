package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.definitions.Placemark

class GameProgressActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_progress)
//        setSupportActionBar(toolbar)
//        backButton.setOnTouchListener({_,_ -> transition(InGameActivity::class.java)})
        (intent.extras["marks"] as Array<Placemark>).forEach { println(it) }

    }

}
