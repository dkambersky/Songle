package io.github.dkambersky.songle.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import io.github.dkambersky.songle.SongleApplication
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.Song
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.SongLyricsDownloader
import io.github.dkambersky.songle.network.SongMapDownloader
import io.github.dkambersky.songle.network.listeners.SongsDatabaseListener
import io.github.dkambersky.songle.storage.MapParser
import io.github.dkambersky.songle.storage.SongsParser
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.lang.StringBuilder
import java.util.*

/* Manages file downloads and storage */
class DataManager(private val songle: SongleApplication,
                  private val downloadsInProgress: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())) : BroadcastReceiver() {
    private val fileClearedSongs: File = File(songle.filesDir, "clearedSongs.txt")
    private var inited = false
    private var connection = ConnectionType.UNKNOWN


    suspend fun initialize() {
        if (!inited) {
            updateAndLoad()
            async {
                while (true) {
                    if (inited) {
                        return@async
                    }
                }
            }.await()
        }
    }


    private fun updateAndLoad(): Boolean {

        if (connection == ConnectionType.ONLINE) {
            /* AsyncTask holdover */
            DownloadXmlTask(
                    SongsDatabaseListener(
                            songle.context,
                            { fetchAllLyrics() }
                    )
            ).execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            return true
        } else {
            val dbFile = File(songle.context.context.filesDir, "songs.xml")
            if (!dbFile.exists()) {
                return false
            }

            songle.context.songs.addAll(
                    SongsParser(songle.context)
                            .parse(FileInputStream(dbFile))!!
            )
            fetchAllLyrics()
            return true
        }


    }

    private fun fetchAllLyrics() {

        val jobs = mutableListOf<Deferred<Int>>()

        /* Fetch lyrics in parallel */
        songle.context.songs.forEach {
            jobs += async { fetchSongLyrics(it) }
        }

        /* After all lyrics are fetched, fetch maps */
        launch {
            jobs.forEach {
                it.await()
            }
            println("Completed downloading lyrics, going for maps")
            fetchSongMapStep()
        }
    }

    private suspend fun fetchSongLyrics(song: Song): Int {

        val file = File(songle.filesDir, "${song.id()}.lyrics")

        if (file.isFile) {
            println("Loading lyrics from file $file")
            async { loadLyrics(file, song.num - 1) }.await()
            return song.num
        }

        SongLyricsDownloader(
                songle.context,
                song.num,
                file
        ).fetchLyrics("${songle.context.root}${song.id()}/words.txt")

        return song.num
    }


    private fun fetchSongMapStep() {
        val offline = connection == ConnectionType.OFFLINE || connection == ConnectionType.UNKNOWN
        println("Downloading offline? $offline")

        /* Get next missing song */
        val nextSong: Song

        try {
            nextSong = songle.context.songs.first { !songle.context.maps.containsKey(it.num) }
        } catch (e: NoSuchElementException) {
            /* Return if everything is downloaded */
            println("Completed fetching all songs")
            loadClearedSongs()
            return
        }

        val (files, urls) = Pair(
                nextSong.fileNames().map { File(songle.filesDir, it) },
                nextSong.urls().map { "${songle.context.root}$it" }
        )

        println("Fetching song ${nextSong.id()}: ${nextSong.title}")

        /* Download the levels in parallel */
        for (level in 1..5) {
            val file = files[level - 1]

            if (file.isFile) {
                downloadsInProgress.add(file.canonicalPath)
                launch {
                    println("Loading map from file $file")
                    val map = loadMap(file, offline).await()
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
                        file,
                        offline
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

    private fun loadMap(file: File, offline: Boolean = false): Deferred<List<Placemark>> {
        return async { MapParser(songle.context, offline).parse(FileInputStream(file)) }
    }

    private fun loadLyrics(file: File, id: Int) {
        val reader = file.reader()
        val sb = StringBuilder()

        /* Write out and return */
        reader.forEachLine { sb.append(it + "\n") }

        /* Save lyrics to Song object */
        val lyrics = sb.toString().lines()
                .filter { it != "" }
                .map { line ->
                    Pair(line.substring(0, 7).trim(' ', '\t').toInt(),
                            line.substring(7).split(" ", ", "))
                }.toMap()

        songle.context.songs[id].lyrics = lyrics
    }


    fun saveClearedSong(song: Song) {
        println("Adding $song to cleared")
        songle.context.clearedSongs.add(song)

        saveClearedSongs()
    }

    private fun saveClearedSongs() {
        val outWriter = FileWriter(fileClearedSongs)
        for (song in songle.context.clearedSongs) {
            outWriter.write(song.num.toString() + "\n")
        }

        outWriter.flush()
        outWriter.close()

    }

    private fun loadClearedSongs() {
        if (!fileClearedSongs.isFile) {
            return
        }
        val clearedIds = fileClearedSongs.reader().readLines().map { it.toInt() }

        inited = true
        for (id in clearedIds) {
            songle.context.clearedSongs.add(songle.context.songs.firstOrNull { it.num == id } ?: return)
        }
        println("Cleared: $clearedIds ")


    }

    /* Track connectivity changes */
    override fun onReceive(context: Context, intent: Intent) {

        val connMgr =
                context.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        val mobileEnabled = PreferenceManager.getDefaultSharedPreferences(songle)
                .getBoolean("mobileEnabled", false)

        connection = when {
            networkInfo?.type == ConnectivityManager.TYPE_WIFI -> {
                ConnectionType.ONLINE
            }
            networkInfo?.type == ConnectivityManager.TYPE_MOBILE && mobileEnabled -> {
                ConnectionType.ONLINE
            }
            else -> {
                ConnectionType.OFFLINE
            }
        }
        println("Switching connection state to $connection")
    }

}


enum class ConnectionType {
    ONLINE, OFFLINE, UNKNOWN
}