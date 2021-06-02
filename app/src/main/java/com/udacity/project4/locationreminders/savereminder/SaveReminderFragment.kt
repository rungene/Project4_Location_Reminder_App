package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig.APPLICATION_ID
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {
    companion object {
        const val REQUEST_ONLY_BACKGROUND_REQUEST_CODE = 10002
        const val REMINDER_UUID_KEY = "REMINDER_UUID_KEY"
    }
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            :use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

         val   reminderDataItem = ReminderDataItem(
                title = title,
                description = description,
                location = location,
                latitude = latitude,
                longitude = longitude
            )
            val checkIfReminderValid = _viewModel.validateEnteredData(reminderDataItem)

            if (checkIfReminderValid) {
                addGeofenceAndSaveReminder(reminderDataItem)

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //Note clear the view model after destroy. It is single view model.
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isPermissionsBackgroundLocationEnabled()) {
            Log.d(TAG, "onRequestPermissionResult")
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                /*   This app has very little use when permissions are not granted so present a snackbar
              explaining that the user needs location permissions */
                Snackbar.make(requireView(),
                    getString(R.string.permission_denied_explanation),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                        getString(R.string.permission_rationale_snackbar_button_text)) {
                     backgroundPermissionsRequest()
                    }
                    .show()
            } else {
                Snackbar.make(requireView(),
                    getString(R.string.permissions_background_denied),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.change_permissions)) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID , null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    .show()
            }
        }
    }
    @TargetApi(29)
    private fun backgroundPermissionsRequest() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestPermissions(
                permissionsArray,
                REQUEST_ONLY_BACKGROUND_REQUEST_CODE
            )
        }
    }

    @TargetApi(29)
    private fun isPermissionsBackgroundLocationEnabled(): Boolean {

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    }
/*    private fun checkPermissionsAndStartGeofencing() {

        if (isPermissionsBackgroundLocationEnabled()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
           backgroundPermissionsRequest()
        }
    }
    *//*
 *  Uses the Location Client to check the current state of location settings, and gives the user
 *  the opportunity to turn on location services within our app.
 *//*
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        //  Step 6 add code to check that the device's location is on
        // create a LocationRequest, a LocationSettingsRequest Builder.
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

//use LocationServices to get the Settings Client and create a val called
// locationSettingsResponseTask to check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

//Since the case we are most interested in here is finding out if the location settings are not
// satisfied, add an onFailureListener() to the locationSettingsResponseTask.
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }


        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceAndSaveReminder()
            }
        }

    }*/






    @SuppressLint("MissingPermission")
    private fun addGeofenceAndSaveReminder(reminderDataItem: ReminderDataItem) {
        if (isPermissionsBackgroundLocationEnabled()) {
//build geofence
            val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(
                    reminderDataItem.latitude!!,
                    reminderDataItem.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS // Radius in meters
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
//Build the geofence request.
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
            intent.putExtra(REMINDER_UUID_KEY, reminderDataItem.id)
            val geofencePendingIntent = PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {

                addOnSuccessListener {

                    _viewModel.validateAndSaveReminder(reminderDataItem)

                    Toast.makeText(
                        requireActivity(), R.string.geofences_added,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.e("Add Geofence", geofence.requestId)
                }
                addOnFailureListener {

                    val message = if (it.message != null) it.message else "-100"
                    val temp = message!!.replace(": ", "")

                    val errorType = when (Integer.parseInt(temp)) {
                        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> R.string.enable_google_location_services
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> R.string.too_many_geofences
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> R.string.too_many_geofences
                        else -> R.string.unknown_error
                    }
                    _viewModel.showSnackBarInt.value = errorType

                    it.message?.let { message ->
                        Log.w(TAG, message)
                    }
                }
            }
        }else {
            if (!isPermissionsBackgroundLocationEnabled()) {
                val alertDialogeBuilder = AlertDialog.Builder(requireActivity())
                alertDialogeBuilder
                    .setTitle(getString(R.string.title_background_location_required_dialogue))
                    .setMessage(R.string.body_background_location_required_dialogue)
                    .setPositiveButton(R.string.enablelocation_location_required) { dialog: DialogInterface?, which: Int ->
                      backgroundPermissionsRequest()
                    }
                    .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, _: Int ->
                        dialog?.let {
                            dialog.dismiss()
                        }
                    }
                    .show()
            }
        }
    }




}

private const val TAG = "SaveReminderFragment"
private const val GEOFENCE_RADIUS_IN_METERS = 100f
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(24 * 7 * 52)
