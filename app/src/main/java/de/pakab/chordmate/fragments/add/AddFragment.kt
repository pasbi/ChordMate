package de.pakab.chordmate.fragments.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.pakab.chordmate.R
import de.pakab.chordmate.Song
import de.pakab.chordmate.SongViewModel
import de.pakab.chordmate.databinding.FragmentAddBinding

class AddFragment : Fragment() {
    private lateinit var mSongViewModel: SongViewModel
    private var _binding: FragmentAddBinding? = null
    public val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        mSongViewModel = ViewModelProvider(this).get(SongViewModel::class.java)
        _binding!!.btnAdd.setOnClickListener {
            insertDataToDatabase()
        }
        return view
    }

    private fun insertDataToDatabase() {
        val title = _binding!!.etTitle.text.toString()
        mSongViewModel.addSong(Song(0, title, _binding!!.etInterpret?.text.toString()))
        Toast.makeText(requireContext(), "Added Song $title to database.", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_addFragment_to_listFragment)
    }
}
