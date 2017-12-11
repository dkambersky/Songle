package io.github.dkambersky.songle

import android.app.Application
import io.github.dkambersky.songle.data.DataManager
import io.github.dkambersky.songle.data.defs.SongleContext

/**
 * Stores global songleContext information to avoid the overhead
 * of constantly serializing and deserializing the same data
 */
class SongleApplication : Application() {
    lateinit var context: SongleContext
    lateinit var data: DataManager
}