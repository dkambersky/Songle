package io.github.dkambersky.songle.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.SongMapListener
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import io.github.dkambersky.songle.storage.SongleContext
import kotlinx.android.synthetic.main.activity_main_screen.*


class MainScreenActivity : BaseActivity() {

    private lateinit var context: SongleContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)


        /* Initialize context */
        context = SongleContext(mutableListOf(), mutableMapOf(), mutableMapOf(), applicationContext)

        /* Register listeners, set up UI */
        b_newGame.setOnTouchListener({ _, _ -> transition(PreGameActivity::class.java) })
        b_settings.setOnTouchListener({ _, _ -> transition(SettingsActivity::class.java) })
        b_about.setOnTouchListener({ _, _ -> transition(AboutActivity::class.java) })

        b_newGame.isEnabled = false

        /* Check for updates, populate maps */
        updateAndLoad()
    }

    private fun updateAndLoad() {
        val snackbarUpdating = snack("Hang tight! Checking for updates.", Snackbar.LENGTH_INDEFINITE)

        DownloadXmlTask(SongsDatabaseListener(context, { snackShowFinished(snackbarUpdating) }))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")


    }

    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()
        println(context.songs[0])
        println("Starting songmap dl task")
        DownloadXmlTask(SongMapListener(context, { play() }, 1))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map5.kml")

        print("After starting task")
        b_newGame.isEnabled = true
    }

    private fun play() {
        println("Printing shit!")
        println(context.maps[0])
    }


}
