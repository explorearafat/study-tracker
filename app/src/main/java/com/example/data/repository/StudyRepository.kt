package com.example.data.repository

import com.example.data.dao.StudySessionDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TaskDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val subjectDao: SubjectDao,
    private val sessionDao: StudySessionDao,
    private val taskDao: TaskDao,
    private val userProfileDao: UserProfileDao
) {
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allSessions: Flow<List<StudySession>> = sessionDao.getAllSessions()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getSubjectById(id: Int): Subject? = subjectDao.getSubjectById(id)

    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    suspend fun deleteSubjectById(id: Int) = subjectDao.deleteSubjectById(id)

    fun getSessionsBySubjectId(subjectId: Int): Flow<List<StudySession>> =
        sessionDao.getSessionsBySubjectId(subjectId)

    fun getSessionsInTimeRange(startTimeMs: Long, endTimeMs: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsInTimeRange(startTimeMs, endTimeMs)

    suspend fun insertSession(session: StudySession): Long = sessionDao.insertSession(session)

    suspend fun deleteSession(session: StudySession) = sessionDao.deleteSession(session)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun getUserProfileDirect(): UserProfile? = userProfileDao.getUserProfileDirect()

    suspend fun saveUserProfile(profile: UserProfile) = userProfileDao.insertOrUpdateProfile(profile)
}
