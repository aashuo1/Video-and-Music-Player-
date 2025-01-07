package be.binarybeam.videoandaudioplayer

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import be.binarybeam.videoandaudioplayer.databinding.PreviewBinding
import com.bumptech.glide.Glide

class MusicAdaptor(private val activity: Activity, private val itemList: ArrayList<MainActivity.Music>, private val broadcast: EditText) : RecyclerView.Adapter<MusicAdaptor.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PreviewBinding.inflate(LayoutInflater.from(activity), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int { return itemList.size }
    override fun getItemId(position: Int): Long { return position.toLong() }
    override fun getItemViewType(position: Int): Int { return position }

    inner class ViewHolder(private val id: PreviewBinding) : RecyclerView.ViewHolder(id.root) {
        fun bind(position: Int) {
            val music = itemList[position]

            id.name.text = MainActivity().decrypt(music.title)
            id.artist.text = MainActivity().decrypt(music.artist)

            Glide.with(activity.applicationContext).load(MainActivity().decrypt(music.cover)).into(id.cover)
            id.cardView3.setOnClickListener {
                val map = hashMapOf(
                    "title" to music.title,
                    "artist" to music.artist,
                    "path" to music.path,
                    "cover" to music.cover
                )
                activity.startActivity(Intent(activity, PlayerActivity::class.java).putExtra("content", map))
            }

            id.root.setOnClickListener {
                val map = hashMapOf(
                    "title" to music.title,
                    "artist" to music.artist,
                    "path" to music.path,
                    "cover" to music.cover
                )
                activity.startActivity(Intent(activity, PlayerActivity::class.java).putExtra("content", map))
            }

            id.root.setOnLongClickListener {
                broadcast.setText(position.toString())
                true
            }
        }
    }
}