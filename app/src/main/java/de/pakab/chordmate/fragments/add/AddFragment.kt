package de.pakab.chordmate.fragments.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.set
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
import de.pakab.chordmate.renderChordPro
import de.pakab.chordmate.toChordPro
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
            R.id.action_convert_from_chord_pro -> {
                fragment.setContent(renderChordPro(fragment.content()))
                true
            }
            R.id.action_convert_to_chord_pro -> {
                fragment.setContent(toChordPro(fragment.content()))
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(AddFragmentMenuProvider(this), viewLifecycleOwner, Lifecycle.State.RESUMED)
        val view = binding.root

        if (args.currentSong != null) {
            _binding!!.etTitle.setText(args.currentSong!!.title)
            _binding!!.etInterpret.setText(args.currentSong!!.interpret)
            _binding!!.etContent.setText(args.currentSong!!.content)
        }

        mSongViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        _binding!!.btnAdd.setOnClickListener {
            if (args.currentSong == null) {
                insertDataToDatabase()
            } else {
                updateCurrentSong()
            }
        }
        return view
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
        Log.v("FOO", "update song: ${args.currentSong!!.id}")
        val action = AddFragmentDirections.actionAddFragmentToSongFragment(args.currentSong!!)
        findNavController().navigate(action, navOptions { popUpTo(R.id.songFragment) })
    }

    fun setContent(text: String) {
        _binding?.etContent?.setText(text)
    }

    fun content(): String = _binding?.etContent?.text.toString()
}
