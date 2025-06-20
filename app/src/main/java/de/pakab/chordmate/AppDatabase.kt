package de.pakab.chordmate

import android.content.Context
import android.content.Intent
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.pakab.chordmate.model.Song
import okio.IOException
import java.io.File

const val DATABASE_NAME = "song-database"
const val DATABASE_BACKUP_SUFFIX = "-bkp"
const val SQLITE_WALFILE_SUFFIX = "-wal"
const val SQLITE_SHMFILE_SUFFIX = "-shm"

@Database(
    entities = [Song::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return instance ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DATABASE_NAME,
                        ).build()
                this.instance = instance
                // return instance
                instance
            }
        }
    }

    fun backup(context: Context): Boolean {
        if (instance == null) {
            return false
        }
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WALFILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHMFILE_SUFFIX)
        val bkpFile = File(dbFile.path + DATABASE_BACKUP_SUFFIX)
        val bkpWalFile = File(bkpFile.path + SQLITE_WALFILE_SUFFIX)
        val bkpShmFile = File(bkpFile.path + SQLITE_SHMFILE_SUFFIX)
        if (bkpFile.exists()) bkpFile.delete()
        if (bkpWalFile.exists()) bkpWalFile.delete()
        if (bkpShmFile.exists()) bkpShmFile.delete()
        checkpoint()
        try {
            dbFile.copyTo(bkpFile, true)
            if (dbWalFile.exists()) dbWalFile.copyTo(bkpWalFile, true)
            if (dbShmFile.exists()) dbShmFile.copyTo(bkpShmFile, true)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun restore(
        context: Context,
        restart: Boolean,
    ) {
        if (!File(context.getDatabasePath(DATABASE_NAME).path + DATABASE_BACKUP_SUFFIX).exists()) {
            return
        }
        if (instance == null) {
            return
        }

        val dbpath = instance!!.openHelper.readableDatabase.path
        val dbFile = File(dbpath!!)
        val dbWalFile = File(dbFile.path + SQLITE_WALFILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHMFILE_SUFFIX)
        val bkpFile = File(dbFile.path + DATABASE_BACKUP_SUFFIX)
        val bkpWalFile = File(bkpFile.path + SQLITE_WALFILE_SUFFIX)
        val bkpShmFile = File(bkpFile.path + SQLITE_SHMFILE_SUFFIX)
        try {
            bkpFile.copyTo(dbFile, true)
            if (bkpWalFile.exists()) bkpWalFile.copyTo(dbWalFile, true)
            if (bkpShmFile.exists()) bkpShmFile.copyTo(dbShmFile, true)
            checkpoint()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (restart) {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
            System.exit(0)
        }
    }

    private fun checkpoint() {
        val db = openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);")
        db.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }
}
