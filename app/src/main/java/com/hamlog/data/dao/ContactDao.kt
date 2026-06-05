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

    @Query("SELECT DISTINCT dateEpochDay FROM contacts WHERE dateEpochDay IS NOT NULL ORDER BY dateEpochDay DESC")
    fun getAllDates(): Flow<List<Long>>

    @Query("SELECT dateEpochDay, COUNT(*) as count FROM contacts GROUP BY dateEpochDay ORDER BY dateEpochDay DESC")
    fun getAllDatesWithCount(): Flow<List<DateCount>>

    @Query("SELECT COUNT(*) FROM contacts")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT DISTINCT callsign FROM contacts WHERE callsign IS NOT NULL ORDER BY callsign ASC")
    suspend fun getDistinctCallsigns(): List<String>

    @Query("SELECT DISTINCT callsign FROM contacts WHERE callsign LIKE :prefix || '%' ORDER BY callsign ASC LIMIT 8")
    suspend fun searchCallsigns(prefix: String): List<String>

    @Query("SELECT DISTINCT callsign,frequencyMHz,mode FROM contacts WHERE callsign = :callsign ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastContactByCallsign(callsign: String): LastContactInfo?

    @Query("SELECT * FROM contacts WHERE callsign = :callsign ORDER BY createdAt DESC")
    fun getContactsByCallsign(callsign: String): Flow<List<ContactRecord>>

    @Query("SELECT * FROM contacts WHERE callsign LIKE :prefix || '%' ORDER BY dateEpochDay DESC, createdAt DESC")
    fun searchContactsByCallsignPrefix(prefix: String): Flow<List<ContactRecord>>

    @Query("SELECT dateEpochDay, COUNT(*) as count FROM contacts WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay GROUP BY dateEpochDay ORDER BY dateEpochDay ASC")
    fun getDateCountsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DateCount>>

    @Query("SELECT mode, COUNT(*) as count FROM contacts WHERE mode IS NOT NULL AND mode != '' GROUP BY mode ORDER BY count DESC")
    fun getModeDistribution(): Flow<List<ModeCount>>

    @Query("SELECT * FROM contacts ORDER BY createdAt ASC")
    fun getAllContactsForStats(): Flow<List<ContactRecord>>

    @Query("SELECT MIN(dateEpochDay) FROM contacts")
    fun getFirstContactDate(): Flow<Long?>

    @Query("SELECT MAX(dateEpochDay) FROM contacts")
    fun getLastContactDate(): Flow<Long?>

    @Query("SELECT COUNT(DISTINCT callsign) FROM contacts WHERE callsign IS NOT NULL AND callsign != ''")
    fun getDistinctCallsignCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT dateEpochDay) FROM contacts")
    fun getActiveDaysCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactRecord): Long

    @Delete
    suspend fun delete(contact: ContactRecord)

    @Update
    suspend fun update(contact: ContactRecord)
}

data class LastContactInfo(
    val callsign: String,
    val frequencyMHz: Double,
    val mode: String
)
data class DateCount(
    val dateEpochDay: Long,
    val count: Int
)
data class ModeCount(
    val mode: String,
    val count: Int
)