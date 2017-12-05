package io.github.dkambersky.songle.storage

import android.annotation.SuppressLint
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import java.io.FileInputStream
import java.text.ParseException

/**
 * Parses the Songs database.
 */
class SongsParser(context: SongleContext) : BaseParser(context) {
    private var root: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<Song>? {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, null)
            parser.nextTag()


            /* If reading from file, parse directly */
            if (input is FileInputStream) {
                parseMetadata(parser)
                return readDb(parser)
            }


            /* If reading from web, ensure we're parsing the right file; check timestamp */
            return if (parseMetadata(parser)) readDb(parser) else null
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDb(parser: XmlPullParser): List<Song> {
        val entries = ArrayList<Song>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the Song tag
            if (parser.name == "Song") {
                entries.add(readSong(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }


    /**
     * Ensures we're reading a Songs file,
     * parses the timestamp and root.
     *
     * @return *true* if the database needs an update; false otherwise
     */
    @SuppressLint("SimpleDateFormat")
    private fun parseMetadata(parser: XmlPullParser): Boolean {
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        root = parser.getAttributeValue(null, "root")
        val prefs = context.context.getSharedPreferences("base", 0)!!

        /* Check timestamps */
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        try {
            val stampUpdated = parser.getAttributeValue(null, "timestamp")
            val timeUpdated = format.parse(stampUpdated)
            val stampLastUpdated = prefs.getString("last-updated", "")

            /* Detect & handle first launch */
            if (stampLastUpdated == "") {
                val editor = prefs.edit()
                editor.putString("last-updated", stampUpdated)
                editor.apply()
                return true
            }

            /* Detect & handle newer version of database */
            val needUpdate = format.parse(stampLastUpdated).before(timeUpdated)
            if (needUpdate) {
                val editor = prefs.edit()
                editor.putString("last-updated", stampUpdated)
                editor.apply()
                return true
            }
        } catch (e: ParseException){
            /* Parsing failed, update just in case */
            return true
        }
        return false
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): Song {
        parser.require(XmlPullParser.START_TAG, ns, "Song")
        var number = "-1"
        var artist = ""
        var title = ""
        var link = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "Number" -> number = readByTag(parser, "Number")
                "Artist" -> artist = readByTag(parser, "Artist")
                "Title" -> title = readByTag(parser, "Title")
                "Link" -> link = readByTag(parser, "Link")
                else -> skip(parser)
            }
        }
        return Song(number.toShort(), artist, title, link)
    }





}