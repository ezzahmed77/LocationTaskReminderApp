package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var dataSource: FakeDataSource

    @Before
    fun setUp(){
        stopKoin()
        dataSource = FakeDataSource()
    }

    @After
     fun tearDown() = runBlocking{
        dataSource.deleteAllReminders()
    }

    // For Testing LiveData
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadAllReminders_CheckTheyAreLoaded(){
        // GIVEN ReminderListViewModel
        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // WHEN loading reminders
        remindersListViewModel.loadReminders()

        val reminderList = remindersListViewModel.remindersList

        // THEN reminders are loaded
        assertThat(reminderList).isNotNull()

    }

    // Here the test of live data using shouldReturnError and checkLoading

    @Test
    fun loadAllReminder_MakeError_CheckTheyAreNotLoaded(){
        // GIVEN ReminderListViewModel
        // testing that the result will be Result.Error
        dataSource.setShouldReturnError(true)
        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // Get the reminderList
        remindersListViewModel.loadReminders()
        val reminderList = remindersListViewModel.remindersList

        // Make sure it is null
        assertThat(reminderList.value).isNull()
    }

    // Testing using showLoadingStating
    // so showLoading is first set to true in loadReminder function in reminderListViewModel
    // So we will check if it is false this means the data has been loaded whether it succeeded or not
    // So i will make 2 test function the first is to check that data has been correctly loaded and the
    // reminderList is not null
    // and the second that it hasn't been loaded and the reminderList is null

    @Test
    fun checkLoading_ReminderListAreCorrectlyLoaded()  {
        // GIVEN ReminderListViewModel
        // testing that the result will be Result.Success
        dataSource.setShouldReturnError(false)

        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // WHEN loading reminder from fakeDataSource
        remindersListViewModel.loadReminders()

        val reminderList = remindersListViewModel.remindersList

        // THEN   the value of showLoading will be false, and the are correctly loaded
        assertThat(reminderList).isNotNull()
        assertThat(remindersListViewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun checkLoading_ReminderListAreNotLoaded()  {
        // GIVEN ReminderListViewModel
        // testing that the result will be Result.Error
        dataSource.setShouldReturnError(true)

        val remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

        // WHEN loading reminder from fakeDataSource
        remindersListViewModel.loadReminders()

        val reminderList = remindersListViewModel.remindersList

        // THEN   the value of showLoading will be false, and the are not correctly loaded
        assertThat(reminderList.value).isNull()
        assertThat(remindersListViewModel.showLoading.value).isEqualTo(false)
    }

}