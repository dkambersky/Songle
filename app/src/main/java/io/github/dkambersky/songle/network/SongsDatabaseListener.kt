package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.storage.Song
import io.github.dkambersky.songle.storage.SongleContext
import io.github.dkambersky.songle.storage.SongsParser
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/**
 * Created by David on 10/11/2017.
 */
class SongsDatabaseListener() : DownloadCompleteListener {

    var context: SongleContext? = null


    constructor(context: SongleContext) : this() {
        this.context = context
    }


    override fun downloadComplete(result: String) {
        /* Save the current db into a file */
        val outFile = File(context!!.context.filesDir, "songs.xml")
        val outWriter = FileWriter(outFile)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()

        val songs = SongsParser(context!!.context).parse(result.byteInputStream()) ?: loadSongs()

        /* Load into the application
         *   TODO do this properly in the parser
         */
        context!!.songs.addAll(songs)
        context!!.ready = true
    }

    private fun loadSongs(): List<Song> {
        return SongsParser(context!!.context)
                .parse(FileInputStream(File(context!!.context.filesDir, "songs.xml")))!!
    }

}