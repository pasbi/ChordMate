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
import de.pakab.chordmate.renderChordPro
import de.pakab.chordmate.viewmodel.SongViewModel

class SongFragment : Fragment() {
    private var _binding: FragmentSongBinding? = null
    public val binding get() = _binding!!

    val args: SongFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        val view = binding.root
        val toolbar = (activity as AppCompatActivity).supportActionBar!!
        toolbar.title = "${args.currentSong.title} – ${args.currentSong.interpret}"
        _binding!!.tvSongContent.text = renderChordPro(args.currentSong.content)
        requireActivity().addMenuProvider(
            object : MenuProvider {
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
                            AlertDialog
                                .Builder(
                                    requireContext(),
                                ).setTitle("Delete ${args.currentSong.title} – ${args.currentSong.interpret}?")
                                .setPositiveButton("Delete") { dialog, which -> deleteCurrentSong() }
                                .setNegativeButton("Cancel") { dialog, which -> null }
                                .create()
                                .show()
                            true
                        }
                        R.id.action_edit_song -> {
                            findNavController().navigate(
                                SongFragmentDirections.actionSongFragmentToAddFragment(currentSong = args.currentSong!!),
                            )
                            true
                        }
                        else -> false
                    }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
        return view
    }

    fun deleteCurrentSong() {
        ViewModelProvider(this)[SongViewModel::class.java].deleteSong(args.currentSong)
        findNavController().navigate(SongFragmentDirections.actionSongFragmentToListFragment())
    }
}
