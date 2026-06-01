package com.hamlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hamlog.data.dao.ContactDao
import com.hamlog.data.dao.DailyLogDao
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.entity.DailyLog

@Database(
    entities = [ContactRecord::class, DailyLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hamlog.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
