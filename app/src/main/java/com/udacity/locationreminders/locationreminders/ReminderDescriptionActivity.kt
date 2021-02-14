package com.udacity.locationreminders.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.locationreminders.R
import com.udacity.locationreminders.databinding.ActivityReminderDescriptionBinding
import com.udacity.locationreminders.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        val intent = this.intent
        val bundle = intent.extras
        if (bundle != null) {
            binding.reminderDataItem = bundle.get(EXTRA_ReminderDataItem) as ReminderDataItem
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val reminderActivityIntent = Intent(applicationContext, RemindersActivity::class.java)
        startActivity(reminderActivityIntent)
    }
}
