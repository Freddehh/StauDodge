package com.example.staudodge

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * This class handles the check for permissions and the call for asking for it.
 */

class Permissions(private val activity: MainActivity) {
    private var context = activity.baseContext

    //If user needs to give permission this function asks for it
    fun fetchLocationPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                activity.locationPermissionCode
            )
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                activity.locationPermissionCode
            )
        }
    }

    //Returns true if the user have given permission otherwise returns false
    fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }
}