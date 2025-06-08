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
import de.pakab.chordmate.R
import de.pakab.chordmate.databinding.FragmentAddBinding
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.transpose
import de.pakab.chordmate.viewmodel.SongViewModel
import kotlin.getValue

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
    val binding get() = _binding!!
    val args: AddFragmentArgs by navArgs()
    var trackSpinnerAdapter: SpotifySpinnerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(AddFragmentMenuProvider(this), viewLifecycleOwner, Lifecycle.State.RESUMED)
        val view = binding.root

        trackSpinnerAdapter = SpotifySpinnerAdapter(requireContext())

        if (args.currentSong != null) {
            _binding!!.etTitle.setText(args.currentSong!!.title)
            _binding!!.etInterpret.setText(args.currentSong!!.interpret)
            _binding!!.etContent.setText(args.currentSong!!.content)
        }

        mSongViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        _binding!!.fabOk.setOnClickListener {
            if (args.currentSong == null) {
                insertDataToDatabase()
            } else {
                updateCurrentSong()
            }
        }

        val spinnerAdapter = SpotifySpinnerAdapter(requireContext())
        _binding!!.spPlayback.adapter = spinnerAdapter
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
                    spinnerAdapter.update(_binding!!.etTitle.text.toString(), _binding!!.etInterpret.text.toString())
                }
            }
        _binding!!.etTitle.addTextChangedListener(playbackSearchUpdater)
        _binding!!.etInterpret.addTextChangedListener(playbackSearchUpdater)

        return view
    }

    private fun updateTrackSpinner() {
        trackSpinnerAdapter?.update(_binding!!.etTitle.text.toString(), _binding!!.etInterpret.text.toString())
    }

    private fun insertDataToDatabase() {
        val title = _binding!!.etTitle.text.toString()
        val currentSong = Song(0, title, _binding!!.etInterpret!!.text.toString(), _binding!!.etContent!!.text.toString())
        mSongViewModel.addSong(currentSong)
        Toast.makeText(requireContext(), "Added Song $title to database.", Toast.LENGTH_LONG).show()

        val action = AddFragmentDirections.actionAddFragmentToSongFragment(currentSong)
        findNavController().navigate(action, navOptions { popUpTo(R.id.listFragment) })
    }

    private fun updateCurrentSong() {
        args.currentSong!!.title = _binding!!.etTitle.text.toString()
        args.currentSong!!.interpret = _binding!!.etInterpret.text.toString()
        args.currentSong!!.content = _binding!!.etContent.text.toString()
        mSongViewModel.updateSong(args.currentSong!!)
        Log.v(TAG, "update song: ${args.currentSong!!.id}")
        val action = AddFragmentDirections.actionAddFragmentToSongFragment(args.currentSong!!)
        findNavController().navigate(action, navOptions { popUpTo(R.id.songFragment) })
    }

    fun setContent(text: String) {
        _binding?.etContent?.setText(text)
    }

    fun content(): String = _binding?.etContent?.text.toString()
}
