package io.github.dkambersky.songle.network.listeners

import io.github.dkambersky.songle.data.definitions.Song
import io.github.dkambersky.songle.data.definitions.SongleContext
import io.github.dkambersky.songle.storage.SongsParser
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/** Listens for and handles the download of the songs database */
class SongsDatabaseListener() : DownloadCompleteListener {


    private lateinit var context: SongleContext
    private lateinit var callback: () -> Unit

    constructor(context: SongleContext, callback: (() -> Unit)) : this() {
        this.context = context
        this.callback = callback
    }


    override fun downloadComplete(result: String?) {
        var songs: List<Song>?
        if (result == null) {
            println("Downloading song database failed, attempting to load saved")
            songs = loadSongs() ?: return
            context.songs.addAll(songs)

            /* Invoke callback, if specified*/
            callback.invoke()
            return
        }

        /* Save the current db into a file */
        val outFile = File(context.context.filesDir, "songs.xml")
        val outWriter = FileWriter(outFile)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()

        songs = SongsParser(context).parse(result.byteInputStream()) ?: loadSongs() ?: return

        context.songs.addAll(songs)


        /* Invoke callback, if specified*/
        callback.invoke()
    }

    private fun loadSongs(): List<Song>? {
        try {
            return SongsParser(context)
                    .parse(FileInputStream(File(context.context.filesDir, "songs.xml")))!!

        } catch (e: Exception) {
            System.err.println("Failed reading songs db file.")
            return null
        }

    }

}