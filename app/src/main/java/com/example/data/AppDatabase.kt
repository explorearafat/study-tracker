package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.StudySessionDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TaskDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Subject::class, StudySession::class, Task::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun taskDao(): TaskDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(AppDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                db.userProfileDao().insertOrUpdateProfile(
                    UserProfile(
                        id = 1,
                        name = "Alex Scholar",
                        academicLevel = "Computer Science Student",
                        motto = "Continuous learning yields lifelong mastery.",
                        targetDailyHours = 3.5f,
                        isDarkMode = false
                    )
                )

                val sub1 = db.subjectDao().insertSubject(
                    Subject(
                        name = "Computer Science",
                        category = "STEM",
                        colorHex = 0xFF4F46E5,
                        targetDailyMinutes = 90,
                        iconName = "Computer"
                    )
                ).toInt()

                val sub2 = db.subjectDao().insertSubject(
                    Subject(
                        name = "Mathematics",
                        category = "STEM",
                        colorHex = 0xFF0284C7,
                        targetDailyMinutes = 60,
                        iconName = "Calculator"
                    )
                ).toInt()

                val sub3 = db.subjectDao().insertSubject(
                    Subject(
                        name = "Physics",
                        category = "STEM",
                        colorHex = 0xFF059669,
                        targetDailyMinutes = 45,
                        iconName = "Science"
                    )
                ).toInt()

                val sub4 = db.subjectDao().insertSubject(
                    Subject(
                        name = "English Literature",
                        category = "Humanities",
                        colorHex = 0xFFD97706,
                        targetDailyMinutes = 30,
                        iconName = "Book"
                    )
                ).toInt()

                db.taskDao().insertTask(
                    Task(
                        subjectId = sub1,
                        title = "Implement Binary Search Tree assignment",
                        description = "Complete unit tests and benchmark analysis.",
                        priority = "High",
                        isCompleted = false
                    )
                )
                db.taskDao().insertTask(
                    Task(
                        subjectId = sub2,
                        title = "Solve Multivariable Calculus Chapter 4 Exercises",
                        description = "Focus on double integrals and surface area calculations.",
                        priority = "High",
                        isCompleted = false
                    )
                )
                db.taskDao().insertTask(
                    Task(
                        subjectId = sub3,
                        title = "Review Quantum Mechanics Lecture Notes",
                        description = "Prepare summary flashcards for Friday quiz.",
                        priority = "Medium",
                        isCompleted = false
                    )
                )
                db.taskDao().insertTask(
                    Task(
                        subjectId = sub4,
                        title = "Draft 500-word essay on Modernist Poetry",
                        description = "Cite references and proofread for final submission.",
                        priority = "Low",
                        isCompleted = false
                    )
                )

            }
        }
    }
}
