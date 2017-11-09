package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_main_screen.*

class MainScreenActivity : BaseActivity() {
    /* Don't do back button shenanigans */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        /* Register listeners */
        b_newGame.setOnTouchListener({ _, _ -> transition(InGameActivity::class.java) })
        b_settings.setOnTouchListener({ _, _ -> transition(SettingsActivity::class.java) })
        b_about.setOnTouchListener({ _, _ -> transition(AboutActivity::class.java) })
    }

//    override fun onBackPressed() {
//        finish()
//    }
}
