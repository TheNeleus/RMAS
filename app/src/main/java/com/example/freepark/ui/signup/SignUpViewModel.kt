package com.example.freepark.ui.signup

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.freepark.data.repository.AuthenticateRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val AuthenticateRepo: AuthenticateRepo
) : ViewModel() {

    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")
    val phoneNumber = mutableStateOf("")
    val photoUri = mutableStateOf<Uri?>(null)
    val showPassword = mutableStateOf(false)
    val passwordError = mutableStateOf<String?>(null)
    private val MIN_PASSWORD_LENGTH = 6
    val signUpSuccess = mutableStateOf<Boolean?>(null)
    val signUpError = mutableStateOf<String?>(null)

    fun register() {
        val usernameValue = username.value
        val passwordValue = password.value
        val firstNameValue = firstName.value
        val lastNameValue = lastName.value
        val phoneValue = phoneNumber.value
        val photoValue = photoUri.value

        if (usernameValue.isBlank() || passwordValue.isBlank() ||
            firstNameValue.isBlank() || lastNameValue.isBlank() || phoneValue.isBlank()
        ) {
            signUpSuccess.value = false
            signUpError.value = "All fields are required"
            return
        }

        if (passwordValue.length < MIN_PASSWORD_LENGTH) {
            passwordError.value = "Password must have at least $MIN_PASSWORD_LENGTH characters."
            signUpSuccess.value = false
            return // Zaustavi slanje zahteva Firebase-u
        }

        val fakeEmail = "$usernameValue@freepark.com"

        AuthenticateRepo.registerUser(
            fakeEmail,
            passwordValue,
            usernameValue,
            firstNameValue,
            lastNameValue,
            phoneValue,
            photoValue
        ) { success, error ->
            if (success) {
                signUpSuccess.value = true
                signUpError.value = null
            } else {
                signUpSuccess.value = false
                signUpError.value = error?.localizedMessage
            }
        }
    }
}