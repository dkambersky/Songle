package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.network.listeners.DownloadCompleteListener
import io.github.dkambersky.songle.storage.MapParser
import io.github.dkambersky.songle.storage.Song
import io.github.dkambersky.songle.storage.SongleContext
import io.github.dkambersky.songle.storage.SongsParser
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/** Listens for and handles the download of the songs database */
class SongMapListener(var context: SongleContext,
                      private var callback: () -> Unit, private var id: Short = -1) : DownloadCompleteListener {



    override fun downloadComplete(result: String) {
        /* Sanity check for ID */
        if (id == (-1).toShort()) return

        /* Save the downloaded song into a file */
        val outFile = File(context.context.filesDir, "song$id.xml")
        val outWriter = FileWriter(outFile)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()

        val song = MapParser(context).parse(result.byteInputStream())

        /* Load into the application
         *   TODO do this properly in the parser
         */
        context.maps.put(id, song)


        /* Invoke callback, if specified*/
        callback.invoke()
    }

    private fun loadSongs(): List<Song> {
        return SongsParser(context)
                .parse(FileInputStream(File(context.context.filesDir, "songs.xml")))!!
    }

}