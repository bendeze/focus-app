package com.example.focus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getSessionsForUser(userId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSessions(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: FocusSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<FocusSessionEntity>)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}
