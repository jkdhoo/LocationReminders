package com.udacity.locationreminders.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.udacity.locationreminders.R
import com.udacity.locationreminders.authentication.AuthenticationViewModel
import com.udacity.locationreminders.authentication.FirebaseUserLiveData
import com.udacity.locationreminders.base.BaseViewModel
import com.udacity.locationreminders.locationreminders.data.ReminderDataSource
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationViewModel.AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED
        }
    }

    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    fun clearAll() {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllReminders()
            showLoading.value = false
            showToast.value = getApplication<Application>().getString(R.string.reminders_cleared)
        }
        loadReminders()
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message!!
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}