package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.SIGN_IN_RESULT_CODE
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    // Getting the AuthenticationViewModel
    private val authenticationViewModel by viewModels<AuthenticationViewModel>()

    // For Binding
    private lateinit var binding: ActivityAuthenticationBinding

    // Code of signing
    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        // Setting onclickListener for login Button
        binding.loginButton.setOnClickListener{
            launchSignInFlow()
        }


        //If the user was authenticated, send him to RemindersActivity
        authenticationViewModel.authenticationState.observe(this, Observer {authenticationState->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> startActivity(getRemainderActivityIntent())
                else -> Log.i("TAG", "Authentication state that doesn't require any UI change $authenticationState")
            }
        })


    }

    private fun launchSignInFlow() {
        // Give the user some options to signIn
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), SIGN_IN_RESULT_CODE)
    }

    private fun getRemainderActivityIntent(): Intent {
        return Intent(this, RemindersActivity::class.java)
    }
}
