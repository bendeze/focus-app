package com.example.focus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FocusDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getInstance(context: Context): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_app.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
