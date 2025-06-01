package de.pakab.chordmate

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    fun searchTrack() {
        var query = HashMap<String, String>()
        query["query"] = "Hello"
        query["type"] = "track"
        val call = spotifyWebApi!!.search(query)
        call.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>,
                ) {
                    Log.i(TAG, "Search succeeded: ${response.code()} ${response.body()?.string()}")
                }

                override fun onFailure(
                    call: Call<ResponseBody?>,
                    t: Throwable,
                ) {
                    Log.e(TAG, "Search failed.")
                }
            },
        )
    }
}
