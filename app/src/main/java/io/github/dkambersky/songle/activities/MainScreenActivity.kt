package io.github.dkambersky.songle.activities


import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.data.Song
import io.github.dkambersky.songle.data.SongleContext
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.listeners.SongMapListener
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import kotlinx.android.synthetic.main.activity_main_screen.*


class MainScreenActivity : BaseActivity() {

    private val downloadsInProgress: MutableSet<String> = mutableSetOf()

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

        /* Get next missing song */
        val nextSong: Song
        try {
            nextSong = songle.context.songs.first { !songle.context.maps.containsKey(it.num) }
        } catch (e: NoSuchElementException) {
            /* Return if everything is downloaded */
            return
        }

        println("Starting download of song $nextSong")
        /* Download the levels in parallel */
        for (level in 1..5) {

            val url = "${songle.context.root}${nextSong.id()}/map$level.kml"

            downloadsInProgress.add(url)
            DownloadXmlTask(
                    SongMapListener(
                            songle.context,
                            { finishMapDownload(url) },
                            nextSong.num,
                            level.toShort()
                    )
            ).execute(url)

        }

    }

    private fun finishMapDownload(url: String) {
        downloadsInProgress.remove(url)


        /* If all downloads for current song completed, download next song. */
        if (downloadsInProgress.isEmpty()) updateStep()

    }


    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()

        updateStep()
        b_newGame.isEnabled = true
    }


}
