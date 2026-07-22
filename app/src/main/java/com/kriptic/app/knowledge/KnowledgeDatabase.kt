package com.kriptic.app.knowledge

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Query(
        """
        SELECT rowid, * FROM knowledge_entries
        WHERE knowledge_entries MATCH :ftsQuery
        """
    )
    fun search(ftsQuery: String): Flow<List<KnowledgeEntry>>

    @Query("SELECT rowid, * FROM knowledge_entries WHERE domain = :domain OR :domain = ''")
    fun observeAll(domain: String = ""): Flow<List<KnowledgeEntry>>

    @Query("SELECT rowid, * FROM knowledge_entries WHERE entryId = :entryId LIMIT 1")
    suspend fun getByEntryId(entryId: String): KnowledgeEntry?

    @Query("SELECT COUNT(*) FROM knowledge_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<KnowledgeEntry>)
}

@Database(entities = [KnowledgeEntry::class], version = 1, exportSchema = false)
abstract class KnowledgeDatabase : RoomDatabase() {
    abstract fun knowledgeDao(): KnowledgeDao

    companion object {
        @Volatile private var instance: KnowledgeDatabase? = null

        fun getInstance(context: Context): KnowledgeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KnowledgeDatabase::class.java,
                    "kriptic_knowledge.db",
                ).build().also { instance = it }
            }
    }
}
