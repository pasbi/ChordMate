package de.pakab.chordmate

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val TAG = "SpotifyAuthInterceptor"

class SpotifyAuthInterceptor(
    private val context: Context,
) : Interceptor {
    var tokenCached: String? = null
    val json = Json { ignoreUnknownKeys = true }

    fun getToken(): String {
        if (tokenCached == null) {
            renewToken()
        }
        return tokenCached!!
    }

    fun renewToken() {
        val service =
            Retrofit
                .Builder()
                .baseUrl("https://accounts.spotify.com/api/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(SpotifyWebApi::class.java)
        val response = service.getToken(context.getString(R.string.SPOTIFY_CLIENT_ID), BuildConfig.spotifyClientSecret).execute()
        if (response.code() == 200) {
            Log.i(TAG, "Got new access token.")
            tokenCached = response.body()?.accessToken
        } else {
            Log.e(TAG, "AccessTokenResponse (${response.code()}), ${response.message()}")
        }
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val currentRequest = chain.request().newBuilder()
        currentRequest.addHeader("Authorization", "Bearer ${getToken()}")
        val newRequest = currentRequest.build()
        val response = chain.proceed(newRequest)
        if (response.code != 200) {
            Log.i(TAG, "Request failed (${response.code}): ${response.message}")
            if (response.code == 401) {
                Log.i(TAG, "Refresh Access token ...")
            }
        }
        return response
    }
}
