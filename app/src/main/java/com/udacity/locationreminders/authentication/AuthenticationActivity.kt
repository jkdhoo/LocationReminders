package com.udacity.locationreminders.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.locationreminders.R
import com.udacity.locationreminders.databinding.ActivityAuthenticationBinding
import com.udacity.locationreminders.locationreminders.RemindersActivity
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModels()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Timber.i("Sign in unsuccessful")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAuthenticationBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )
        binding.authViewModel = viewModel
        binding.authButton.setOnClickListener { launchSignInFlow() }
        Timber.i("Observe Authentication State")
        viewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    Timber.i("Authenticated, routing to Reminders")
                    val reminderActivityIntent =
                        Intent(applicationContext, RemindersActivity::class.java)
                    startActivity(reminderActivityIntent)
                }
                AuthenticationViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Timber.i("Unauthenticated")
                    Snackbar.make(
                        binding.root, this.getString(R.string.login_unsuccessful_msg),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> Timber.i("Unable to act on authentication state")
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startForResult.launch(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build()
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
