package de.pakab.chordmate.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.pakab.chordmate.AppDatabase
import de.pakab.chordmate.R
import de.pakab.chordmate.SongsAdapter
import de.pakab.chordmate.databinding.FragmentListBinding
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.viewmodel.SongViewModel

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!
    private lateinit var mSongsViewModel: SongViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        _binding!!.fabAdd.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToAddFragment(null)
            findNavController().navigate(action)
        }
        val adapter =
            SongsAdapter(
                object : SongsAdapter.OnClickListener() {
                    override fun onClick(song: Song) {
                        val action = ListFragmentDirections.actionListFragmentToSongFragment(song)
                        findNavController().navigate(action)
                    }
                },
            )
        _binding!!.rvSongs.adapter = adapter
        _binding!!.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        mSongsViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        mSongsViewModel.readAllData.observe(viewLifecycleOwner, Observer { songs -> adapter.setData(songs) })

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) {
                    menu.clear()
                    menuInflater.inflate(R.menu.menu_main, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.action_settings) {
                        Toast.makeText(requireContext(), "Settings!", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    if (menuItem.itemId == R.id.action_backup) {
                        val context = requireContext()
                        AppDatabase.getDatabase(context).backup(context)
                    }
                    if (menuItem.itemId == R.id.action_restore) {
                        val context = requireContext()
                        AppDatabase.getDatabase(context).restore(context, true)
                    }
                    return false
                }
            },
        )
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
