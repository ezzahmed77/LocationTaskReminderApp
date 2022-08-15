package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reminderLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminderAndGetReminder() = runBlocking {
        // GIVEN - A new task saved in the database.
        val newReminder = ReminderDTO("title", "description", "LocationOfReminder",
        32.2, 538.9, "Id_of_new_reminder")
        reminderLocalRepository.saveReminder(newReminder)

        // WHEN  - Task retrieved by ID.
        val result = reminderLocalRepository.getReminder(newReminder.id)

        // THEN - Same task is returned.
        result as Result.Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("LocationOfReminder"))
        assertThat((result.data.latitude), `is`(32.2))
        assertThat((result.data.longitude), `is`(538.9))
        assertThat((result.data.id), `is`("Id_of_new_reminder"))
    }

    // For Error Testing
    // Try to add reminder and get it with fakeId
    // Make share the result will be Result.Error

    @Test
    fun saveReminderAndTryToGetItWithFakeID() = runBlocking{
        // GIVEN - A new task saved in the database.
        val newReminder = ReminderDTO("title", "description", "LocationOfReminder",
            32.2, 538.9, "Id_of_new_reminder")
        reminderLocalRepository.saveReminder(newReminder)

        // WHEN  - Task retrieved by ID.
        val result = reminderLocalRepository.getReminder("Fake_Reminder_ID")

        // THEN the result will be Result.Error
        // And the reminder will not be found
        result as Result.Error
        assertThat(result).isEqualTo(Result.Error(message="Reminder not found!", statusCode=null))

    }





}