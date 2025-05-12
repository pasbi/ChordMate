package de.pakab.chordmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.pakab.chordmate.model.Song

class SongsAdapter(
    private val onClickListener: OnClickListener,
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {
    private var songList = emptyList<Song>()

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
        holder.itemView.findViewById<TextView>(R.id.tv_title).text = song.title
        holder.itemView.findViewById<TextView>(R.id.tv_interpret).text = song.interpret
        holder.itemView.setOnClickListener { l ->
            onClickListener.onClick(song)
        }
    }

    override fun getItemCount(): Int = songList.size

    class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view)

    abstract class OnClickListener {
        abstract fun onClick(song: Song)
    }

    fun setData(songs: List<Song>) {
        this.songList = songs
        notifyDataSetChanged()
    }
}
