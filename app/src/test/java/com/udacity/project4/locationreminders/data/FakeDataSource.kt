package com.udacity.project4.locationreminders.data


import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource  : ReminderDataSource {

//     Create a fake data source to act as a double to the real data source
companion object {
    const val ERROR_MESSAGE = "Error!"
}
    private var reminderDTO = mutableListOf<ReminderDTO>()
    var hasErrors = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //("Return the reminders")
        if (hasErrors) return Result.Error(ERROR_MESSAGE)
        return Result.Success<List<ReminderDTO>>(reminderDTO)

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
       // ("save the reminder")
    reminderDTO.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //("return the reminder with the id")
        if (hasErrors) return Result.Error(ERROR_MESSAGE)
        var outcome: ReminderDTO? = null
        for (reminder in reminderDTO) {
            if (reminder.id == id) {
                outcome = reminder
                break;
            }
        }

        if (outcome != null) {
            return Result.Success(outcome)
        }
        return Result.Error(message = "Please know we could not find  reminder with the id $id ", 404)
    }

    override suspend fun deleteAReminder(reminder: ReminderDTO) {
        //("Not yet implemented")
        reminderDTO.remove(reminder)
    }

    override suspend fun deleteAllReminders() {
        //("delete all the reminders")
        reminderDTO.clear()
    }


}