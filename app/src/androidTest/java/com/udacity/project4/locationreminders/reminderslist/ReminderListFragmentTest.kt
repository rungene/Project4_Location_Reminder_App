package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeRemindersRepositoryTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest{

//   : test the navigation of the fragments.
//   : test the displayed data on the UI.
//   : add testing for the error messages.

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderDataSource


    private val testModuleToLoad = module {
        single(override = true) { repository }

    }

    @Before
    fun setUpRepository() = runBlockingTest {
        repository = FakeRemindersRepositoryTest()
        loadKoinModules(testModuleToLoad)

    }

    @After
    fun dbCleanUp() = runBlockingTest {
        unloadKoinModules(testModuleToLoad)
    }

    @Test
    fun reminderList_DisplayedInUi()= runBlockingTest {
        // GIVEN - Add active  reminder to the DB
        //Creating a Reminder.
        val reminderTest = constructAReminder("Location Testing", "A Description", "My Location")
        repository.saveReminder(reminderTest)


        // WHEN - Details fragment launched to display task
//The launchFragmentInContainer function creates a FragmentScenario, with this bundle and a theme.
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        // THEN - Reminders list are displayed on the screen
        // make sure that the title/description are both shown and correct
        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssert(1))
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))

    }



    @Test
    fun reminderList_NoDatDisplayedInUi_NoDataAvailable() = runBlockingTest {
        repository.deleteAllReminders()

    launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssert(0))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())))
    }
    @Test
    fun clickFAB_navigateToSaveReminderFragment() = runBlockingTest {
        repository.saveReminder(constructAReminder("Location Testing1", "A Description1", "My Location1"))
        repository.saveReminder(constructAReminder("Location Testing2", "A Description2", "My Location2"))

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //  Use Mockito's mock function to create a mock:
        val navController = mock(NavController::class.java)
        //Make your new mock the fragment's NavController:
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //WHEN-Click on the first list item
        //Add the code to click on the item in the RecyclerView that has the text "TITLE1"
        //RecyclerViewActions is part of the espresso-contrib library and lets you perform
        // Espresso actions on a RecyclerView.
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        //Verify that navigate was called, with the correct argument:
        // THEN - Verify that we navigate to the SaveReminderFragment screen

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder())
    }

    private fun constructAReminder(
        title: String,
        description: String,
        location: String
    ): ReminderDTO {
        return ReminderDTO(
            title = title,
            description = description,
            longitude = 123.00,
            latitude = 123.00,
            location = location
        )
    }


}