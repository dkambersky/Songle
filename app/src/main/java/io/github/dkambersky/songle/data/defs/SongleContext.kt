package io.github.dkambersky.songle.data.defs

import android.content.Context
import java.io.Serializable


/**
 * Carries application data for easy passing around
 */
data class SongleContext(val songs: MutableList<Song>,
                         val maps: MutableMap<Short, MutableMap<Short, List<Placemark>>>,
                         val styles: MutableMap<String, Style>,
                         @Transient val context: Context,
                         var clearedSongs: MutableSet<Song>,
                         var root: String) : Serializable