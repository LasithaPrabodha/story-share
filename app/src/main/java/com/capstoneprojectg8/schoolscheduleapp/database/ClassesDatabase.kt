package com.capstoneprojectg8.schoolscheduleapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.capstoneprojectg8.schoolscheduleapp.models.Class
import com.capstoneprojectg8.schoolscheduleapp.models.ScheduleSlot

@Database(entities = [Class::class, ScheduleSlot::class], version = 3)
abstract class ClassesDatabase : RoomDatabase() {

    abstract fun getClassesDao(): ClassesDao
    abstract fun getClassSlotDao(): ClassSlotDao

    companion object {
        @Volatile
        private var instance: ClassesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance
                    ?: createDatabase(context).also {
                        instance = it
                    }
            }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ClassesDatabase::class.java,
                "class_db"
            ).fallbackToDestructiveMigration().build()
    }
}