package de.pakab.chordmate.repository

import androidx.lifecycle.LiveData
import de.pakab.chordmate.SongDao
import de.pakab.chordmate.model.Song

class SongRepository(
    private val songDao: SongDao,
) {
    val readAllData: LiveData<List<Song>> = songDao.readAllData()

    suspend fun addSong(song: Song) {
        songDao.addSong(song)
    }

    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }

    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }
}
