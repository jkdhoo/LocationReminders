package com.udacity.locationreminders.locationreminders.data.local

import com.udacity.locationreminders.locationreminders.data.ReminderDataSource
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
open class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder: List<ReminderDTO>? = reminders?.filter { it.id == id }
        return if (reminder!!.isNotEmpty()) {
            Result.Success(reminder[0])
        } else Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}