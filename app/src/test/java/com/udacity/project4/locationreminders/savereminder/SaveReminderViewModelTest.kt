package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    // For Testing LiveData
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun saveReminder_CheckItIsSaved()= runBlockingTest{
        // GIVEN saveReminderViewModel with fake dataSource
        val dataSource = FakeDataSource()
        val saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        // WHEN saving the reminder
        val reminder = ReminderDataItem("title", "description", "location", 32.2, 53.2, "id_of_reminder")
        saveReminderViewModel.saveReminder(reminder)

        val reminderTitle= saveReminderViewModel.reminderTitle
        val reminderDescription= saveReminderViewModel.reminderDescription
        val reminderLocation= saveReminderViewModel.reminderSelectedLocationStr
        val reminderLatitude= saveReminderViewModel.latitude
        val reminderLongitude = saveReminderViewModel.longitude

        // THEN liveData not equal to null
        assertThat(reminderTitle).isNotNull()
        assertThat(reminderDescription).isNotNull()
        assertThat(reminderLocation).isNotNull()
        assertThat(reminderLatitude).isNotNull()
        assertThat(reminderLongitude).isNotNull()
    }


}