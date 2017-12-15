package io.github.dkambersky.songle.data.definitions

import android.content.Context
import java.io.Serializable


/**
 * Carries application data for easy passing around
 */
data class SongleContext(val songs: MutableList<Song>,
                         val maps: MutableMap<Int, MutableMap<Int, List<Placemark>>>,
                         val styles: MutableMap<String, Style>,
                         @Transient val context: Context,
                         var clearedSongs: MutableSet<Song>,
                         var root: String) : Serializable