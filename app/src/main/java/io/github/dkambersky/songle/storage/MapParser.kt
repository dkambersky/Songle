package io.github.dkambersky.songle.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Xml
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import io.github.dkambersky.songle.data.definitions.Placemark
import io.github.dkambersky.songle.data.definitions.SongleContext
import io.github.dkambersky.songle.data.definitions.Style
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.END_TAG
import org.xmlpull.v1.XmlPullParser.TEXT
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL


/**
 * Parses a single Map KML file for a song.
 */
class MapParser(context: SongleContext, val skipStyles: Boolean = false) : BaseParser(context) {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<Placemark> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, null)
            parser.nextTag()

            return readMap(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readMap(parser: XmlPullParser): List<Placemark> {

        /* Set up & validate beginning */
        val entries = ArrayList<Placemark>()
        require(parser, "kml", true)
        require(parser, "Document")

        /* Process entries one by one */
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Style" -> {
                    val style = readStyle(parser)
                    if (style != null) {
                        context.styles.put(style.id, style)
                    }

                }
                "Placemark" ->
                    entries.add(readPlacemark(parser))
                else ->
                    skip(parser)
            }
        }


        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readStyle(parser: XmlPullParser): Style? {

        /* Ensure we're reading a Style */
        require(parser, "Style")

        /* Read info */
        val id = parser.getAttributeValue(ns, "id")

        /* If we already know this style, don't bother building it again */
        if (context.styles.containsKey(id)) {
            skip(parser)
            return null
        }
        step(parser)
        require(parser, "IconStyle", true)
        val scale = readByTag(parser, "scale").toFloat()
        step(parser, 2)
        val iconUrl = readByTag(parser, "href")


        /* Fetch & build style */
        var image: Bitmap? = null
        var hue: Float? = null
        if (skipStyles) {
            hue = BitmapDescriptorFactory.HUE_ROSE
        } else {
            image = BitmapFactory.decodeStream(URL(iconUrl).content as InputStream)
        }


        /* Get rid of end tags */
        while (parser.eventType == END_TAG || (parser.eventType == TEXT && parser.isWhitespace)) {
            if (parser.name != "Style") parser.next() else break
        }

        /* Return */
        return Style(id, scale, image, hue)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemark(parser: XmlPullParser): Placemark {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark")
        var lyricPos = Pair(-1, -1)
        var description = ""
        var style = Style()
        var point = LatLng(0.0, 0.0)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "name" -> lyricPos = readLyricPos(parser)
                "description" -> description = readByTag(parser, "description")
                "styleUrl" -> style = context.styles[readByTag(parser, "styleUrl").substring(1)] ?: Style()
                "Point" -> point = readPoint(parser)
                else -> skip(parser)
            }
        }
        return Placemark(lyricPos, description, style, point)
    }


    private fun readLyricPos(parser: XmlPullParser): Pair<Int, Int> {
        val ints = readByTag(parser, "name").split(":").map { it.toInt() }
        return Pair(ints[0], ints[1])
    }

    private fun readPoint(parser: XmlPullParser): LatLng {
        /* Step over <Point> */
        step(parser)

        val (y, x) = readByTag(parser, "coordinates").split(",")

        /* Step over </Point> */
        step(parser)

        return LatLng(x.toDouble(), y.toDouble())
    }


}