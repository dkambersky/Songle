package io.github.dkambersky.songle.network

import android.os.AsyncTask
import io.github.dkambersky.songle.network.listeners.DownloadCompleteListener
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by David on 09/11/2017.
 */
class DownloadXmlTask(private val caller: DownloadCompleteListener) :
        AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg urls: String): String? {
        try {
            return loadXmlFromNetwork(urls[0])
        } catch (e: IOException) {
            System.err.println("Unable to load content. Check your network connection\n" +
                    "Relevant URL: ${urls[0]}")
        } catch (e: XmlPullParserException) {
            System.err.println("Error parsing XML")
        } catch (e: ArrayIndexOutOfBoundsException) {
            System.err.println("No download strings specified.")
        }
        return null
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
        // Also available: HttpsURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        // Starts the query
        conn.connect()
        return conn.inputStream
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }

}