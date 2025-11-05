package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.dao.UserDao
import com.example.gymappfrontendui.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.repository.SyncQueueRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.gymappfrontendui.util.SyncManager

import kotlin.contracts.Returns

class LoginRegistryViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        const val PREFS_NAME = "gym_app_prefs"
        const val KEY_FIRST_LAUNCH = "is_first_launch"
    }
    private val sharedPrefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val userRepository = UserRepository(app.applicationContext)

    private val syncQueueRepository = SyncQueueRepository(app.applicationContext)
    private val _loginState = MutableStateFlow(false)
    val loginState: StateFlow<Boolean> get() = _loginState



    init {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                val loggedIn = users.any { it.isLoggedIn }
                _loginState.value = loggedIn
            }
        }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setAppHasBeenOpened() {
        sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    val loggedInUser: StateFlow<User?> = userRepository.getAllUsers().map { users ->
        users.find { it.isLoggedIn }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun loginWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.loginWithGoogle(idToken)
            if (success) {
                setAppHasBeenOpened()
                try {
                    SyncManager.syncNow(getApplication<Application>().applicationContext)
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Sync after login failed: ${e.message}")
                }
            }
            onResult(success)
        }
    }
    fun login(
        username: String,
        password: String,
        onLoginResult: (success: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = userRepository.login(username, password)
            if (success) {
                setAppHasBeenOpened()
            }
            onLoginResult(success)
        }
    }
    fun register(
        username: String,
        password: String,
        onRegisterResult: (success: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = userRepository.register(username, password)
            onRegisterResult(success)
        }
    }
    suspend fun getLoggedInUserID(): Int? {
        return userRepository.getLoggedInUserId()
    }
    suspend fun getGuestUserId(): Int{
        return userRepository.getGuestUserId()
    }
    fun isGuestMode(): Boolean {
        return !_loginState.value
    }

    suspend fun logout() {
        userRepository.logout()
    }
    val currentUsername: StateFlow<String?> = loggedInUser.map { user ->
        if (user != null) {
            user.userName
        } else {
            "guest"
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(1000),
        initialValue = null
    )

    suspend fun insertUser(user: User): Long{
        return userRepository.insertUser(user)
    }

    fun getUser(): Flow<List<User>> {
        return userRepository.getAllUsers()
    }

    suspend fun hasGuestData(): Boolean {
        return syncQueueRepository.hasGuestData()
    }

    suspend fun assignGuestDataToUser() {
        syncQueueRepository.assignSyncQueueToUser()
    }


    suspend fun changeUserName(currentPassword:String,newUsername: String): Boolean {
        return userRepository.changeUserName(currentPassword,newUsername)
    }

    suspend fun changeUserPassword(currentPassword: String, newPassword: String): Boolean {
        return userRepository.changeUserPassword(currentPassword, newPassword)
    }

    suspend fun addEmail(currentPassword : String,email: String): Boolean {
        return userRepository.addEmail(currentPassword,email)
    }

    suspend fun deleteUserEmail(password: String): Boolean {
        return userRepository.deleteUserEmail(password)
    }
    suspend fun getEmailVerificationCode(): Boolean {
        return userRepository.getEmailVerificationCode()
    }
    suspend fun verifyEmail(code: String): Boolean {
        return userRepository.verifyEmail(code)
    }
}