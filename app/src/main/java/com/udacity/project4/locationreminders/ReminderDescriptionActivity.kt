package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }
    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var geofencingClient: GeofencingClient

    private val reminderDescriptionViewModel: DescriptionReminderViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
//      Add the implementation of the reminder details
        binding.viewModel = reminderDescriptionViewModel

        geofencingClient = LocationServices.getGeofencingClient(this)
        

        val itemReminder = intent.extras?.getSerializable(EXTRA_ReminderDataItem) as ReminderDataItem
        binding.reminderDataItem = itemReminder

        reminderDescriptionViewModel.deletedReminder.observe(this, Observer { reminderHasBeenDeleted ->
            if (reminderHasBeenDeleted) {
                geofencingClient.removeGeofences(mutableListOf(itemReminder.id)).addOnSuccessListener {
                    val intent = Intent(this, RemindersActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    reminderDescriptionViewModel.deletedReminder.value = false
                }
            }
        })
    }
}
