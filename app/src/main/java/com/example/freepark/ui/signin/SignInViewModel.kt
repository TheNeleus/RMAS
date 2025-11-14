package com.example.freepark.ui.signin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.freepark.data.repository.AuthenticateRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val AuthenticateRepo: AuthenticateRepo
) : ViewModel() {
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val showPassword = mutableStateOf(false)

    val loginSuccess = mutableStateOf<Boolean?>(null)
    val loginError = mutableStateOf<String?>(null)


    fun login() {
        val usernameValue = username.value
        val passwordValue = password.value

        if (usernameValue.isBlank() || passwordValue.isBlank()) {
            loginSuccess.value = false
            loginError.value = "All fields are required"
            return
        }

        val fakeEmail = "$usernameValue@freepark.com"

        AuthenticateRepo.loginUser(fakeEmail, passwordValue) { success, error ->
            if (success) {
                loginSuccess.value = true
                loginError.value = null
            } else {
                loginSuccess.value = false
                loginError.value = error?.localizedMessage
            }
        }
    }
}