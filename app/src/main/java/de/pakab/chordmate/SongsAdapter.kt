package de.pakab.chordmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongsAdapter(
    private val db: AppDatabase,
    private val onClickListener: OnClickListener,
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {
    private val songList: MutableList<Song> = ArrayList<Song>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val song = songList[position]
        holder.textView.text = song.title
        holder.itemView.setOnClickListener { l ->
            onClickListener.onClick(song)
        }
    }

    override fun getItemCount(): Int = songList.size

    class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    abstract class OnClickListener {
        abstract fun onClick(song: Song)
    }
}
