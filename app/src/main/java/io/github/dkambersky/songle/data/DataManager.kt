package io.github.dkambersky.songle.data

import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.SongleApplication
import io.github.dkambersky.songle.data.defs.Song
import io.github.dkambersky.songle.network.CoroutineMapDownloader
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import kotlinx.coroutines.experimental.launch
import java.util.*

/* Manages file downloads and storage */
class DataManager(private val songle: SongleApplication,
                  private val downloadsInProgress: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())) {

    fun initialize() {
        updateAndLoad()
    }


    private fun updateAndLoad() {
//        val snackbarUpdating = snack("Hang tight! Checking for updates.", Snackbar.LENGTH_INDEFINITE)

        DownloadXmlTask(SongsDatabaseListener(songle.context, {
//            snackShowFinished(snackbarUpdating)
            println("Finished updating main db!"); fetchSongStep()
        }))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")

    }

    private fun fetchSongStep() {

        /* Get next missing song */
        val nextSong: Song
        try {
            nextSong = songle.context.songs.first { !songle.context.maps.containsKey(it.num) }
        } catch (e: NoSuchElementException) {
            /* Return if everything is downloaded */
            println("Completed downloading all songs")
            return
        }

//        if()

        println("Starting download of song $nextSong")
        /* Download the levels in parallel */
        for (level in 1..5) {

            val url = "${songle.context.root}${nextSong.id()}/map$level.kml"

            downloadsInProgress.add(url)

            launch {
                CoroutineMapDownloader(
                        songle.context,
                        nextSong.num,
                        level.toShort()).fetchMap(url).await()
                finishMapDownload(url)

            }

        }

    }


    private fun finishMapDownload(url: String) {
        downloadsInProgress.remove(url)

        /* If all downloads for current song completed, download next song. */
        if (downloadsInProgress.isEmpty()) fetchSongStep()

    }

    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()

        fetchSongStep()
//        b_newGame.isEnabled = true
    }
}