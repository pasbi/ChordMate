package de.pakab.chordmate

import androidx.lifecycle.LiveData

class SongRepository(
    private val songDao: SongDao,
) {
    val readAllData: LiveData<List<Song>> = songDao.readAllData()

    suspend fun addSong(song: Song) {
        songDao.addSong(song)
    }
}
