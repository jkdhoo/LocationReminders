package com.udacity.locationreminders.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.locationreminders.R
import com.udacity.locationreminders.base.BaseFragment
import com.udacity.locationreminders.base.NavigationCommand
import com.udacity.locationreminders.databinding.FragmentSaveReminderBinding
import com.udacity.locationreminders.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.locationreminders.locationreminders.reminderslist.ReminderDataItem
import com.udacity.locationreminders.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.viewModel = _viewModel
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                _viewModel.setCurrentLocation(location)
            }
        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.poiLatitude.value
            val longitude = _viewModel.poiLongitude.value
            _viewModel.validateAndSaveReminder(ReminderDataItem(title, description, location, latitude, longitude))
            Timber.i("Save Clicked")
        }
        _viewModel.geofenceRequest.observe(viewLifecycleOwner, { geofenceRequest ->
            if (geofenceRequest != null) {
                val geofencePendingIntent: PendingIntent by lazy {
                    val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
                    intent.action = ACTION_GEOFENCE_EVENT
                    // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
                    // addGeofences() and removeGeofences().
                    PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                _viewModel.addGeofence(geofencePendingIntent, geofencingClient, geofenceRequest)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            Timber.i("Navigate back to RemindersListFragment")
            _viewModel.navigationCommand.value = NavigationCommand.Back
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT =
            "com.udacity.locationreminders.action.ACTION_GEOFENCE_EVENT"
    }
}
