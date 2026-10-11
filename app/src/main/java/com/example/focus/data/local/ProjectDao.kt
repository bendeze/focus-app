package com.example.focus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getProjectsForUser(userId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingProjects(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProjects(projects: List<ProjectEntity>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("DELETE FROM projects")
    suspend fun clearAll()
}
