package de.pakab.chordmate.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    override fun onCreateMenu(
        menu: Menu,
        menuInflater: MenuInflater,
    ) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_song, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.action_delete_song -> {
                fragment.showDeleteSongDialog()
                true
            }
            R.id.action_edit_song -> {
                fragment.findNavController().navigate(
                    SongFragmentDirections.actionSongFragmentToAddFragment(currentSong = fragment.args.currentSong!!),
                )
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
        requireActivity().addMenuProvider(SongFragmentMenuProvider(this), viewLifecycleOwner, Lifecycle.State.RESUMED)
        _binding!!.btnPlayPause.setOnClickListener {
            val trackId = args.currentSong.trackId
            if (trackId != null) {
                Log.i(TAG, "Play $trackId")
                SpotifyRemoteControl
                    .playerApi()
                    ?.playerState
                    ?.setResultCallback { playerState ->
                        if (playerState.isPaused) {
                            SpotifyRemoteControl.playerApi()?.play("spotify:track:$trackId")?.setResultCallback {
                                Timer().schedule(
                                    timerTask {
                                        seek()
                                    },
                                    100,
                                )
                            }
                        } else {
                            SpotifyRemoteControl.playerApi()?.pause()
                        }
                    }
            }
        }
        SpotifyRemoteControl.playerApi()!!.subscribeToPlayerState()!!.setEventCallback { event ->
            _binding!!.seekBar.max = event!!.track.duration.toInt()
            if (event!!.isPaused) {
                _binding!!.btnPlayPause.setText(">")
                positionTracker.stop()
            } else {
                _binding!!.btnPlayPause.setText("||")
                positionTracker.start {
                    if (!blockSeekBarUpdates) {
                        _binding!!.seekBar.progress = it.toInt()
                    }
                }
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

        return view
    }

    fun seek() {
        SpotifyRemoteControl.playerApi()?.seekTo(_binding!!.seekBar.progress.toLong())
    }

    fun deleteCurrentSong() {
        ViewModelProvider(this)[SongViewModel::class.java].deleteSong(args.currentSong)
        findNavController().navigate(SongFragmentDirections.actionSongFragmentToListFragment())
    }
}
