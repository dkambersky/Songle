package io.github.dkambersky.songle.services

import android.app.IntentService
import android.content.Intent

/**
 * Created by David on 09/11/2017.
 */
class BackgroundService : IntentService("service") {

    override fun onHandleIntent(workIntent: Intent) {
        // Gets data from the incoming Intent
        val dataString = workIntent.dataString



    }
}