package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.android.gms.tasks.Task
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a Reminder.
        val reminder = ReminderDTO("title", "description", "Location", 12.0, 13.0, "this_is_id_for_test")
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.location, `is`(reminder.location))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
    }



}