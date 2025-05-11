package de.pakab.chordmate

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSong(song: Song)

    @Query("SELECT * FROM songs ORDER BY id ASC")
    fun readAllData(): LiveData<List<Song>>
}
