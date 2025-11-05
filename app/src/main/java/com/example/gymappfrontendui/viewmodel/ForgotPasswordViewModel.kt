package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    EnterUsername,
    EnterCode,
    EnterNewPassword,
    Success,
    Error
}

data class ForgotPasswordState(
    val currentStep: ForgotPasswordStep = ForgotPasswordStep.EnterUsername,
    val username: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Specific field errors
    val usernameError: String? = null,
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    fun updateUsername(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, usernameError = null, errorMessage = null) }
    }

    fun updateCode(newCode: String) {
        _uiState.update { it.copy(code = newCode, codeError = null, errorMessage = null) }
    }

    fun updateNewPassword(newPass: String) {
        _uiState.update { it.copy(newPassword = newPass, newPasswordError = null, errorMessage = null) }
    }

    fun updateConfirmPassword(confirmPass: String) {
        _uiState.update { it.copy(confirmPassword = confirmPass, confirmPasswordError = null, errorMessage = null) }
    }

    fun goBackStep() {
        _uiState.update { currentState ->
            when (currentState.currentStep) {
                ForgotPasswordStep.EnterCode -> {
                    currentState.copy(
                        currentStep = ForgotPasswordStep.EnterUsername,
                        code = "",
                        codeError = null,
                        errorMessage = null,
                        isLoading = false
                    )
                }
                ForgotPasswordStep.EnterNewPassword -> {
                    currentState.copy(
                        currentStep = ForgotPasswordStep.EnterCode,
                        newPassword = "",
                        confirmPassword = "",
                        newPasswordError = null,
                        confirmPasswordError = null,
                        errorMessage = null,
                        isLoading = false
                    )
                }
                else -> {
                    Log.d(TAG, "goBackStep: No action needed for current step.")
                    currentState
                }
            }
        }
    }

    fun cancelAndGoToLogin() {
        _uiState.value = ForgotPasswordState()
    }
    fun sendResetCode() {
        val username = _uiState.value.username
        Log.d(TAG, "sendResetCode called for username: $username")
        _uiState.update { it.copy(usernameError = null, errorMessage = null) }
        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username is required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        Log.d(TAG, "State updated: isLoading=true")
        viewModelScope.launch {
            val success = userRepository.getEmailWithForgotPasswordCode(username)
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.EnterCode,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to send code. User might not exist or email is not linked."
                    )
                }
            }
        }
    }

    fun verifyResetCode() {
        val username = _uiState.value.username
        val code = _uiState.value.code
        if (code.isBlank()) {
            _uiState.update { it.copy(codeError = "Verification code is required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val success = userRepository.verifyResetPasswordCode(username, code)
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.EnterNewPassword,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Invalid or expired verification code."
                    )
                }
            }
        }
    }

    fun resetPassword() {
        val newPassword = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword
        var hasError = false

        val (isValid, errorMsg) = isValidPassword(newPassword)
        if (!isValid) {
            _uiState.update { it.copy(newPasswordError = errorMsg) }
            hasError = true
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match.") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val success = userRepository.resetPassword(newPassword)
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.Success,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to reset password. The reset link/code might have expired."
                    )
                }
            }
        }
    }
    fun goBackToUsernameEntry() {
        _uiState.value = ForgotPasswordState()
    }
}

private fun isValidPassword(password: String): Pair<Boolean, String?> {
    if (password.length < 8) {
        return false to "Password must be at least 8 characters."
    }
    if (!password.any { it.isUpperCase() }) {
        return false to "Password must contain at least one uppercase letter."
    }
    if (!password.any { it.isDigit() }) {
        return false to "Password must contain at least one digit."
    }
    return true to null
}