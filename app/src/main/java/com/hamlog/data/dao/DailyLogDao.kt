package com.hamlog.data.dao

import androidx.room.*
import com.hamlog.data.entity.DailyLog

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :dateEpochDay")
    suspend fun getByDate(dateEpochDay: Long): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyLog: DailyLog)
}
