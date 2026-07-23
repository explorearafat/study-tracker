package com.example.data.dao

import androidx.room.*
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getSessionsBySubjectId(subjectId: Int): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE timestamp >= :startTimeMs AND timestamp <= :endTimeMs ORDER BY timestamp DESC")
    fun getSessionsInTimeRange(startTimeMs: Long, endTimeMs: Long): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)
}
