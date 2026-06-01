package com.hamlog.data.dao

import androidx.room.*
import com.hamlog.data.entity.ContactRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt DESC")
    fun getContactsByDate(dateEpochDay: Long): Flow<List<ContactRecord>>

    @Query("SELECT * FROM contacts ORDER BY dateEpochDay DESC, createdAt DESC")
    fun getAllContacts(): Flow<List<ContactRecord>>

    @Query("SELECT COUNT(*) FROM contacts WHERE dateEpochDay = :dateEpochDay")
    fun getContactCountByDate(dateEpochDay: Long): Flow<Int>

    @Query("SELECT DISTINCT dateEpochDay FROM contacts ORDER BY dateEpochDay DESC")
    fun getAllDates(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM contacts")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactRecord): Long

    @Delete
    suspend fun delete(contact: ContactRecord)

    @Update
    suspend fun update(contact: ContactRecord)
}
