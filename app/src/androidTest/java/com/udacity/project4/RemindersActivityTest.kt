package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.ToastMatcher
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


//     add End to End testing to the app

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }
    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun remindersFragment_clickFab_locationNotSelected_showSnackBar() = runBlocking {
        // Start up RemindersActivity screen.

        //there is an ActivityScenarioRule which calls launch and close for you.
        /*     any setup of the data state, such as adding reminders to the repository, must happen before
             ActivityScenario.launch() is called. Calling such additional setup code, such as saving
             reminders to the repository, is not currently supported by ActivityScenarioRule. Therefore,
             we choose not to use ActivityScenarioRule and instead manually call launch and close.*/
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.
        // Click on the task on the FAB and verify that all the data is correct.
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Add title1"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Add description1"))


        onView(withId(R.id.saveReminder)).perform(click())

        val snackBarErrorMessage = appContext.getString(R.string.select_location)

        onView(withText(snackBarErrorMessage))
            .check(matches((isDisplayed())))


        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun remindersFragment_clickFab_saveReminderFragment() = runBlocking {
        // Start up RemindersActivity screen.

        //there is an ActivityScenarioRule which calls launch and close for you.
        /*     any setup of the data state, such as adding reminders to the repository, must happen before
             ActivityScenario.launch() is called. Calling such additional setup code, such as saving
             reminders to the repository, is not currently supported by ActivityScenarioRule. Therefore,
             we choose not to use ActivityScenarioRule and instead manually call launch and close.*/
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.
        // Click on the task on the FAB and verify that all the data is correct.
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Add title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Add description"))

        onView(withId(R.id.selectLocation)).perform(click())

        // Click on the saveLocationButton button and save reminder.
        onView(withId(R.id.locationSaveButton)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        //confirm toast is displayed
      onView(withText(R.string.geofences_added)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
        // Verify a reminder is displayed on screen in the reminder list fragment.
        onView(withText("Add title")).check(matches(isDisplayed()))

    /*    onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.permission_denied_explanation)))*/


        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }




}
