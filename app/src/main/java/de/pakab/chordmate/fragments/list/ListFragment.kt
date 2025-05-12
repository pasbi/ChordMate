package de.pakab.chordmate.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.pakab.chordmate.R
import de.pakab.chordmate.SongsAdapter
import de.pakab.chordmate.databinding.FragmentListBinding
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.viewmodel.SongViewModel

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    public val binding get() = _binding!!
    private lateinit var mSongsViewModel: SongViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        _binding!!.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
