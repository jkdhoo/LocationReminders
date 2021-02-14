package com.udacity.locationreminders.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.locationreminders.R
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result
import com.udacity.locationreminders.locationreminders.data.local.FakeAndroidTestRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.hamcrest.MatcherAssert.assertThat as assertThatHam

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: FakeAndroidTestRepository
    private val reminderDTO = ReminderDTO("Title1", "Description1", "Location1", 1.1, 1.1, "1")

    @Before
    fun initRepository() = runBlockingTest {
        repository = FakeAndroidTestRepository()
        repository.saveReminder(reminderDTO)
    }

    @After
    fun cleanupDb() = runBlockingTest {
        repository.deleteAllReminders()
        repository.setReturnError(false)
    }

    @Test
    fun clickAddReminderButton_navigateToAddReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun getAllReminders_triggerError() = runBlockingTest {
        repository.setReturnError(true)
        val reminders = repository.getReminders() as Result.Error
        assertThatHam(reminders, IsEqual(Result.Error("Test exception")))
    }
}