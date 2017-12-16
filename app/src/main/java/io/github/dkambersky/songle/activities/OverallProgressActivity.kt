package io.github.dkambersky.songle.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import io.github.dkambersky.songle.R
import kotlinx.android.synthetic.main.activity_overall_progress.*

class OverallProgressActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overall_progress)
        t_progress_main.movementMethod = ScrollingMovementMethod()

        rebuildList()
    }

    private fun rebuildList() {
        val songStrings = songle.context.songs.map {
            if (songle.context.clearedSongs.contains(it))
                "${it.artist} - ${it.title}"
            else
                "Song ${it.id()} - hidden"
        }

        val builder = StringBuilder()

        println(songStrings.size)
        for (song in songStrings) {
            builder.append(song + "\n\n")
        }

        t_progress_main.text = builder.toString()
    }

}
