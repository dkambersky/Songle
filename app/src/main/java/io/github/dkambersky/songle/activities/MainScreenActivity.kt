package io.github.dkambersky.songle.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_main_screen.*

class MainScreenActivity : BaseActivity() {
    private var receiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)


        /* Register network receiver */
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(receiver,filter)

        /* Register listeners */
        b_newGame.setOnTouchListener({ _, _ -> transition(PreGameActivity::class.java) })
        b_settings.setOnTouchListener({ _, _ -> transition(SettingsActivity::class.java) })
        b_about.setOnTouchListener({ _, _ -> transition(AboutActivity::class.java) })

        /* Check for updates */
        updateCheck()
    }

    private fun updateCheck() {


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
