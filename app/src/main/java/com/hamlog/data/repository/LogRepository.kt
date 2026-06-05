package com.hamlog.data.repository

import com.hamlog.data.AppDatabase
import com.hamlog.data.dao.DateCount
import com.hamlog.data.dao.LastContactInfo
import com.hamlog.data.dao.ModeCount
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.entity.DailyLog
import kotlinx.coroutines.flow.Flow

class LogRepository(private val database: AppDatabase) {

    private val contactDao = database.contactDao()
    private val dailyLogDao = database.dailyLogDao()

    fun getContactsByDate(dateEpochDay: Long): Flow<List<ContactRecord>> =
        contactDao.getContactsByDate(dateEpochDay)

    fun getContactsByCallsign(callsign: String): Flow<List<ContactRecord>> =
        contactDao.getContactsByCallsign(callsign)

    fun getAllContacts(): Flow<List<ContactRecord>> =
        contactDao.getAllContacts()

    fun getContactCountByDate(dateEpochDay: Long): Flow<Int> =
        contactDao.getContactCountByDate(dateEpochDay)

    fun getAllDates(): Flow<List<Long>> =
        contactDao.getAllDates()

    fun getAllDatesWithCount(): Flow<List<DateCount>> =
        contactDao.getAllDatesWithCount()

    fun getTotalCount(): Flow<Int> =
        contactDao.getTotalCount()

    suspend fun getDistinctCallsigns(): List<String> =
        contactDao.getDistinctCallsigns()

    fun searchContactsByCallsignPrefix(prefix: String): Flow<List<ContactRecord>> =
        contactDao.searchContactsByCallsignPrefix(prefix)

    suspend fun searchCallsigns(prefix: String): List<String> =
        contactDao.searchCallsigns(prefix)

    suspend fun getLastContactByCallsign(callsign: String): LastContactInfo? =
        contactDao.getLastContactByCallsign(callsign)

    suspend fun insertContact(contact: ContactRecord): Long =
        contactDao.insert(contact)

    suspend fun deleteContact(contact: ContactRecord) =
        contactDao.delete(contact)

    suspend fun updateContact(contact: ContactRecord) =
        contactDao.update(contact)

    suspend fun getDailyLog(dateEpochDay: Long): DailyLog? =
        dailyLogDao.getByDate(dateEpochDay)

    suspend fun insertDailyLog(dailyLog: DailyLog) =
        dailyLogDao.insert(dailyLog)

    fun getDateCountsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DateCount>> =
        contactDao.getDateCountsInRange(startEpochDay, endEpochDay)

    fun getModeDistribution(): Flow<List<ModeCount>> =
        contactDao.getModeDistribution()

    fun getAllContactsForStats(): Flow<List<ContactRecord>> =
        contactDao.getAllContactsForStats()

    fun getFirstContactDate(): Flow<Long?> = contactDao.getFirstContactDate()

    fun getLastContactDate(): Flow<Long?> = contactDao.getLastContactDate()

    fun getDistinctCallsignCount(): Flow<Int> = contactDao.getDistinctCallsignCount()

    fun getActiveDaysCount(): Flow<Int> = contactDao.getActiveDaysCount()
}
