package de.pakab.chordmate.fragments

import android.os.Handler
import android.os.Looper
import android.util.Log
import de.pakab.chordmate.SpotifyRemoteControl
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.TimeSource

private const val TAG = "SpotifyPlaybackPositionTracker"

class SpotifyPlaybackPositionTracker {
    var timer = Timer()
    val clock = TimeSource.Monotonic
    var lastSynchronizedPosition: Long = 0
    var lastSynchronizedTimeMark: TimeSource.Monotonic.ValueTimeMark = clock.markNow()

    fun start(onPositionChange: (Long) -> Unit) {
        Log.i(TAG, "start")
        timer.schedule(
            timerTask {
                synchronize()
            },
            0,
            5000,
        )
        timer.schedule(
            timerTask {
                val elapsed = clock.markNow() - lastSynchronizedTimeMark
                onPositionChange(lastSynchronizedPosition + elapsed.inWholeMilliseconds)
            },
            0,
            1,
        )
    }

    fun stop() {
        Log.i(TAG, "stop")
        timer.cancel()
        timer = Timer()
    }

    private fun synchronize() {
        Handler(Looper.getMainLooper()).post {
            val x = SpotifyRemoteControl.playerApi()!!.playerState!!
            x.setResultCallback { event ->
                lastSynchronizedPosition = event.playbackPosition
                lastSynchronizedTimeMark = clock.markNow()
                Log.i(
                    TAG,
                    "synchronize ${lastSynchronizedPosition / 1000.0}ms at $lastSynchronizedTimeMark",
                )
            }
            x.setErrorCallback {
                Log.i(TAG, "ERROR: $it")
            }
        }
    }
}
