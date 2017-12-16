package io.github.dkambersky.songle.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.DataManager
import io.github.dkambersky.songle.data.definitions.SongleContext
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*


class MainScreenActivity : BaseActivity() {
    private lateinit var snackbarAlert: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        /* Initialize application */
        if (!songle.inited) {
            songle.context = SongleContext(mutableListOf(), Collections.synchronizedMap(mutableMapOf()), Collections.synchronizedMap(mutableMapOf()), applicationContext, mutableSetOf(), "")
            songle.data = DataManager(songle)
            songle.inited = true
        }


        /* Register listeners, set up UI */
        b_exit.setOnClickListener({ transition(PreGameActivity::class.java) })
        b_leaderboard.setOnClickListener { transition(OverallProgressActivity::class.java) }
        b_settings.setOnClickListener({ transition(SettingsActivity::class.java) })
        b_about.setOnClickListener({ transition(AboutActivity::class.java) })


        b_exit.isEnabled = false

        /* Load data */
        initialize()
    }

    private fun initialize() {
        snackbarAlert = snack("Hang tight! Checking for updates.", Snackbar.LENGTH_INDEFINITE)

        /* Launch non-blocking init co-routine */
        launch(UI) {
            async(UI) {
                songle.data.initialize()
            }.await()
            println("Loading data finished.")
            snackbarAlert.dismiss()
            b_exit.isEnabled = true
        }
    }

}
