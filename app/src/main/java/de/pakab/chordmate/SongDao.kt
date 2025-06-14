package de.pakab.chordmate

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.pakab.chordmate.model.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSong(song: Song)

    @Delete(entity = Song::class)
    suspend fun deleteSong(song: Song)

    @Update(entity = Song::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSong(song: Song)

    @Query("SELECT * FROM songs ORDER BY id ASC")
    fun readAllData(): LiveData<List<Song>>
}
