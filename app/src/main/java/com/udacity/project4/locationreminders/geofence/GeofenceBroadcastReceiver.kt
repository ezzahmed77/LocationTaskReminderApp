package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var coroutineJob: Job = Job()
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private lateinit var mContext : Context

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        Toast.makeText(context, "Geofence Triggered...", Toast.LENGTH_LONG).show()
        Log.i("geofenceTriggered", "Geofence Triggered....")

        // Get geofencing event
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Getting list of triggering Geofence
        val listOfGeofence: List<Geofence> = geofencingEvent.triggeringGeofences

        sendNotification(listOfGeofence)

    }


    private fun sendNotification(triggeringGeofence: List<Geofence>) {
        var requestId = ""
        for(geofence : Geofence in triggeringGeofence){
            requestId = geofence.requestId
        }

        //Get the local repository instance
        val remindersLocalRepository = RemindersLocalRepository(LocalDB.createRemindersDao(mContext))
        //Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                com.udacity.project4.utils.sendNotification(
                    mContext, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }
}