package de.pakab.chordmate.fragments.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import de.pakab.chordmate.MySpinner
import de.pakab.chordmate.R
import de.pakab.chordmate.Track
import de.pakab.chordmate.databinding.FragmentAddBinding
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.transpose
import de.pakab.chordmate.viewmodel.SongViewModel
import kotlin.getValue

private const val TAG = "AddFragment"

class AddFragmentMenuProvider(
    val fragment: AddFragment,
) : MenuProvider {
    override fun onCreateMenu(
        menu: Menu,
        menuInflater: MenuInflater,
    ) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_edit_song, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.action_transpose_up -> {
                fragment.setContent(transpose(fragment.content(), 1))
                true
            }
            R.id.action_transpose_down -> {
                fragment.setContent(transpose(fragment.content(), -1))
                true
            }
            else -> false
        }
}

class AddFragment : Fragment() {
    private lateinit var mSongViewModel: SongViewModel
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val args: AddFragmentArgs by navArgs()
    private var trackSpinnerAdapter: SpotifySpinnerAdapter? = null
    private var blockTrackSpinnerAdapterSearch = false
    private var blockNextSongMetaDataUpdate = false
    private var initialSearch = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(AddFragmentMenuProvider(this), viewLifecycleOwner, Lifecycle.State.RESUMED)
        val view = binding.root

        trackSpinnerAdapter =
            SpotifySpinnerAdapter(requireContext())

        if (args.currentSong != null) {
            _binding!!.etTitle.setText(args.currentSong!!.title)
            _binding!!.etInterpret.setText(args.currentSong!!.interpret)
            _binding!!.etContent.setText(args.currentSong!!.content)
            trackSpinnerAdapter?.setCurrentTrackId(args.currentSong?.trackId)
        }
        updateTrackSpinnerCandidates()

        mSongViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        _binding!!.fabOk.setOnClickListener {
            if (args.currentSong == null) {
                insertDataToDatabase()
            } else {
                updateCurrentSong()
            }
        }

        _binding!!.spPlayback.adapter = trackSpinnerAdapter
        _binding!!.spPlayback.onItemSelectedListener =
            object : MySpinner.OnItemSelectedEvenIfSameListener {
                override fun onItemSelected(position: Int) {
                    if (blockNextSongMetaDataUpdate) {
                        blockNextSongMetaDataUpdate = false
                    } else {
                        updateSongMetaData()
                    }
                }
            }

        val playbackSearchUpdater =
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // do nothing
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    // do nothing
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!blockTrackSpinnerAdapterSearch) {
                        updateTrackSpinnerCandidates()
                    }
                }
            }
        _binding!!.etTitle.addTextChangedListener(playbackSearchUpdater)
        _binding!!.etInterpret.addTextChangedListener(playbackSearchUpdater)

        return view
    }

    private fun updateTrackSpinnerCandidates() {
        Log.i(TAG, "search: ${_binding!!.etTitle.text}, ${_binding!!.etInterpret.text}")
        trackSpinnerAdapter?.search(_binding!!.etTitle.text.toString(), _binding!!.etInterpret.text.toString()) {
            blockNextSongMetaDataUpdate = true
            if (initialSearch) {
                initialSearch = false
            } else {
                trackSpinnerAdapter!!
                    .bestSearchResultIndex()
                    ?.let { _binding!!.spPlayback.setSelection(it) }
            }
        }
    }

    private fun insertDataToDatabase() {
        val title = _binding!!.etTitle.text.toString()
        val currentSong = Song(0, title, _binding!!.etInterpret.text.toString(), _binding!!.etContent.text.toString())
        mSongViewModel.addSong(currentSong)
        Toast.makeText(requireContext(), "Added Song $title to database.", Toast.LENGTH_LONG).show()

        val action = AddFragmentDirections.actionAddFragmentToSongFragment(currentSong)
        findNavController().navigate(action, navOptions { popUpTo(R.id.listFragment) })
    }

    private fun updateCurrentSong() {
        args.currentSong!!.title = _binding!!.etTitle.text.toString()
        args.currentSong!!.interpret = _binding!!.etInterpret.text.toString()
        args.currentSong!!.content = _binding!!.etContent.text.toString()
        args.currentSong!!.trackId = (_binding!!.spPlayback.selectedItem as Track).id
        mSongViewModel.updateSong(args.currentSong!!)
        Log.v(TAG, "update song: ${args.currentSong!!.id}")
        val action = AddFragmentDirections.actionAddFragmentToSongFragment(args.currentSong!!)
        findNavController().navigate(action, navOptions { popUpTo(R.id.songFragment) })
    }

    fun setContent(text: String) {
        _binding?.etContent?.setText(text)
    }

    fun content(): String = _binding?.etContent?.text.toString()

    private fun updateSongMetaData() {
        val track = (_binding!!.spPlayback.selectedItem as Track)
        blockTrackSpinnerAdapterSearch = true
        _binding?.etTitle?.setText(track.name)
        _binding?.etInterpret?.setText(track.artists.joinToString(", ") { it -> it.name })
        blockTrackSpinnerAdapterSearch = false
    }
}
