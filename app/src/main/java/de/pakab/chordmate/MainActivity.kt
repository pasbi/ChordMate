package de.pakab.chordmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import de.pakab.chordmate.databinding.ActivityMainBinding

private var spotifyAppRemote: SpotifyAppRemote? = null
val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onStart() {
        super.onStart()

        val connectionParams =
            ConnectionParams
                .Builder(getString(R.string.SPOTIFY_CLIENT_ID))
                .setRedirectUri(getString(R.string.SPOTIFY_REDIRECT_URI))
                .showAuthView(true)
                .build()

        Log.d(TAG, "Connecting ...")
        SpotifyAppRemote.connect(
            this,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote?) {
                    spotifyAppRemote = appRemote
                    Log.d("MainActivity", "Connected to Spotify.")
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request: AuthorizationRequest =
            AuthorizationRequest
                .Builder(
                    getString(R.string.SPOTIFY_CLIENT_ID),
                    AuthorizationResponse.Type.TOKEN,
                    getString(R.string.SPOTIFY_REDIRECT_URI),
                ).setScopes(arrayOf("app-remote-control"))
                .build()

        AuthorizationClient.openLoginActivity(this, 1337, request)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navController = supportFragmentManager.findFragmentById(R.id.fragment)!!.findNavController()
        setupActionBarWithNavController(navController)
        toolbar.setNavigationOnClickListener { navController.popBackStack() }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1337) {
            val response: AuthorizationResponse = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // do whatever you need with the access token `response.accessToken`

                    // ADD YOUR CODE HERE and it should be able to play !
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Auth error : " + response.error)
                }

                else -> {
                    Log.e(TAG, "Auth result: " + response.type)
                }
            }
        }
    }

    private fun connected() {
        spotifyAppRemote?.let {
            // Play a playlist
            val playlistURI = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL"
            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("MainActivity", track.name + " by " + track.artist.name)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}
