package de.pakab.chordmate.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "interpret") var interpret: String?,
    @ColumnInfo(name = "content") var content: String?,
    @ColumnInfo(name = "track_id") var trackId: String? = null,
    @ColumnInfo(name = "transposing", defaultValue = "0") var transposing: Int = 0,
) : Parcelable
