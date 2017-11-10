package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.storage.Song
import io.github.dkambersky.songle.storage.SongleContext
import io.github.dkambersky.songle.storage.SongsParser
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.lang.Thread.sleep

class SongsDatabaseListener() : DownloadCompleteListener {

    private var context: SongleContext? = null
    private var callback: (()-> Unit)? = null

    constructor(context: SongleContext, callback: (()-> Unit)? = null) : this() {
        this.context = context
        this.callback = callback
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


        
        /* Invoke callback, if specified*/
        callback?.invoke()
    }

    private fun loadSongs(): List<Song> {
        return SongsParser(context!!.context)
                .parse(FileInputStream(File(context!!.context.filesDir, "songs.xml")))!!
    }

}