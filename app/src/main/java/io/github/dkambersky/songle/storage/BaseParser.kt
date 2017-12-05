package io.github.dkambersky.songle.storage

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


/**
 *  Implements common functionality for parsers
 */
open class BaseParser(val context: SongleContext) {
    protected val ns: String? = null


    @Throws(XmlPullParserException::class, IOException::class)
    protected fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }


    @Throws(IOException::class, XmlPullParserException::class)
    protected fun readByTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val out = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return out
    }

    /* Utility function to reduce some of the boilerplate */
    protected fun require(parser: XmlPullParser, tag: String, skip: Boolean = false) {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        if (skip) parser.nextTag()
    }


}

