package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.content.Context
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


import kotlin.contracts.Returns

class LoginRegistryViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        const val PREFS_NAME = "gym_app_prefs"
        const val KEY_FIRST_LAUNCH = "is_first_launch"
    }
    private val sharedPrefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val userRepository = UserRepository(app.applicationContext)
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

    private val _currentUsername = MutableStateFlow(getCurrentUsername())

    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            userRepository.login(username, password)
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

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    private fun getCurrentUsername(): String {
        return if (!_loginState.value) {
            "guest"
        } else {
            sharedPrefs.getString("current_username", "") ?: ""
        }
    }

    suspend fun insertUser(user: User): Long{
        return userRepository.insertUser(user)
    }

    fun getUser(): Flow<List<User>> {
        return userRepository.getAllUsers()
    }
}


