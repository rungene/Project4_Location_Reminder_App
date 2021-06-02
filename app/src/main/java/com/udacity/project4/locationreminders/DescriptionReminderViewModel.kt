package com.udacity.project4.locationreminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class DescriptionReminderViewModel(val app: Application, val dataSource: ReminderDataSource) : AndroidViewModel(app) {

    val deletedReminder = MutableLiveData(false)

    fun deleteAReminder(reminderDataItem: ReminderDataItem) {
        viewModelScope.launch {
            dataSource.deleteAReminder(ReminderDTO(
                id = reminderDataItem.id,
                title = reminderDataItem.title,
                description = reminderDataItem.description,
                location = reminderDataItem.location,
                longitude = reminderDataItem.longitude,
                latitude = reminderDataItem.latitude
            ))
            deletedReminder.value = true
        }
    }
}