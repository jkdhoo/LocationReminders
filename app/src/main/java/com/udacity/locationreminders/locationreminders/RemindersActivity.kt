package com.udacity.locationreminders.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.locationreminders.R
import kotlinx.android.synthetic.main.activity_reminders.*
import timber.log.Timber

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 33
        private const val TARGET_API = 30
    }

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        checkAndRequestForegroundLocation()
    }

    @TargetApi(TARGET_API)
    fun checkAndRequestForegroundLocation() {
        Timber.i("Start foreground location permission check")
        val foregroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
        if (foregroundLocationApproved) {
            Timber.i("Foreground location permission already approved")
            checkAndRequestBackgroundLocation()
            return
        }
        Timber.i("Requesting foreground location permissions")
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        )
    }

    @TargetApi(TARGET_API)
    fun checkAndRequestBackgroundLocation() {
        Timber.i("Start background location permission check")
        if (!runningQOrLater) {
            Timber.i("Device is not running Android Q or later. Skipping.")
            return
        } else {
            Timber.i("Device is running Android Q or later")
            val backgroundLocationApproved =
                (PackageManager.PERMISSION_GRANTED == this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            if (backgroundLocationApproved) {
                Timber.i("Background location permission already approved")
                return
            }
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
            return
        }
    }

    private fun checkAndRequestLocationServices(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        Timber.i("Starting failure listener")
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(this, REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.i("Failure Listener: Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                    this.nav_host_fragment,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkAndRequestForegroundLocation()
                }.show()
            }
        }
        Timber.i("Starting success listener")
        locationSettingsResponseTask.addOnCompleteListener {
            Timber.i("Success Listener: Location is turned on")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] != 1) {
                    Timber.i("Failed to grant background location permissions")
                } else Timber.i("Successfully granted background location permissions")
                checkAndRequestLocationServices()
            }
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] != 0) {
                    Timber.i("Failed to grant foreground location permissions")
                } else Timber.i("Successfully granted foreground location permissions")
                checkAndRequestBackgroundLocation()
            }
            else -> {
                Timber.i("Unrecognized permissions result")
                Timber.i("$requestCode, ${permissions[0]}, ${grantResults[0]}")
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
