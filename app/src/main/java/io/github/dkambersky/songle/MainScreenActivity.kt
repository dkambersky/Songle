package io.github.dkambersky.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View

class MainScreenActivity : AppCompatActivity() {

    private val StartGameListener = View.OnTouchListener { _, _ ->
        startGame()
        true
    }

    private fun startGame(){
        val intent = Intent(this,IngameActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        enterFullscreen()
    }



    private fun enterFullscreen(){
        val decorView = window.decorView ?: return
        // Hide the status bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        val actionBar = actionBar
        actionBar?.hide()
    }


}
