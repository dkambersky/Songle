package io.github.dkambersky.songle.activities

import android.os.Bundle
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_pre_game.*

class PreGameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)
        b_easy.setOnClickListener({transition(InGameActivity::class.java)})
    }

//    fun switchDifficulty{
//
//    }



}
