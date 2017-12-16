package io.github.dkambersky.songle.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        textView2.movementMethod = ScrollingMovementMethod()
    }


}