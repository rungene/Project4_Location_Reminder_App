package com.udacity.project4.locationreminders.data.local
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

//     Add testing implementation to the RemindersLocalRepository.kt

    // Class under test
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setupDbAndRepository() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersLocalRepository =
            RemindersLocalRepository(
                remindersDatabase.reminderDao(),TestCoroutineDispatcher()
            )
    }
    @After
    fun closeDb() = remindersDatabase.close()


    @Test
    fun insertAReminder_getAReminderFromDbById() = runBlockingTest {
        // GIVEN - insert a  reminder in the database

        val aReminder = constructAReminder("A Reminder","A Description","A Location")

      remindersLocalRepository.saveReminder(aReminder)

        val resultSaveReminder =remindersLocalRepository.getReminder(aReminder.id) as Result.Success<ReminderDTO>



        assertThat(resultSaveReminder.data, notNullValue())
        assertThat(resultSaveReminder.data.id, `is`(aReminder.id))
        assertThat(resultSaveReminder.data.title, `is`(aReminder.title))
        assertThat(resultSaveReminder.data.description, `is`(aReminder.description))
        assertThat(resultSaveReminder.data.location, `is`(aReminder.location))
        assertThat(resultSaveReminder.data.latitude, `is`(aReminder.latitude))
        assertThat(resultSaveReminder.data.longitude, `is`(aReminder.longitude))

    }

    @Test
    fun getAReminderById_reminderNotFound() = runBlocking {
        val reminder = remindersLocalRepository.getReminder("") as Result.Error

        assertThat(reminder.message, notNullValue())
        assertThat(reminder.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteReminders_GetReminders() = runBlockingTest {
        val aReminder = constructAReminder("title", "description", "my location")
        val aReminder2 = constructAReminder("title", "description", "my location")
        remindersLocalRepository.saveReminder(aReminder)
        remindersLocalRepository.saveReminder(aReminder2)
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders() as Result.Success<List<ReminderDTO>>

            assertThat(result.data, notNullValue())
            assertThat(result.data.size, `is`(0))

    }




    private fun constructAReminder(title: String, description: String, location: String): ReminderDTO {
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