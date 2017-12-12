package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.DataManager
import io.github.dkambersky.songle.data.definitions.SongleContext
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
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

        /* Load data */
        initialize()
    }


    private fun initialize() {
        /* Launch non-blocking init co-routine */
        launch {
            async { songle.data.initialize() }.await()
            b_newGame.isEnabled = true
        }
    }
}
