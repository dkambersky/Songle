package io.github.dkambersky.songle.storage

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.END_TAG
import org.xmlpull.v1.XmlPullParser.TEXT
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

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
            println("Now parsing a thingy with name ${parser.name}")
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


        val id = parser.getAttributeValue(ns, "id")

        parser.next()
        parser.next()
        require(parser, "IconStyle", true)

        val scale = readByTag(parser, "scale").toFloat()

        step(parser)
        step(parser)

        val icon = readByTag(parser, "href")


        /* Get rid of end tags*/
        while (parser.eventType == END_TAG || (parser.eventType == TEXT && parser.isWhitespace)) {
            if (parser.name != "Style") parser.next() else break
        }

        /* Return */
        return Style(id, scale, icon)

    }

    /* Steps one tag further and skips any potential whitespace */
    private fun step(parser: XmlPullParser, no: Int = 1) {
        for (i in 1..no) {
            while (parser.next() == TEXT && parser.isWhitespace) {

            }
        }

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemark(parser: XmlPullParser): Placemark {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark")
        var name = ""
        var description = ""
        var style = Style()
        var point = Point2D(0f, 0f)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "name" -> name = readByTag(parser, "name")
                "description" -> description = readByTag(parser, "description")
                "styleUrl" -> style = context.styles[readByTag(parser, "styleUrl")] ?: Style()
                "point" -> point = readPoint(parser)
                else -> skip(parser)
            }
        }
        println("Built a placemark  ")
        return Placemark(name, description, style, point)
    }

    private fun readPoint(parser: XmlPullParser): Point2D {
        skip(parser)
        return Point2D(1f, 1f)
    }


}