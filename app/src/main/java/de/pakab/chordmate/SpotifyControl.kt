package de.pakab.chordmate

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SpotifyControl {
    private val TAG = "SpotifyControl"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    val requestCode = 237657

    fun start(activity: MainActivity) {
        val request: AuthorizationRequest =
            AuthorizationRequest
                .Builder(
                    activity.getString(R.string.SPOTIFY_CLIENT_ID),
                    AuthorizationResponse.Type.TOKEN,
                    activity.getString(R.string.SPOTIFY_REDIRECT_URI),
                ).setScopes(arrayOf("app-remote-control"))
                .build()

        Log.d(TAG, "open login activity")
        AuthorizationClient.openLoginActivity(activity, requestCode, request)
        renewAccessToken(activity)
    }

    fun onRequestReturns(
        context: Context,
        resultCode: Int,
        data: Intent?,
    ) {
        val response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)

        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                Log.d(TAG, "Request returned successfully.")
                connectToRemote(context)
            }
            AuthorizationResponse.Type.ERROR -> {
                Log.e(TAG, "Auth error : " + response.error)
            }
            else -> {
                Log.e(TAG, "Unexpected Auth result: " + response.type)
            }
        }
    }

    private fun connectToRemote(context: Context) {
        val connectionParams =
            ConnectionParams
                .Builder(context.getString(R.string.SPOTIFY_CLIENT_ID))
                .setRedirectUri(context.getString(R.string.SPOTIFY_REDIRECT_URI))
                .showAuthView(true)
                .build()

        Log.d(TAG, "Connecting to Remote App ...")
        SpotifyAppRemote.connect(
            context,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote?) {
                    spotifyAppRemote = appRemote
                    Log.d(TAG, "Connected to Spotify.")
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.connected_to_spotify),
                            Toast.LENGTH_SHORT,
                        ).show()
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, throwable.message, throwable)
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.failed_to_connect_to_spotify),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            },
        )
    }

    fun stop() {
        spotifyAppRemote?.let {
            it.playerApi.pause()
            SpotifyAppRemote.disconnect(it)
        }
    }

    fun renewAccessToken(context: Context) {
        val networkJson = Json { ignoreUnknownKeys = true }
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl("https://accounts.spotify.com/api/")
                .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
                .build()
        val service = retrofit.create(SpotifyWebApi::class.java)
        val call =
            service.getToken(
                context.getString(R.string.SPOTIFY_CLIENT_ID),
                BuildConfig.spotifyClientSecret,
            )
        call.enqueue(
            object : Callback<AccessTokenResponse> {
                override fun onResponse(
                    call: Call<AccessTokenResponse?>,
                    response: Response<AccessTokenResponse?>,
                ) {
                    Log.i(TAG, "Got Access Token")
                    response.body()
                    Log.i(TAG, "response: $response")
                    Log.i(TAG, "response body: ${response.body()}")
                    Log.i(TAG, "response body token: ${response.body()?.accessToken}")
                }

                override fun onFailure(
                    call: Call<AccessTokenResponse?>,
                    t: Throwable,
                ) {
                    Log.i(TAG, "Getting Access Token failed")
                }
            },
        )
    }

    private fun connected() {
        spotifyAppRemote?.let {
//            it.playerApi.resume()
//            // Play a playlist
//            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
//            it.playerApi.play(playlistURI)
//            // Subscribe to PlayerState
//            it.playerApi.subscribeToPlayerState().setEventCallback {
//                val track: Track = it.track
//                Log.d("MainActivity", track.name + " by " + track.artist.name)
//            }
        }
    }
}
