package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValueForTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var reminderListViewModel: RemindersListViewModel

    private val repositoryReminder = FakeDataSource()

    @Before
    fun setUp() {
        val app: MyApp = ApplicationProvider.getApplicationContext()
        reminderListViewModel = RemindersListViewModel(app,repositoryReminder)
    }


    @Test
    fun listReminders_loadAllReminders_returnReminderList() = runBlockingTest {
        val reminder = buildReminder( "Testing location","Sample description","location name")


        repositoryReminder.saveReminder(reminder)
        reminderListViewModel.loadReminders()

        val listOfReminders = reminderListViewModel.remindersList.getOrAwaitValueForTest ()

        assertThat("List is not null", listOfReminders, notNullValue())


    }

    @Test
    fun loadRemindersWhenAreUnavailable_shouldReturnError() = runBlocking {
        repositoryReminder.deleteAllReminders()
        repositoryReminder.hasErrors =true
        reminderListViewModel.loadReminders()

        val snackbarMessage = reminderListViewModel.showSnackBar.getOrAwaitValueForTest ()
        val showNoData = reminderListViewModel.showNoData.getOrAwaitValueForTest()

        assertThat("Error Message Is Displayed",snackbarMessage, notNullValue())
        assertThat("Not Data is shown",showNoData, notNullValue())

        repositoryReminder.hasErrors = false

    }



    private fun buildReminder(title: String, description: String, location: String): ReminderDTO {
        val reminder = ReminderDTO(
            title = title,
            description = description,
            longitude = 22.647596,
            latitude = 88.645856,
            location = location,
            id = "12459"

        )
        return reminder
    }

}