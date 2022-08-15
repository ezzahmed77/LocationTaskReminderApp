package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.bind

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding

    // For Map
    private lateinit var map : GoogleMap

    // For reminderDataItem
    private lateinit var reminderDataItem: ReminderDataItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        val reminderDataItemFromList = intent.getSerializableExtra("reminderItem")
        val reminderDataItemFromNotification  = intent.getSerializableExtra(EXTRA_ReminderDataItem)

        // Get Support Map Fragment
        // Connecting the map to fragment Async
        val mapFragment = supportFragmentManager.findFragmentById(R.id.showSelectedLocationFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(reminderDataItemFromNotification != null ){
            reminderDataItem = reminderDataItemFromNotification as ReminderDataItem
        }
        else{
            reminderDataItem = reminderDataItemFromList as ReminderDataItem
        }
        // Attaching reminderItem to binding
        binding.reminderDataItem = reminderDataItem



    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // showing location of reminder in fragment
        showReminderItemLocation()

    }

    private fun showReminderItemLocation() {
        // Animate the camera to home location
        val homeLatLong = LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, 17f))
        // Adding marker to home
        map.addMarker(MarkerOptions().position(homeLatLong))

    }


}
