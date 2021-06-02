package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.getOrAwaitValueForTest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //: provide testing to the SaveReminderView and its live data objects


    // Changes the main dispatcher so that it does not depend on the Android Looper
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Use with LiveData to ensure architechture background jobs run on the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersRepository: ReminderDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel




    @Before
    fun setUp()= runBlockingTest {
        remindersRepository=FakeDataSource()


        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown()= runBlockingTest {

        stopKoin()
    }


    @Test
    fun saveReminder_validateValidAndInvalid_returnsValidAndInvalidReminders() = runBlockingTest {
//valid data entered
        val validReminder = ReminderDataItem(
            title = "Valid Reminder",
            description = null,
            longitude = null,
            latitude = null,
            location = "Valid Reminder Location"
        )


// invalid data -location missing
        val invalidReminder = ReminderDataItem(
            title = "Valid Title",
            description = null,
            longitude = null,
            latitude = null,
            location = null
        )
        // invalid data -Title missing
        val invalidReminder2nd = ReminderDataItem(
            title = null,
            description = null,
            longitude = null,
            latitude = null,
            location = "Valid Location"
        )

        val inValidReminderResult = saveReminderViewModel.validateEnteredData(invalidReminder)
        assertThat(inValidReminderResult, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValueForTest(), notNullValue())
        saveReminderViewModel.showSnackBarInt.value = null

     val validReminderResult =  saveReminderViewModel.validateEnteredData(validReminder)
        assertThat(validReminderResult, `is`(true))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValueForTest(), IsNull())
        saveReminderViewModel.showSnackBarInt.value = null



        val invalidReminder2ndResult = saveReminderViewModel.validateEnteredData(invalidReminder2nd)
        assertThat(invalidReminder2ndResult, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValueForTest(), notNullValue())
        saveReminderViewModel.showSnackBarInt.value = null



    }


    @Test
    fun saveReminder_saveValidReminder_returnsTrue() = runBlockingTest {

        val reminderDataItem = ReminderDataItem(
            "Location",
            "Description",
            "location",
            11.647596,
            55.645856,
            "123456"
        )


        mainCoroutineRule.testCoroutineDispatcher.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)


        assertThat(saveReminderViewModel.showLoading.getOrAwaitValueForTest(),`is`(true))
        mainCoroutineRule.testCoroutineDispatcher.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValueForTest(),`is`(false))

        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValueForTest(),IsInstanceOf(NavigationCommand.Back::class.java))

    saveReminderViewModel.onClear()
    }


}