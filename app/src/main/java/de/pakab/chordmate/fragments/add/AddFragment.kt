package de.pakab.chordmate.fragments.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import de.pakab.chordmate.R
import de.pakab.chordmate.databinding.FragmentAddBinding
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.viewmodel.SongViewModel
import kotlin.getValue

class AddFragment : Fragment() {
    private lateinit var mSongViewModel: SongViewModel
    private var _binding: FragmentAddBinding? = null
    public val binding get() = _binding!!
    val args: AddFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        if (args.currentSong != null) {
            _binding!!.etTitle.setText(args.currentSong!!.title)
            _binding!!.etInterpret.setText(args.currentSong!!.interpret)
//            _binding!!.etContent.setText(args.currentSong.content)
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
        val currentSong = Song(0, title, _binding!!.etInterpret!!.text.toString())
//        val currentSong = Song(0, title, _binding!!.etInterpret!!.text.toString(), _binding!!.etContent!!.text.toString())
        mSongViewModel.addSong(currentSong)
        Toast.makeText(requireContext(), "Added Song $title to database.", Toast.LENGTH_LONG).show()

        val action = AddFragmentDirections.actionAddFragmentToSongFragment(currentSong)
        findNavController().navigate(action, navOptions { popUpTo(R.id.listFragment) })
    }

    private fun updateCurrentSong() {
    }
}
