package com.kriptic.app.map

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

class MarkerTypeConverters {
    @TypeConverter
    fun fromMarkerType(type: MarkerType): String = type.name

    @TypeConverter
    fun toMarkerType(value: String): MarkerType = MarkerType.fromWireValue(value) ?: MarkerType.GATHER
}

@Dao
interface MarkerDao {
    @Query("SELECT * FROM markers WHERE expiresAt > :nowMs ORDER BY timestamp DESC")
    fun observeActive(nowMs: Long): Flow<List<Marker>>

    @Query("SELECT * FROM markers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Marker?

    @Query("SELECT * FROM markers WHERE expiresAt > :nowMs")
    suspend fun getActiveSnapshot(nowMs: Long): List<Marker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(marker: Marker)

    @Query("DELETE FROM markers WHERE expiresAt <= :nowMs")
    suspend fun deleteExpired(nowMs: Long)

    /** Used by panic wipe — see security/PanicWipeManager.kt. */
    @Query("DELETE FROM markers")
    suspend fun clearAll()
}

@Database(entities = [Marker::class], version = 1, exportSchema = false)
@TypeConverters(MarkerTypeConverters::class)
abstract class MarkerDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao

    companion object {
        @Volatile private var instance: MarkerDatabase? = null

        fun getInstance(context: Context): MarkerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MarkerDatabase::class.java,
                    "kriptic_markers.db",
                ).build().also { instance = it }
            }

        /** Drops and recreates the DB file — used only by panic wipe. */
        fun destroyInstance(context: Context) {
            synchronized(this) {
                instance?.close()
                instance = null
                context.applicationContext.deleteDatabase("kriptic_markers.db")
            }
        }
    }
}
