package io.github.dkambersky.songle.network.listeners

import io.github.dkambersky.songle.data.defs.Song
import io.github.dkambersky.songle.data.defs.SongleContext
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
        if (result == null) {
            System.err.println("Database download failed!")
            return
        }


        /* Save the current db into a file */
        val outFile = File(context.context.filesDir, "songs.xml")
        val outWriter = FileWriter(outFile)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()

        val songs = SongsParser(context).parse(result.byteInputStream()) ?: loadSongs()


        context.songs.addAll(songs)


        /* Invoke callback, if specified*/
        callback.invoke()
    }

    private fun loadSongs(): List<Song> {
        return SongsParser(context)
                .parse(FileInputStream(File(context.context.filesDir, "songs.xml")))!!
    }

}