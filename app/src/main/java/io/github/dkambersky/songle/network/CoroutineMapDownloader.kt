package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.data.defs.SongleContext
import io.github.dkambersky.songle.storage.MapParser
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Experimental coroutine downloadXml class
 */
class CoroutineMapDownloader(private var songleContext: SongleContext,
                             private var id: Short = -1, private var level: Short = -1, private val file: File) {
    fun fetchMap(vararg urls: String): Deferred<Unit> {
        return async {
            urls.forEach {
                val xml = downloadXml(it).await()
                processMapXml(xml)
            }
        }
    }


    private fun downloadXml(url: String): Deferred<String> {

        return async {
            var result: String
            while (true) {

                try {
                    result = loadXmlFromNetwork(url)
                    break
                } catch (e: SocketTimeoutException) {
                    println("Download of $url timed out.")
                }
            }
            result
        }
    }


    private fun processMapXml(result: String?) {
        /* Sanity checks for ID */

        if (id == (-1).toShort()) return
        if (result == null) {
            System.err.println("Map downloadXml failed! id $id, level $level")
            return
        }

        /* Save the downloaded song into a file */
        val outWriter = FileWriter(file)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()


        val map = MapParser(songleContext).parse(result.byteInputStream())

        songleContext.maps.getOrPut(id, { mutableMapOf() }).put(level, map)
    }

    /* Downloads an XML file given URL, returns as string */
    @Throws(SocketTimeoutException::class)
    private fun loadXmlFromNetwork(urlString: String): String {

        /* Open connection */
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect()
        val reader = conn.inputStream.reader()
        val sb = StringBuilder()

        /* Write out and return */
        reader.forEachLine { sb.append(it) }
        return sb.toString()

    }


}