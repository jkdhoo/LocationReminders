/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.locationreminders.locationreminders.data.local

import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeAndroidTestRepository : FakeDataSource() {

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return super.getReminders()
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        super.saveReminder(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return super.getReminder(id)
    }

    override suspend fun deleteAllReminders() {
        super.deleteAllReminders()
    }
}
