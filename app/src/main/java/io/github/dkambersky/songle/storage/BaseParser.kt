package io.github.dkambersky.songle.storage

import io.github.dkambersky.songle.data.definitions.SongleContext
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

    /**
     * Require a specific start tag - less boilerplate
     */
    protected fun require(parser: XmlPullParser, tag: String, skip: Boolean = false) {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        if (skip) parser.nextTag()
    }

    /** Steps [no] of tags forward and skips any potential whitespace
     * @param parser parser to use
     * @param no Number of tags to go forward; defaults to 1
     */
    protected fun step(parser: XmlPullParser, no: Int = 1) {

        /* The missing blocks are generally not a good idea,
         *   but this function is complete and it looks cleaner
         *   than two effectively empty blocks.
         */
        for (i in 1..no)
            while (parser.next() == XmlPullParser.TEXT && parser.isWhitespace);

    }



}

