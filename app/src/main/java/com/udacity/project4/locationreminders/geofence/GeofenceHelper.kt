package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import java.lang.Exception

class GeofenceHelper(base: Context?) : ContextWrapper(base) {

    // Here I will provide some helper methods to add new geofence

    fun getGeofencingRequest(geofence: Geofence) : GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

    }

    fun getGeofence(ID: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setTransitionTypes(transitionTypes)
            .setRequestId(ID)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            this, 2607, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // Get Geofence Error
    fun getErrorOfGeofence(e: Exception) : String{
        var geofenceError = ""
        if(e is ApiException){
            val apiException = e
            geofenceError = when(apiException.statusCode){
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE->"Geofence Not Available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES->"Geofence Too Many Geofence"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS->"Geofence Too Many PendingIntents"
                else->"Error"
            }
        }
        return geofenceError
    }

}