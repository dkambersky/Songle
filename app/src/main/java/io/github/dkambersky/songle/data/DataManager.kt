package io.github.dkambersky.songle.data

import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.SongleApplication
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.Song
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.SongLyricsDownloader
import io.github.dkambersky.songle.network.SongMapDownloader
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import io.github.dkambersky.songle.storage.MapParser
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileInputStream
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
            downloadLyrics()
        }))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")

    }

    fun downloadLyrics() {

        val jobs = mutableListOf<Deferred<Int>>()

        // start coroutine per thing
        for (song in songle.context.songs)
            jobs += async {

                val file = File(songle.filesDir, "${song.id()}.lyrics")

                SongLyricsDownloader(
                        songle.context,
                        song.num,
                        file
                ).fetchLyrics("${songle.context.root}${song.id()}/words.txt")

                song.num
            }



        launch {
            jobs.forEach {
                it.await()
                println("Download of lyrics for $it completed")
            }
            println(" Completed downloading lyrics, going for maps")
            fetchSongMapStep()
        }
    }


    private fun fetchSongMapStep() {

        /* Get next missing song */
        val nextSong: Song

        try {
            nextSong = songle.context.songs.first { !songle.context.maps.containsKey(it.num) }
        } catch (e: NoSuchElementException) {
            /* Return if everything is downloaded */
            println("Completed fetching all songs")
            return
        }

        val (files, urls) = Pair(
                nextSong.fileNames().map { File(songle.filesDir, it) },
                nextSong.urls().map { "${songle.context.root}$it" }
        )



        println("Fetching song $nextSong")
        /* Download the levels in parallel */
        for (level in 1..5) {
            val file = files[level - 1]

            if (file.isFile) {
                downloadsInProgress.add(file.canonicalPath)
                launch {
                    println("Loading map from file $file")
                    val map = loadMap(file).await()
                    songle.context.maps.getOrPut(nextSong.id().toInt(), { mutableMapOf() }).put(level, map)

                    finishMapDownload(file.canonicalPath)
                }
                continue
            }


            val url = urls[level - 1]
            println("Downloading map from URL $url")

            downloadsInProgress.add(url)

            launch {
                SongMapDownloader(
                        songle.context,
                        nextSong.num,
                        level,
                        file
                ).fetchMap(url).await()
                finishMapDownload(url)

            }

        }

    }


    private fun finishMapDownload(url: String) {
        downloadsInProgress.remove(url)

        /* If all downloads for current song completed, download next song. */
        if (downloadsInProgress.isEmpty()) fetchSongMapStep()

    }

    private fun loadMap(file: File): Deferred<List<Placemark>> {
        return async { MapParser(songle.context).parse(FileInputStream(file)) }
    }

    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()

        fetchSongMapStep()
//        b_newGame.isEnabled = true
    }
}