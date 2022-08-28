package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject



class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    // Adding variable to check if the device running Q or later to choose whether to ask for
    // background permission or not
    private  var  runningQOrLater: Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // Class that contains some helper methods for adding geofence
    private lateinit var geofenceHelper : GeofenceHelper

    // for geofencing client
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
        binding.selectLocation.setOnClickListener {
        // Navigate to  selectLocationFragment to get the user location
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }
        // Instantiating GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        geofenceHelper = GeofenceHelper(requireContext())

        binding.saveReminder.setOnClickListener {
            checkPermissionsAndStartGeofencing()

        }
    }


    private fun getReminderDataItemSaved(): ReminderDataItem{
        return ReminderDataItem(
            _viewModel.reminderTitle.value,
            _viewModel.reminderDescription.value,
            _viewModel.reminderSelectedLocationStr.value,
            _viewModel.latitude.value,
            _viewModel.longitude.value
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    // Function to check for location access and adding new geofence
    private fun addNewGeofence(reminderDataItem: ReminderDataItem){
            // We have access to location, So add geofence
            val latLng = LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!)
            val geofence = geofenceHelper.getGeofence(reminderDataItem.id, latLng,
                GEOFENCE_RADIUS.toFloat(), Geofence.GEOFENCE_TRANSITION_ENTER)
            val geofenceRequest = geofenceHelper.getGeofencingRequest(geofence)
            val pendingIntent = geofenceHelper.getPendingIntent()
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.i(TAG, "Geofence was successfully added")
                    Toast.makeText(requireContext(), "Geofence Added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    val errorMessage = geofenceHelper.getErrorOfGeofence(e)
                    Log.i(TAG, errorMessage)
                    Toast.makeText(requireContext(), "Geofence Not Added", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndAddNewGeofence(false)
        }
    }

    // Start of methods that asks for permissions to used geofencing
    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndAddNewGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettingsAndAddNewGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0,0,0,null)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.saveReminderMain,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndAddNewGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                val reminderDataItem = getReminderDataItemSaved()
                if(_viewModel.validateAndSaveReminder(reminderDataItem)){
                    addNewGeofence(reminderDataItem)
                }
            }
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        // Updated to use request permission for fragment
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            || requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // This means we have the permission
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_LONG).show()
                checkDeviceLocationSettingsAndAddNewGeofence()
            }
            else{
                // we don't have the permission
                Toast.makeText(requireContext(), "App needs access to location to be able to add geofence", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // End of methods that asks for permissions to used geofencing

}

// Some constants used to get the result code for permissions
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val GEOFENCE_RADIUS = 200.0
private const val TAG = "LocationReminder"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
