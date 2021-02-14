package com.udacity.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminders.R
import com.udacity.locationreminders.data.local.FakeDataSource
import com.udacity.locationreminders.locationreminders.data.ReminderDataSource
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result
import com.udacity.locationreminders.locationreminders.reminderslist.ReminderDataItem
import com.udacity.locationreminders.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminders.util.MainCoroutineRule
import com.udacity.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initTests() {
        reminderDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun tearDown() = runBlockingTest {
        reminderDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun saveReminder_getReminder() = runBlockingTest {
        val reminder = ReminderDataItem("Title","Description","Location",0.0, 0.0, "ID")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(reminderDataSource.getReminder("ID"), notNullValue())
    }

    @Test
    fun saveBadReminder_getNoReminder() = runBlockingTest {
        val reminder = ReminderDataItem(null,"Description","Location",0.0, 0.0, "ID")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(reminderDataSource.getReminder("ID"), `is`(Result.Error("Reminder not found")))
    }

    @Test
    fun saveReminderSuccess_loading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the viewmodel
        val reminder = ReminderDataItem("Title","Description","Location",0.0, 0.0, "ID")
        saveReminderViewModel.validateAndSaveReminder(reminder)

        // Then progress indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveInvalidReminder_verifySnackbar() = runBlockingTest {

        assertThat(saveReminderViewModel.snackbarString.getOrAwaitValue(), `is`(-1))

        val reminder = ReminderDataItem(null,"Description","Location",0.0, 0.0, "ID")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        val reminders = reminderDataSource.getReminders() as Result.Success

        // Verify the task is completed
        assertThat(reminders.data.size, IsEqual(0))

        // The snackbar is updated
        val snackbarText = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(snackbarText, `is`(R.string.err_enter_title))

    }
}