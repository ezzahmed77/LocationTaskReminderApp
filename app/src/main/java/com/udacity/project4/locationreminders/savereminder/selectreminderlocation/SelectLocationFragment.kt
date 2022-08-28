package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


private const val TAG = "LocationMap"
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // For using current location of user
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation = UserLocation()

    private lateinit var locationCallback: LocationCallback

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    // For map
    private lateinit var map : GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Connecting the map to fragment Async
        val mapFragment = childFragmentManager.findFragmentById(R.id.select_location_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Check if the user chose location and save it
        binding.saveLocationButton.setOnClickListener{
            // Navigate to the saveReminderFragment
            _viewModel.navigationCommand.postValue(
                NavigationCommand.To(
                    SelectLocationFragmentDirections
                        .actionSelectLocationFragmentToSaveReminderFragment()
                )
            )
        }

        // For getting current location of user
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // For getting exact location of user
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    showCurrentLocationOfUser()
                }
            }
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Log.i("OnResume", "OnResume Was called")
        startLocationUpdates()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Setting onclick on the map to get the userSelectedLocation
        setMapOnClick(map)

        // Styling Map
        setMapStyle(map)

        // If the user clicks on a point of interest save the location and show window
        setPoiClick(map)

        // showing current location of user
        checkPermissionAndShowCurrentLocationOfUser()

    }


    // These methods related to tracking the current location of the user

    private fun startLocationUpdates() {
        if(isPermissionGranted()){
            fusedLocationClient.requestLocationUpdates(
                LocationRequest(),
                locationCallback,
                Looper.getMainLooper())
        }

    }



    private fun isPermissionGranted(): Boolean {
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){ return false }
        return true
    }

    private fun checkPermissionAndShowCurrentLocationOfUser() {
        // Check if permission granted, if not request it
        if(isPermissionGranted()){
            showCurrentLocationOfUser()
        }
        else{
            Toast.makeText(requireContext(), "Please Enable Location", Toast.LENGTH_LONG).show()
            requestPermissions( arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
    }

    private fun showCurrentLocationOfUser(){
        val task = fusedLocationClient.lastLocation
        // Enable the location layer to enable user to get to his location

        task.addOnSuccessListener { location: Location?->
            map.isMyLocationEnabled = true
            if(location != null){
                userLocation.latitude = location.latitude
                userLocation.longitude = location.longitude
                // Animate the camera to home location
                val homeLatLong = LatLng(userLocation.latitude, userLocation.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, 15f))
                // Adding marker to home
                map.addMarker(MarkerOptions().position(homeLatLong))

            }
            else{
                startLocationUpdates()
                Toast.makeText(requireContext(), "Error in Getting Location ", Toast.LENGTH_LONG).show()
            }

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i("ResultPermission", "onRequestPermissionsResult was called")
        if(requestCode == 101){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("ResultPermission", "Show CurrentLocation")
                Toast.makeText(requireContext(), "Result Permission granted", Toast.LENGTH_LONG).show()
                showCurrentLocationOfUser()
            }
        }
        else{
            Toast.makeText(requireContext(), "Please Enable Location For better usage of the app", Toast.LENGTH_LONG).show()
        }
    }


    // Methods related to styling the map

    // Adding marker when the user long click on the map
    private fun setMapOnClick(map : GoogleMap){
        map.setOnMapClickListener { latLong->
            // Saving the chosen location
            setUserChosenLocation(latLong)
            // Adding snippet that shows the location of marker in window below title
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLong.latitude,
                latLong.longitude
            )
            map.addMarker(MarkerOptions()
                .position(latLong)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
                // Changing the Color of Marker
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
    }

    // This function styles the map from the file json
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.maps_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    // Adding marker with info window POI--> point of interest
    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { poi->
            // Save it as point of interest
            _viewModel.selectedPOI.value = poi
            // Save it as chosen location
            setUserChosenLocation(poi.latLng)

            val poiMarker = map.addMarker(MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
            )
            // Showing window with the name of POI
            poiMarker!!.showInfoWindow()
        }
    }


    // Function to set data to saveReminderViewModel
    private fun setUserChosenLocation(latLng: LatLng){
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = String.format(
            Locale.getDefault(),"LatLong (%1\$.3f, %2\$.3f)",
            latLng.latitude,
            latLng.longitude )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}

// Class For User's Location
data class UserLocation(var latitude: Double = 0.0, var longitude: Double = 0.0)
