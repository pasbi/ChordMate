package de.pakab.chordmate.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") var title: String? = null, // TODO consider to make all columns non-nullable.
    @ColumnInfo(name = "interpret") var interpret: String? = null,
    @ColumnInfo(name = "content") var content: String? = null,
    @ColumnInfo(name = "track_id") var trackId: String? = null,
    @ColumnInfo(name = "transposing", defaultValue = "0") var transposing: Int = 0,
) : Parcelable
