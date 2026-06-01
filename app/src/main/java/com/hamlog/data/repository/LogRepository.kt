package com.hamlog.data.repository

import com.hamlog.data.AppDatabase
import com.hamlog.data.entity.ContactRecord
import com.hamlog.data.entity.DailyLog
import kotlinx.coroutines.flow.Flow

class LogRepository(private val database: AppDatabase) {

    private val contactDao = database.contactDao()
    private val dailyLogDao = database.dailyLogDao()

    fun getContactsByDate(dateEpochDay: Long): Flow<List<ContactRecord>> =
        contactDao.getContactsByDate(dateEpochDay)

    fun getAllContacts(): Flow<List<ContactRecord>> =
        contactDao.getAllContacts()

    fun getContactCountByDate(dateEpochDay: Long): Flow<Int> =
        contactDao.getContactCountByDate(dateEpochDay)

    fun getAllDates(): Flow<List<Long>> =
        contactDao.getAllDates()

    fun getTotalCount(): Flow<Int> =
        contactDao.getTotalCount()

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
}
