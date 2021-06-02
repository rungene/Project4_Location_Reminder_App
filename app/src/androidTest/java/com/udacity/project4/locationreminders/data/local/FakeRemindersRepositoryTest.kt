package com.udacity.project4.locationreminders.data.local


import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeRemindersRepositoryTest : ReminderDataSource {
    //     Create a fake data source to act as a double to the real data source
    companion object {
        const val ERROR_MESSAGE = "Error!"
    }
    private var reminderDTO = mutableListOf<ReminderDTO>()
    var hasErrors = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (hasErrors) return Result.Error(ERROR_MESSAGE)
        return Result.Success<List<ReminderDTO>>(reminderDTO)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        reminderDTO.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (hasErrors) return Result.Error(ERROR_MESSAGE)
        for (reminder in reminderDTO) {
            if (reminder.id == id) return Result.Success(reminder)
        }

        return Result.Error(message = "Could not find a reminder with the id $id")
    }

    override suspend fun deleteAReminder(reminder: ReminderDTO) {
        reminderDTO.remove(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminderDTO.clear()
    }


}
