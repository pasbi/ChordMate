package de.pakab.chordmate.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.pakab.chordmate.R
import de.pakab.chordmate.databinding.FragmentSongBinding
import de.pakab.chordmate.transpose
import de.pakab.chordmate.viewmodel.SongViewModel

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
    val binding get() = _binding!!

    val args: SongFragmentArgs by navArgs()

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
        _binding!!.tvSongContent.text = transpose(args.currentSong.content.orEmpty(), args.currentSong.transposing)
        requireActivity().addMenuProvider(SongFragmentMenuProvider(this), viewLifecycleOwner, Lifecycle.State.RESUMED)
        return view
    }

    fun deleteCurrentSong() {
        ViewModelProvider(this)[SongViewModel::class.java].deleteSong(args.currentSong)
        findNavController().navigate(SongFragmentDirections.actionSongFragmentToListFragment())
    }
}
