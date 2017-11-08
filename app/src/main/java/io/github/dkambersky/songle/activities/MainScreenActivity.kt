package io.github.dkambersky.songle.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_main_screen.*

class MainScreenActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        enterFullscreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)


        setContentView(R.layout.activity_main_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        enterFullscreen()

        /* Register listeners */
        b_newGame.setOnTouchListener({ _, _ -> transitionPreGame() })
        b_settings.setOnTouchListener({ _, _ -> transitionSettings() })

    }


    private fun enterFullscreen() {
        val decorView = window.decorView ?: return
        // Hide the status bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        val actionBar = actionBar
        actionBar?.hide()
    }


    /* Activity Transitions */
    private fun transitionPreGame(): Boolean {
        val intent = Intent(this, IngameActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun transitionSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

}
