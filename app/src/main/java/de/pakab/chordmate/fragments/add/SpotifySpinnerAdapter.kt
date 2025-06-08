package de.pakab.chordmate.fragments.add

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.size.Scale
import coil3.target.ImageViewTarget
import de.pakab.chordmate.R
import de.pakab.chordmate.SearchResponse
import de.pakab.chordmate.SpotifyWebControl
import de.pakab.chordmate.Track
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val TAG = "SpotifySpinnerAdapter"

class SpotifySpinnerAdapter(
    context: Context,
) : ArrayAdapter<Track>(context, R.layout.track_item) {
    val imageLoader =
        ImageLoader
            .Builder(
                context,
            ).components { add(OkHttpNetworkFetcherFactory(callFactory = { OkHttpClient() })) }
            .build()

    fun update(
        title: String,
        interpret: String,
    ) {
        val query = "$title $interpret"
        Log.i(TAG, "Searching for track $query")
        SpotifyWebControl.searchTrack(
            query,
            object : Callback<SearchResponse> {
                override fun onResponse(
                    call: Call<SearchResponse?>,
                    response: Response<SearchResponse?>,
                ) {
                    Log.i(TAG, "Search succeeded.")
                    clear()
                    response
                        .body()
                        ?.tracks
                        ?.items
                        ?.let { addAll(it) }
                    Log.i(TAG, "Items: $count")
                }

                override fun onFailure(
                    call: Call<SearchResponse?>,
                    t: Throwable,
                ) {
                    clear()
                    Log.e(TAG, "Failed to search track: $t")
                }
            },
        )
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view =
            if (convertView == null) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(R.layout.track_item, null)
            } else {
                convertView
            }
        val track = getItem(position)
        if (track != null) {
            view.findViewById<TextView>(R.id.tv_track_title)?.text = track.name
            view.findViewById<TextView>(R.id.tv_track_interpret)?.text = track.artists.joinToString(", ") { it.name }
            view.findViewById<TextView>(R.id.tv_track_album)?.text = track.album.name
            val images = track.album.images
            if (images.isNotEmpty()) {
                imageLoader.enqueue(
                    ImageRequest
                        .Builder(context)
                        .data(images[0].url)
                        .target(ImageViewTarget(view.findViewById<ImageView>(R.id.iv_track_album)))
                        .scale(Scale.FILL)
                        .build(),
                )
            }
        }
        return view
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View? = getView(position, convertView, parent)
}
