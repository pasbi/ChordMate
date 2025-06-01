package de.pakab.chordmate

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SpotifyWebControl {
    private const val TAG = "SpotifyWebControl"
    var spotifyWebApi: SpotifyWebApi? = null
    val json = Json { ignoreUnknownKeys = true }

    fun start(context: Context) {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(OkHttpClient.Builder().addInterceptor(SpotifyAuthInterceptor(context)).build())
                .build()
        spotifyWebApi = retrofit.create(SpotifyWebApi::class.java)
    }

    fun searchTrack(
        query: String,
        callback: Callback<SearchResponse>,
    ) {
        spotifyWebApi!!
            .search(
                hashMapOf(
                    "query" to query,
                    "type" to "track",
                ),
            ).enqueue(callback)
    }
}
