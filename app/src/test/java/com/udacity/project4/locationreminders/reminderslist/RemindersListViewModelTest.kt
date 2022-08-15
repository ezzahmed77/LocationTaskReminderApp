package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    // For Testing LiveData
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadAllReminders_CheckTheyAreLoaded(){
        // GIVEN ReminderListViewModel
        val dataSource = FakeDataSource()
        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // WHEN loading reminders
        remindersListViewModel.loadReminders()

        val reminderList = remindersListViewModel.remindersList.getOrAwaitValue()

        // THEN reminders are loaded
        assertThat(reminderList).isNotNull()

    }

}