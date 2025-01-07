@file:Suppress("DEPRECATION")

package be.binarybeam.videoandaudioplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import be.binarybeam.videoandaudioplayer.databinding.ActivityPlayerBinding
import eightbitlab.com.blurview.RenderScriptBlur

class PlayerActivity : AppCompatActivity() {
    private lateinit var id: ActivityPlayerBinding
    private lateinit var music: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(id.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        music = intent.getSerializableExtra("content") as HashMap<String, String>
        id.loader.setupWith(findViewById(android.R.id.content), RenderScriptBlur(this)).setBlurRadius(15f)
        id.loader.setOnClickListener { }

        try {
            id.video.setVideoURI(Uri.parse(MainActivity().decrypt(music["path"].toString())))
            id.video.setOnPreparedListener {
                id.loader.visibility = View.GONE
                id.seekBar.max = id.video.duration
                id.duration.text = getTimeFromMillis(id.video.duration)

                id.video.start()
                id.playImg.setImageResource(R.drawable.round_pause_24)

                updateDuration()
                id.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            if (progress > id.video.currentPosition) {
                                if (id.video.canSeekForward()) id.video.seekTo(progress)
                            }
                            else if (id.video.canSeekBackward()) id.video.seekTo(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) { }
                    override fun onStopTrackingTouch(seekBar: SeekBar?) { }
                })
            }

            id.video.setOnCompletionListener { id.playImg.setImageResource(R.drawable.round_play_arrow_24) }
            id.cardView4.setOnClickListener { finish() }

            id.video.setOnErrorListener { _, what, _ ->
                Toast.makeText(this, "Error : $what", Toast.LENGTH_SHORT).show()
                finish()
                false
            }

            id.rightTapper.setOnClickListener { if (id.video.canSeekBackward()) seek(-10000) }
            id.leftTapper.setOnClickListener { if (id.video.canSeekForward()) seek(+10000) }
        }
        catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }

        id.playCard.setOnClickListener {
            if (id.video.isPlaying) {
                id.video.pause()
                id.playImg.setImageResource(R.drawable.round_play_arrow_24)
                return@setOnClickListener
            }

            try {
                id.video.resume()
                id.playImg.setImageResource(R.drawable.round_pause_24)
            }
            catch (e: Exception) {
                try {
                    id.loader.visibility = View.VISIBLE
                    id.video.start()
                    id.playImg.setImageResource(R.drawable.round_pause_24)
                }
                catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun seek(i: Int) {
        id.video.seekTo(id.video.currentPosition + i)
        id.seekBar.progress = id.video.currentPosition
    }

    private fun updateDuration() {
        val currentPosition = id.video.currentPosition

        id.currenTime.text = getTimeFromMillis(currentPosition)
        id.seekBar.progress = currentPosition
        Handler().postDelayed({ updateDuration() }, 1000)
    }

    private fun getTimeFromMillis(currentPosition: Int): String {
        var hrs = 0
        var min = 0
        var sec = currentPosition/1000

        if (sec > 59) {
            min = sec/60

            if (min > 59) {
                hrs = min/60
                min %= 60
                sec = min%60
            }
            else sec %= 60
        }
        return "$hrs:${if (min > 9) min else "0$min"}:${if (sec > 9) sec else "0$sec"}"
    }
}