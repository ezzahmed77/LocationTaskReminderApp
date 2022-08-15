package com.udacity.project4.locationreminders.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // Getting reminderDao to add or delete reminder
    // Building database in memory as it is fake one
    private val remindersDao = createRemindersDao(ApplicationProvider.getApplicationContext())

   private fun createRemindersDao(context: Context): RemindersDao {
        return Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java
        ).build().reminderDao()
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.Success(remindersDao.getReminders())
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO)  {
        withContext(Dispatchers.IO){
            remindersDao.saveReminder(reminder)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> = withContext(Dispatchers.IO) {
        try {
            val reminder = remindersDao.getReminderById(id)
            if (reminder != null) {
                return@withContext Result.Success(reminder)
            } else {
                return@withContext Result.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e.localizedMessage)
        }

    }

    override suspend fun deleteAllReminders() {
        withContext(Dispatchers.IO){
            remindersDao.deleteAllReminders()
        }
    }
}