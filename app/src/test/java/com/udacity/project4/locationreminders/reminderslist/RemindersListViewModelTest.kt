package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.FakeFirebaseUserLiveData
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.runner.RunWith
import org.koin.android.ext.android.getKoin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.robolectric.annotation.Config
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // provide testing to the RemindersListViewModel and its live data objects
    // Executes each task synchronously using Architecture Components.
    //your add instantExecutorRule since you are testing Architecture Components.
    // Use with LiveData to ensure architechture background jobs run on the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    //since you are testing coroutines and view models
    // Changes the main dispatcher so that it does not depend on the Android Looper
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var reminderRepository: ReminderDataSource

    // Subject under test
    private lateinit var reminderListViewModel:RemindersListViewModel

    val modulesToLoad = module {
        single(override = true) { FakeFirebaseUserLiveData() as LiveData<FirebaseUser?>  }
    }

    @Before
    fun setupViewModel() = runBlocking{
        val app: MyApp = ApplicationProvider.getApplicationContext()
        reminderRepository = FakeDataSource()
        loadKoinModules(modulesToLoad)
        reminderListViewModel = RemindersListViewModel(app, reminderRepository)


    }
    @After
    fun tearDownViewModel() = runBlockingTest {
        unloadKoinModules(modulesToLoad)
        stopKoin()
    }

    @Test
    fun check_loading() = runBlockingTest {
        reminderRepository.deleteAllReminders()
        val reminder = makeReminder("Test Reminder", "A reminder to test functionality...", "Custom Location")
        reminderRepository.saveReminder(reminder)

        reminderListViewModel.loadReminders()
        val list = reminderListViewModel.remindersList.getOrAwaitValue()

        assertThat("List is not null", list, notNullValue())
        assertThat("List is not empty on loading reminders", list.isEmpty(), `is`(false))
        assertThat("Show loading is false", reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat("Show no data is false", reminderListViewModel.showNoData.getOrAwaitValue(), `is`(false))
        //assertThat()
    }

    @Test
    fun loadReminders_listEmpty() = runBlockingTest {
        reminderRepository.deleteAllReminders()

        reminderListViewModel.loadReminders()

        val list = reminderListViewModel.remindersList.getOrAwaitValue()

        assertThat("List is not null", list, notNullValue())
        assertThat("List is empty", list.isEmpty(), `is`(true))
        assertThat("Show loading is false", reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat("Show no data is true", reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun shouldReturnError() = runBlockingTest {
        reminderRepository.deleteAllReminders()
        (reminderRepository as FakeDataSource).hasErrors = true

        reminderListViewModel.loadReminders()

        val message = reminderListViewModel.showSnackBar.getOrAwaitValue()

        assertThat("Snackbar error message is shown", message, notNullValue())
        assertThat("Show no data is true", reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))

        (reminderRepository as FakeDataSource).hasErrors = false
    }

    private fun makeReminder(title: String, description: String, location: String): ReminderDTO {
        val reminder = ReminderDTO(
            title = title,
            description = description,
            longitude = 123.00,
            latitude = 123.00,
            location = location
        )
        return reminder
    }

}