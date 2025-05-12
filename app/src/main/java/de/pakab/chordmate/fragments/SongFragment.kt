package de.pakab.chordmate.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.pakab.chordmate.databinding.FragmentSongBinding

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

        _binding!!.tvSongInterpret.text = args.currentSong.interpret
        _binding!!.tvSongTitle.text = args.currentSong.title

        return view
    }
}
