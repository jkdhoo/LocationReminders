package com.udacity.locationreminders.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.udacity.locationreminders.R
import com.udacity.locationreminders.base.BaseViewModel
import com.udacity.locationreminders.base.NavigationCommand
import com.udacity.locationreminders.locationreminders.data.ReminderDataSource
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String>()
    private val _reminderSelectedLocationStr = MutableLiveData<String>()
    val reminderSelectedLocationStr: LiveData<String>
        get() = _reminderSelectedLocationStr

    val snackbarString: LiveData<Int>
        get() = showSnackBarInt

    val toastString: LiveData<String>
        get() = showToast

    private val _poiLatitude = MutableLiveData<Double>()
    val poiLatitude: LiveData<Double>
        get() = _poiLatitude

    private val _poiLongitude = MutableLiveData<Double>()
    val poiLongitude: LiveData<Double>
        get() = _poiLongitude

    private val _currentLatitude = MutableLiveData<Double>()
    val currentLatitude: LiveData<Double>
        get() = _currentLatitude

    private val _currentLongitude = MutableLiveData<Double>()
    val currentLongitude: LiveData<Double>
        get() = _currentLongitude

    private val _geofenceRequest = MutableLiveData<GeofencingRequest>()
    val geofenceRequest: LiveData<GeofencingRequest>
        get() = _geofenceRequest

    init {
        showSnackBarInt.value = -1
        showToast.value = ""
    }

    fun setCurrentLocation(location: Location) {
        _currentLatitude.value = location.latitude
        _currentLongitude.value = location.longitude
    }

    fun setPOILocation(latLng: LatLng, snippet: String) {
        _poiLatitude.value = latLng.latitude
        _poiLongitude.value = latLng.longitude
        _reminderSelectedLocationStr.value = snippet
    }

    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        _reminderSelectedLocationStr.value = null
        _poiLatitude.value = null
        _poiLongitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
        }
        buildGeofence(reminderData)
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_description
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    private fun buildGeofence(reminderData: ReminderDataItem) {
        val lat = reminderData.latitude ?: return
        val lon = reminderData.longitude ?: return
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(lat, lon, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        Timber.i("Geofence built, $lat, $lon")
        _geofenceRequest.value = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        Timber.i("Geofence Request built, $lat, $lon")
        _geofenceRequest.value = null
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(
        geofencePendingIntent: PendingIntent,
        geofencingClient: GeofencingClient,
        geofenceRequest: GeofencingRequest
    ) {
        geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.i("Geofence Added")
                navigationCommand.value = NavigationCommand.Back
            }
            addOnFailureListener {
                Timber.i("Geofence Failed")
                navigationCommand.value = NavigationCommand.Back
            }
        }
    }
}