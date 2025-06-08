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

private const val TAG = "SpotifySpinnerAdapter"

class SpotifySpinnerAdapter(
    context: Context,
) : ArrayAdapter<Track>(context, R.layout.track_item) {
    private val imageLoader =
        ImageLoader
            .Builder(
                context,
            ).components { add(OkHttpNetworkFetcherFactory(callFactory = { OkHttpClient() })) }
            .build()

    private var currentTrack: Track? = null
    private var candidateTracks: MutableList<Track> = ArrayList<Track>()
    private var searchCall: Call<SearchResponse>? = null

    fun setCurrentTrackId(trackId: String?) {
        if (trackId == null) {
            currentTrack = null
            update()
            return
        }
        SpotifyWebControl
            .track(
                trackId!!,
                object : Callback<Track> {
                    override fun onResponse(
                        call: Call<Track?>,
                        response: Response<Track?>,
                    ) {
                        Log.i(TAG, "Retrieving track $trackId succeeded.")
                        currentTrack = response.body()
                        update()
                    }

                    override fun onFailure(
                        call: Call<Track?>,
                        t: Throwable,
                    ) {
                        Log.e(TAG, "Failed to retrieve track: $trackId")
                        currentTrack = null
                        update()
                    }
                },
            )
    }

    fun search(
        title: String,
        interpret: String,
        onSearchSuccessful: () -> Unit,
    ) {
        val query = "$title $interpret"
        Log.i(TAG, "Searching for track $query")
        searchCall?.cancel()
        searchCall =
            SpotifyWebControl.searchTrack(
                query,
                object : Callback<SearchResponse> {
                    override fun onResponse(
                        call: Call<SearchResponse?>,
                        response: Response<SearchResponse?>,
                    ) {
                        clear()
                        response
                            .body()
                            ?.tracks
                            ?.items
                            ?.let {
                                candidateTracks = it
                            }
                        update()
                        onSearchSuccessful()
                        Log.i(TAG, "Search succeeded: $count")
                    }

                    override fun onFailure(
                        call: Call<SearchResponse?>,
                        t: Throwable,
                    ) {
                        Log.e(TAG, "Failed to search track: $t")
                        if (!call.isCanceled) {
                            candidateTracks = ArrayList<Track>()
                        }
                        update()
                    }
                },
            )
    }

    fun update() {
        clear()
        if (currentTrack == null) {
            addAll(candidateTracks)
        } else {
            add(currentTrack)
            addAll(candidateTracks.filter { it.id != currentTrack!!.id })
        }
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
                        .scale(Scale.FIT)
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

    fun bestResultIndex(): Int? {
        if (candidateTracks.isEmpty()) {
            return null
        }
        return if (currentTrack == null) {
            0
        } else {
            1
        }
    }
}
