package de.pakab.chordmate.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.pakab.chordmate.R
import de.pakab.chordmate.SpotifyRemoteControl
import de.pakab.chordmate.databinding.FragmentSongBinding
import de.pakab.chordmate.viewmodel.SongViewModel
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "SongFragment"

class SongFragmentMenuProvider(
    val fragment: SongFragment,
) : MenuProvider {
    var menu: Menu? = null

    override fun onCreateMenu(
        menu: Menu,
        menuInflater: MenuInflater,
    ) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_song, menu)
        menu.findItem(R.id.action_mode_both).isChecked = true
        this.menu = menu
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.action_delete_song -> {
                fragment.showDeleteSongDialog()
                true
            }
            R.id.action_edit_song -> {
                fragment.findNavController().navigate(
                    SongFragmentDirections.actionSongFragmentToAddFragment(currentSong = fragment.args.currentSong),
                )
                true
            }
            R.id.action_mode_play, R.id.action_mode_scroll, R.id.action_mode_both -> {
                menu!!.findItem(R.id.action_mode_play).isChecked = false
                menu!!.findItem(R.id.action_mode_scroll).isChecked = false
                menu!!.findItem(R.id.action_mode_both).isChecked = false
                menuItem!!.isChecked = true
                true
            }
            R.id.action_adj_speed -> {
                menuItem.isChecked = !menuItem.isChecked
                fragment.setSeekBarScrollSpeedVisibility(menuItem.isChecked)
                true
            }
            else -> false
        }
}

class SongFragment : Fragment() {
    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!
    val args: SongFragmentArgs by navArgs()
    val positionTracker = SpotifyPlaybackPositionTracker()
    var blockSeekBarUpdates = false
    private var scrollAnimator: ValueAnimator? = null
    var isPlaying: Boolean = false

    fun showDeleteSongDialog() {
        AlertDialog
            .Builder(
                requireContext(),
            ).setTitle("Delete ${args.currentSong.title} – ${args.currentSong.interpret}?")
            .setPositiveButton("Delete") { dialog, which -> deleteCurrentSong() }
            .setNegativeButton("Cancel") { dialog, which -> null }
            .create()
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        val view = binding.root
        val toolbar = (activity as AppCompatActivity).supportActionBar!!
        toolbar.title = "${args.currentSong.title} – ${args.currentSong.interpret}"
        _binding!!.tvSongContent.text = args.currentSong.content.orEmpty()
        val menuProvider = SongFragmentMenuProvider(this)
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding!!.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                _binding!!.btnPlayPause.text = "▶"
                SpotifyRemoteControl.playerApi()?.pause()
                pauseAutoScroll()
            } else {
                isPlaying = true
                _binding!!.btnPlayPause.text = "⏸"
                val menu = menuProvider.menu
                val both = menu?.findItem(R.id.action_mode_both)?.isChecked == true
                val scroll = menu?.findItem(R.id.action_mode_scroll)?.isChecked == true
                val play = menu?.findItem(R.id.action_mode_play)?.isChecked == true
                if (scroll || both) resumeAutoScroll()
                if (play || both) {
                    args.currentSong.trackId?.let {
                        Log.i(TAG, "Play $it")
                        SpotifyRemoteControl.playerApi()?.play("spotify:track:$it")?.setResultCallback {
                            Timer().schedule(timerTask { seek() }, 100)
                        }
                    }
                }
            }
        }
        SpotifyRemoteControl.playerApi()!!.subscribeToPlayerState()!!.setEventCallback { event ->
            _binding!!.seekBar.max = event!!.track.duration.toInt()
            if (event.isPaused) {
                positionTracker.stop()
            } else {
                positionTracker.start { if (!blockSeekBarUpdates) _binding!!.seekBar.progress = it.toInt() }
            }
        }

        _binding!!.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    val duration = progress.milliseconds
                    val seconds = "%02d".format(duration.inWholeSeconds % 60)
                    val milliSeconds = "%03d".format(duration.inWholeMilliseconds % 1000)
                    _binding!!.tvPosition.text = "${duration.inWholeMinutes}:$seconds:$milliSeconds"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    blockSeekBarUpdates = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seek()
                    blockSeekBarUpdates = false
                }
            },
        )

        initTextViewSongContent(view)

        return view
    }

    private fun initTextViewSongContent(view: ConstraintLayout) {
        val textView = view.findViewById<TextView>(R.id.tv_song_content)!!
        textView.movementMethod = ScrollingMovementMethod()
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            startSmoothAutoScroll()
        }

        // Pause on touch
        textView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> pauseAutoScroll()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    resumeAutoScroll()
                    view.performClick()
                }
            }
            false
        }
    }

    fun seek() {
        SpotifyRemoteControl.playerApi()?.seekTo(_binding!!.seekBar.progress.toLong())
    }

    fun deleteCurrentSong() {
        ViewModelProvider(this)[SongViewModel::class.java].deleteSong(args.currentSong)
        findNavController().navigate(SongFragmentDirections.actionSongFragmentToListFragment())
    }

    fun setSeekBarScrollSpeedVisibility(visible: Boolean) {
        view?.findViewById<View>(R.id.sb_scroll_speed)?.visibility =
            if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun startSmoothAutoScroll() {
        if (_binding == null || scrollAnimator != null) {
            return
        }
        val textView = _binding!!.tvSongContent
        val linesPerSecond = 3
        val totalHeight = textView.layout.height
        val visibleHeight = textView.height
        val maxScrollY = totalHeight - visibleHeight

        if (maxScrollY <= 0) return // No scrolling needed

        val lineHeight = textView.lineHeight
        val scrollSpeedPxPerSec = linesPerSecond * lineHeight

        val durationMs = (maxScrollY / scrollSpeedPxPerSec * 1000).toLong()

        scrollAnimator =
            ValueAnimator.ofInt(0, maxScrollY).apply {
                duration = durationMs
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    val scrollY = animation.animatedValue as Int
                    textView.scrollTo(0, scrollY)
                }
            }
    }

    private fun pauseAutoScroll() {
        scrollAnimator?.pause()
    }

    private fun resumeAutoScroll() {
        if (scrollAnimator == null) {
            return
        }
        if (scrollAnimator!!.isStarted) {
            scrollAnimator!!.resume()
        } else {
            scrollAnimator!!.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollAnimator?.cancel()
    }
}
