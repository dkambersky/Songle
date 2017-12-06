package io.github.dkambersky.songle.activities


import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.SongleContext
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.listeners.SongMapListener
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import kotlinx.android.synthetic.main.activity_main_screen.*


class MainScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        /* Initialize context */
        songle.context = SongleContext(mutableListOf(), mutableMapOf(), mutableMapOf(), applicationContext, mutableSetOf(), "")

        /* Register listeners, set up UI */
        b_newGame.setOnClickListener({ transition(PreGameActivity::class.java) })
        b_settings.setOnClickListener({ transition(SettingsActivity::class.java) })
        b_about.setOnClickListener({ transition(AboutActivity::class.java) })

        b_newGame.isEnabled = false

        /* Check for updates, populate maps */
        updateAndLoad()
    }

    private fun updateAndLoad() {
        val snackbarUpdating = snack("Hang tight! Checking for updates.", Snackbar.LENGTH_INDEFINITE)


        DownloadXmlTask(SongsDatabaseListener(songle.context, { snackShowFinished(snackbarUpdating) }))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")


    }

    private fun updateStep() {
        DownloadXmlTask(SongMapListener(songle.context, { updateStep() }, 1, 1))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map5.kml")
    }

    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()

        updateStep()
        b_newGame.isEnabled = true
    }


}
