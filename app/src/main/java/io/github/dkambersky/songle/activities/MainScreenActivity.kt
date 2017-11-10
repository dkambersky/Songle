package io.github.dkambersky.songle.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.network.DownloadXmlTask
import io.github.dkambersky.songle.network.SongsDatabaseListener
import io.github.dkambersky.songle.storage.SongleContext
import kotlinx.android.synthetic.main.activity_main_screen.*


class MainScreenActivity : BaseActivity() {
    private var receiver = NetworkReceiver()
    private var context: SongleContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)


        /* Register network receiver */
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        println("Registering receiver!")
//        registerReceiver(receiver,filter)

        /* Initialize context */
        context = SongleContext(mutableListOf(), mutableListOf(), applicationContext)

        /* Register listeners, set up UI */
        b_newGame.setOnTouchListener({ _, _ -> transition(PreGameActivity::class.java) })
        b_settings.setOnTouchListener({ _, _ -> transition(SettingsActivity::class.java) })
        b_about.setOnTouchListener({ _, _ -> transition(AboutActivity::class.java) })
//        b_leaderboard.setOnTouchListener()

        b_newGame.isEnabled = false

        /* Check for updates, populate maps */
        updateAndLoad()
    }

    private fun updateAndLoad() {
        val snackbarUpdating = snack("Hang tight! Checking for updates.", Snackbar.LENGTH_INDEFINITE)

        DownloadXmlTask(SongsDatabaseListener(context!!, { snackShowFinished(snackbarUpdating) }))
                .execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")

    }

    private fun snackShowFinished(snackbarUpdating: Snackbar) {
        snackbarUpdating.dismiss()
        b_newGame.isEnabled = true
    }


    // TODO preferences (replace networkPref)!
    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connMgr =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)
                            as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            if (/*networkPref == wifi && */
            networkInfo?.type == ConnectivityManager.TYPE_WIFI) {
                // Wi-Fi is connected, so use Wi-Fi
            } else if (/*networkPref == any && */networkInfo != null) {
                // Have a network connection and permission, so use data
            } else {
                // No Wi-Fi and no permission, or no network connection
            }
        }

    }


}
