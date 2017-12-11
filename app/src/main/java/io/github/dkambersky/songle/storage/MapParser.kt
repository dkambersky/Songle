package io.github.dkambersky.songle.storage

import android.graphics.BitmapFactory
import android.util.Xml
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import io.github.dkambersky.songle.data.Placemark
import io.github.dkambersky.songle.data.SongleContext
import io.github.dkambersky.songle.data.Style
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.END_TAG
import org.xmlpull.v1.XmlPullParser.TEXT
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL


/**
 * Parses a single Map KML file for a song.
 */
class MapParser(context: SongleContext) : BaseParser(context) {

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
                    context.styles.put(style.id, style)

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
    private fun readStyle(parser: XmlPullParser): Style {
        /* Ensure we're reading a Style */
        require(parser, "Style")

        /* Read info */
        val id = parser.getAttributeValue(ns, "id")
        step(parser)
        require(parser, "IconStyle", true)
        val scale = readByTag(parser, "scale").toFloat()
        step(parser, 2)
        val iconUrl = readByTag(parser, "href")


        /* Fetch & build style */
        val inStream = URL(iconUrl).content as InputStream
        val buffer = ByteArray(8192)
        var bytesRead: Int = inStream.read(buffer)
        val output = ByteArrayOutputStream()
        while (bytesRead != -1) {
            bytesRead = inStream.read(buffer)
            output.write(buffer, 0, bytesRead)
        }
        val iconBitmap = output.toByteArray()

        val bm = BitmapFactory.decodeByteArray(iconBitmap, 0, iconBitmap.size)
        val icon = BitmapDescriptorFactory.fromBitmap(bm)

        /* Get rid of end tags */
        while (parser.eventType == END_TAG || (parser.eventType == TEXT && parser.isWhitespace)) {
            if (parser.name != "Style") parser.next() else break
        }

        /* Return */
        println("Built style with ID $id")
        return Style(id, scale, icon)

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemark(parser: XmlPullParser): Placemark {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark")
        var name = ""
        var description = ""
        var style = Style()
        var point = LatLng(0.0, 0.0)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "name" -> name = readByTag(parser, "name")
                "description" -> description = readByTag(parser, "description")
                "styleUrl" -> style = context.styles[readByTag(parser, "styleUrl").substring(1)] ?: Style()
                "Point" -> point = readPoint(parser)
                else -> skip(parser)
            }
        }
        return Placemark(name, description, style, point)
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