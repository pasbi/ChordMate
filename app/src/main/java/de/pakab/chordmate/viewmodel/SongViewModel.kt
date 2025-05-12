package de.pakab.chordmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.pakab.chordmate.AppDatabase
import de.pakab.chordmate.model.Song
import de.pakab.chordmate.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val readAllData: LiveData<List<Song>>
    private val repository: SongRepository

    init {
        val songDao = AppDatabase.Companion.getDatabase(application).songDao()
        repository = SongRepository(songDao)
        readAllData = repository.readAllData
    }

    fun addSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSong(song)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSong(song)
        }
    }
}
