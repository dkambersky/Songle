package io.github.dkambersky.songle.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import io.github.dkambersky.songle.R
import io.github.dkambersky.songle.SongleApplication
import java.io.Serializable


/**
 * Base Activity class.
 * Implements shared functionality like UI tweaks
 *  and simple transitions not to duplicate that
 *  in every activity separately.
 *
 */
abstract class BaseActivity : AppCompatActivity() {
    lateinit var songle: SongleApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Get application */
        songle = application as SongleApplication

        /* Enter / keep fullscreen */
        enterFullscreen()

    }

    /* UI Manipulation */
    private fun enterFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()
    }

    /* Activity Transitions */
    fun <T> transition(activity: Class<T>, vararg data: Pair<String, Serializable>): Boolean where T : Activity {
        val intent = Intent(this, activity)

        for (pair in data) {
            intent.putExtra(pair.first, pair.second)
        }


        startActivity(intent)
        return true
    }

    /* Common snackBar methods */
    fun snack(message: String, length: Int = Snackbar.LENGTH_LONG): Snackbar {

        val rootView = this.window.decorView.findViewById<View>(android.R.id.content)
        val bar = Snackbar.make(rootView, message, length)

        bar.show()
        return bar

    }

}