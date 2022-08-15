package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationViewModel : ViewModel() {

    // Enum class to determine the state of user
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    // Adding variable to observe to get the user state
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}