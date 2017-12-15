package io.github.dkambersky.songle.network

import io.github.dkambersky.songle.data.definitions.SongleContext
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Downloads the lyrics of a song
 */
class SongLyricsDownloader(private var songleContext: SongleContext, private val id: Int, private val file: File) {
    fun fetchLyrics(vararg urls: String): Deferred<Unit> {
        return async {
            urls.forEach {
                val lyrics = downloadTxt(it).await()
                processLyrics(lyrics)
            }
        }
    }

    private fun downloadTxt(url: String): Deferred<String> {
        return async {
            var result: String
            while (true) {

                try {
                    result = loadTxtFromNetwork(url)
                    break
                } catch (e: SocketTimeoutException) {
                    println("Download of $url timed out.")
                }
            }
            result
        }
    }


    private fun processLyrics(result: String) {

        /* Save the downloaded lyrics into a file */
        val outWriter = FileWriter(file)
        outWriter.write(result)
        outWriter.flush()
        outWriter.close()


        /* Save lyrics to Song object */
        val lyrics = result.lines().map {
            Pair(it.substring(0, 8).trim(' ').toInt(),
                    it.substring(8).split(" ", ", "))
        }.toMap()

        songleContext.songs[id].lyrics = lyrics
    }

    /* Downloads an XML file given URL, returns as string */
    @Throws(SocketTimeoutException::class)
    private fun loadTxtFromNetwork(urlString: String): String {

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