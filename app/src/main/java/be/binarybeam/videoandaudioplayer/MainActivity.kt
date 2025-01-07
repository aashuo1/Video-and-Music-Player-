@file:Suppress("DEPRECATION")

package be.binarybeam.videoandaudioplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import be.binarybeam.videoandaudioplayer.databinding.ActivityMainBinding
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.serialization.Serializable
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var id: ActivityMainBinding
    private lateinit var shaker: Vibrator
    private var musicList = ArrayList<Music>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = ActivityMainBinding.inflate(layoutInflater)
        shaker = getSystemService(VIBRATOR_SERVICE) as Vibrator
        setContentView(id.root)

        try {
            val songs = File(filesDir, "songs.txt").readText().split(";")
            for (music in songs) {
                if (music.isEmpty()) continue
                val data = music.split(":")
                musicList.add(Music(data[0], data[1], data[2], data[3]))
            }
        }
        catch (e: Exception) {
            Toast.makeText(this, "No songs found.", Toast.LENGTH_SHORT).show()
        }

        id.recycler.adapter = MusicAdaptor(this, musicList, id.broadcast)
        id.recycler.layoutManager = LinearLayoutManager(this)
        id.noteBar.setupWith(findViewById(android.R.id.content), RenderScriptBlur(this)).setBlurRadius(15f)
        id.noteBar.setOnClickListener { }

        id.cardView2.setOnClickListener {
            vibrate()
            id.doneCard.setOnClickListener {
                vibrate()
                listChanges(true, 0)
            }

            id.deleteCard.visibility = View.GONE
            id.noteBar.visibility = View.VISIBLE
            id.noteBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        }

        id.hint3.setOnClickListener {
            vibrate()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, 11)
        }

        id.hint4.setOnClickListener {
            vibrate()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 10)
        }

        id.broadcast.addTextChangedListener { input->
            if (input.toString().isNotEmpty()) {
                val position = input.toString().toInt()

                id.doneCard.setOnClickListener {
                    vibrate()
                    listChanges(false, position)
                }

                id.deleteCard.setOnClickListener {
                    musicList.removeAt(position)
                    id.recycler.adapter?.notifyItemRemoved(position)

                    id.noteBar.visibility = View.GONE
                    id.noteBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out))

                    clearNoteInput()
                    vibrate()
                    updateSongs()
                }

                id.broadcast.setText("")
                id.deleteCard.visibility = View.VISIBLE
                id.noteBar.visibility = View.VISIBLE
                id.noteBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

                id.input1.setText(decrypt(musicList[position].title))
                id.input2.setText(decrypt(musicList[position].artist))
                id.input3.setText(decrypt(musicList[position].path))
                id.input4.setText(decrypt(musicList[position].cover))
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (data != null) {
                when (requestCode) {
                    10 -> id.input4.setText(uri.toString())
                    11 -> id.input3.setText(uri.toString())
                }
            }
        }
    }

    private fun clearNoteInput() {
        id.input1.setText("")
        id.input2.setText("")
        id.input3.setText("")
        id.input4.setText("")
    }

    private fun listChanges(new: Boolean, position: Int) {
        val input1 = id.input1.text.toString().replace(";", "`co`").replace(":", "`sc`").trim()
        val input2 = id.input2.text.toString().replace(";", "`co`").replace(":", "`sc`").trim()
        val input3 = id.input3.text.toString().replace(";", "`co`").replace(":", "`sc`").trim()
        val input4 = id.input4.text.toString().replace(";", "`co`").replace(":", "`sc`").trim()

        if (input1.isEmpty() || input2.isEmpty() || input3.isEmpty() || input4.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (new) {
            musicList.add(position, Music(input1, input2, input3, input4))
            id.recycler.adapter?.notifyItemInserted(position)
        }
        else {
            musicList[position] = Music(input1, input2, input3, input4)
            id.recycler.adapter?.notifyItemChanged(position)
        }

        updateSongs()
        clearNoteInput()

        id.noteBar.visibility = View.GONE
        id.noteBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out))
    }

    private fun updateSongs() {
        val list = ArrayList<String>()
        for (music in musicList) { list.add("${music.title}:${music.artist}:${music.path}:${music.cover}") }
        File(filesDir, "songs.txt").writeText(list.joinToString(";"))
    }

    private fun vibrate() {
        shaker.vibrate(25)
    }

    @Serializable
    data class Music(
        val title: String = "",
        val artist: String = "",
        val path: String = "",
        val cover: String = ""
    )

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (id.noteBar.isVisible) {
            id.noteBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out))
            id.noteBar.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    fun decrypt(text: String): String {
        return text.replace("`co`", ";").replace("`sc`", ":")
    }
}