package com.udacity.locationreminders.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.locationreminders.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminders.locationreminders.data.dto.Result
import com.udacity.locationreminders.util.MainAndroidTestCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private val reminderDTO1 = ReminderDTO("Title1", "Description1", "Location1", 1.1, 1.1, "1")
    private val reminderDTO2 = ReminderDTO("Title2", "Description2", "Location2", 2.2, 2.2, "2")

    private lateinit var remindersRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersRepository = RemindersLocalRepository(
            database.reminderDao(), Dispatchers.Main
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() = database.close()

    @Test
    fun addReminder_requestReminders() = mainCoroutineRule.runBlockingTest {
        remindersRepository.saveReminder(reminderDTO1)
        remindersRepository.saveReminder(reminderDTO2)
        val remindersAll = remindersRepository.getReminders() as Result.Success
        val reminderSpecific = remindersRepository.getReminder("1") as Result.Success
        assertThat(remindersAll.data.size, IsEqual(2))
        assertThat(reminderSpecific.data, IsEqual(reminderDTO1))
    }
}

