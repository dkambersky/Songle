package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.data.SongleContext
import io.github.dkambersky.songle.storage.MapParser
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

/**
 * Experimental coroutine downloadXml class
 */

class CoroutineMapDownloader(private var songleContext: SongleContext,
                             private var id: Short = -1, private var level: Short = -1) {
    fun fetchMap(vararg urls: String) {
        async {
            urls.forEach {
                println("Starting download of $it")
                val xml = downloadXml(it).await()
                downloadComplete(xml)
            }
        }
    }


    private fun downloadXml(url: String): Deferred<String> {

        return async {
            loadXmlFromNetwork(url)
        }
    }


    private fun downloadComplete(result: String?) {
        /* Sanity checks for ID */
        println("DownloadComplete firing ")
        if (id == (-1).toShort()) return
        if (result == null) {
            System.err.println("Map downloadXml failed! id $id, level $level")
            return
        }

        /* Save the downloaded song into a file */
        val outFile = File(songleContext.context.filesDir, "song$id.xml")
        val outWriter = FileWriter(outFile)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()


        println("Trying to parse thing ")
        val map = MapParser(songleContext).parse(result.byteInputStream())

        /* Load into the application
     *   TODO do this properly in the parser
     */
        songleContext.maps.getOrPut(id, { mutableMapOf() }).put(level, map)

        println("Maps: ${songleContext.maps}")
        /* Invoke callback, if specified*/
        // TODO callback equivalent
    }

    /* Loads the XML into a String representation */
    private fun loadXmlFromNetwork(urlString: String): String {
        val sb = StringBuilder()
        val reader = downloadUrl(urlString).reader()

        reader.forEachLine { sb.append(it) }

        return sb.toString()
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true

        conn.connect()
        return conn.inputStream
    }


}