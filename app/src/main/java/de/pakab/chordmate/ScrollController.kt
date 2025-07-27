package de.pakab.chordmate
import android.os.Handler
import android.os.Looper
import android.widget.TextView

class ScrollController(
    private val textView: TextView,
) {
    var linesPerSecond: Double = 1.0
    private val handler = Handler(Looper.getMainLooper())
    private val intervalMs = 60L
    private var carryOver = 0.0
    var running = false
        private set

    private val runnable =
        object : Runnable {
            override fun run() {
                updateScroll()
                handler.postDelayed(this, intervalMs)
            }
        }

    fun start() {
        handler.post(runnable)
        running = true
    }

    fun stop() {
        carryOver = 0.0
        handler.removeCallbacks(runnable)
        running = false
    }

    fun updateScroll() {
        val intervalS = intervalMs / 1000.0
        val pixelDelta = (textView.lineHeight * linesPerSecond / intervalS) + carryOver
        val pixelDeltaInt = pixelDelta.toInt()
        carryOver = pixelDelta - pixelDeltaInt
        textView.scrollTo(0, textView.scrollY + pixelDeltaInt)
    }
}
