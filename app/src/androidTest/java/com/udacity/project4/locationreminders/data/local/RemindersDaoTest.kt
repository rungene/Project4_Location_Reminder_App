package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    : Add testing implementation to the RemindersDao.kt
@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase


    @Before
    fun initializeDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    @After
    fun closeDb() = database.close()

    @Test
    fun insertAReminderAndGetById() = runBlockingTest {
        // Given that a task is inserted
        val reminderTest = constructAReminder("Location Testing", "A Description", "My Location")
        database.reminderDao().saveReminder(reminderTest)

        // WHEN - Get the reminder by id from the database.
        val loadedReminder = database.reminderDao().getReminderById(reminderTest.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminderTest.id))
        assertThat(loadedReminder.title, `is`(reminderTest.title))
        assertThat(loadedReminder.description, `is`(reminderTest.description))
        assertThat(loadedReminder.longitude, `is`(reminderTest.longitude))
        assertThat(loadedReminder.latitude, `is`(reminderTest.latitude))
        assertThat(loadedReminder.location, `is`(reminderTest.location))
    }

    @Test
    fun deleteAReminderItem() = runBlockingTest {
        // sample data for testing
        val reminderTest = constructAReminder("Location Testing", "A Description", "My Location")

        database.reminderDao().saveReminder(reminderTest)
        database.reminderDao().deleteReminder(reminderTest)

        val loadedReminder:ReminderDTO? = database.reminderDao().getReminderById(reminderTest.id)



        assertThat(loadedReminder, nullValue())
    }



    @Test
    fun insertRemindersAndGetReminders() = runBlocking {
        val firstReminder = constructAReminder("Location Testing", "A Description", "My Location")
        val secondReminder = constructAReminder("2nd Location Testing", "2nd Description", "2nd My Location")
        database.reminderDao().saveReminder(firstReminder)
        database.reminderDao().saveReminder(secondReminder)

        val loadedReminders = database.reminderDao().getReminders()

        assertThat(loadedReminders.isNotEmpty(), `is`(true))

    }





    @Test
    fun insertAReminderAndRemoveAllReminders() = runBlockingTest {
        // Given that you have one reminder in the database, delete all Reminders
        val reminderTest = constructAReminder("Location Testing", "A Description", "My Location")
        database.reminderDao().saveReminder(reminderTest)
        database.reminderDao().deleteAllReminders()

        val loadedReminders = database.reminderDao().getReminders()

        assertThat(loadedReminders.isEmpty(), `is`(true))
    }

    private fun constructAReminder(
        title: String,
        description: String,
        location: String
    ): ReminderDTO {
        return ReminderDTO(
            title = title,
            description = description,
            longitude = 22.647596,
            latitude = 22.647596,
            location = location
        )
    }


}