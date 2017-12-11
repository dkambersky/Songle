package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.DataManager
import io.github.dkambersky.songle.data.defs.SongleContext
import kotlinx.android.synthetic.main.activity_main_screen.*
import java.util.*


class MainScreenActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        /* Initialize songleContext */
        songle.context = SongleContext(mutableListOf(), Collections.synchronizedMap(mutableMapOf()), Collections.synchronizedMap(mutableMapOf()), applicationContext, mutableSetOf(), "")
        songle.data = DataManager(songle)

        /* Register listeners, set up UI */
        b_newGame.setOnClickListener({ transition(PreGameActivity::class.java) })
        b_settings.setOnClickListener({ transition(SettingsActivity::class.java) })
        b_about.setOnClickListener({ transition(AboutActivity::class.java) })

        b_newGame.isEnabled = false

        /* Check for updates, populate maps */
        songle.data.initialize()
    }




}
