package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback,GoogleMap.OnMapLongClickListener,GoogleMap.OnPoiClickListener {
    companion object {
        const val TAG = "SelectLocationFragment"
        const val REQUEST_ONLY_FOREGROUND_PERMISSIONS_REQUEST_CODE = 10001
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationKnownLast: Location? = null
    private var latLngSelected: LatLng? = null
    private var pointOfInterestSelected: PointOfInterest? = null
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient = FusedLocationProviderClient(requireActivity())


        val googleMapFragment = childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        googleMapFragment.getMapAsync(this)

//        add the map setup implementation

//         zoom to the user location after taking his permission
//         add style to the map
//         put a marker to location that the user selected


//        call this function after the user confirms on the selected location


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        _viewModel.confirmedLocation.observe(viewLifecycleOwner, {
            if (it) {
                onLocationSelected()
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //  Change the map type based on the user's selection.
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {

        if (isForegroundPermissionEnabled()) {
            enableMyLocation()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){

                Snackbar.make(requireView(), getString(R.string.access_location_needed), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.enable_location)) {
                        requestForegroundPermissions()
                    }
                    .show()
            } else {
                Snackbar.make(requireView(), getString(R.string.location_permission_needed), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.change_permissions)) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", com.udacity.project4.BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                    .show()
            }
        }
    }

  fun isForegroundPermissionEnabled(): Boolean {
        return (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION))
    }

    protected fun requestForegroundPermissions() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissionsArray, REQUEST_ONLY_FOREGROUND_PERMISSIONS_REQUEST_CODE)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Use isForegroundPermissionEnabled() method to check whether foreground and background location permissions are granted
        if (isForegroundPermissionEnabled()) {
            googleMap.setMyLocationEnabled(true)
            val resultLocation = fusedLocationProviderClient.lastLocation
            resultLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    locationKnownLast = task.result
                    if (locationKnownLast != null) {
                        latLngSelected = LatLng(locationKnownLast!!.latitude,
                            locationKnownLast!!.longitude)
                        val myTitle = getString(R.string.given_location)
                        pointOfInterestSelected = PointOfInterest(latLngSelected, getString(R.string.my_location), myTitle)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngSelected, 16f))
                        if (latLngSelected != null) {
                            val myMarker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLngSelected as LatLng)
                                    .title(myTitle)
                            )
                            myMarker.showInfoWindow()
                        }
                    }
                }
            }

        }
        else {
            // Use requestForegroundPermissions() method to request foreground and background permissions
            requestForegroundPermissions()
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        enableMyLocation()
        setMapStyle(map)
        googleMap.setOnMapLongClickListener(this)
        googleMap.setOnPoiClickListener(this)
    }

    /*To set the JSON style to the map, call setMapStyle() on the GoogleMap object. Pass in a
 MapStyleOptions object, which loads the JSON file. The setMapStyle() method returns a boolean
 indicating the success of the styling.*/
    private fun setMapStyle(map: GoogleMap) {
        try {

            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Can't find style. Error: .",e)
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        googleMap.clear()
        latLngSelected = latLng
        val title = getString(R.string.given_location)
        pointOfInterestSelected = PointOfInterest(latLngSelected as LatLng, "myId", title)
        val gogleMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
        gogleMarker.showInfoWindow()
    }
//This click-listener places a marker on the map
//    //immediately when the user clicks on a POI.
    override fun onPoiClick(pointOfInterest: PointOfInterest) {
        googleMap.clear()
        pointOfInterestSelected = pointOfInterest
        latLngSelected = pointOfInterest.latLng
        val poiMarker = googleMap.addMarker(MarkerOptions()
            .position(pointOfInterest.latLng)
            .title(pointOfInterest.name)
        )
      //  call showInfoWindow() on poiMarker to show the info window.
        poiMarker.showInfoWindow()
    }

    //         When the user confirms on the selected location,
    //         send back the selected location details to the view model
    //         and navigate back to the previous fragment to save the reminder and add the geofence
    private fun onLocationSelected() {
        if (isForegroundPermissionEnabled()) {
            if (latLngSelected != null && pointOfInterestSelected != null) {
                _viewModel.locationConfirmation(latLngSelected as LatLng, pointOfInterestSelected as PointOfInterest)
            }
        } else {
            requestForegroundPermissions()
        }
    }

}
