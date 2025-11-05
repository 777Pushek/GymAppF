package com.example.gymappfrontendui.repository

import android.content.Context
import android.util.Base64.URL_SAFE
import android.util.Base64.decode

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb

import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.relationships.UserWithBodyMeasurements
import com.example.gymappfrontendui.db.relationships.UserWithExercises
import com.example.gymappfrontendui.db.relationships.UserWithExercisesAndMuscleGroups
import com.example.gymappfrontendui.db.relationships.UserWithExercisesAndSets
import com.example.gymappfrontendui.db.relationships.UserWithWorkoutTemplates
import com.example.gymappfrontendui.db.relationships.UserWithWorkouts
import com.example.gymappfrontendui.models.AccountType
import com.example.gymappfrontendui.network.ApiClient
import com.example.gymappfrontendui.network.TokenManager
import com.example.gymappfrontendui.network.dto.reguest.AddEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.ChangePasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.ChangeUsernameRequest
import com.example.gymappfrontendui.network.dto.reguest.DeleteEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.ForgotPasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.GoogleLoginRequest
import com.example.gymappfrontendui.network.dto.reguest.LoginRequest
import com.example.gymappfrontendui.network.dto.reguest.RegisterRequest
import com.example.gymappfrontendui.network.dto.reguest.ResetPasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.VerifyEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.VerifyResetCodeRequest
import com.example.gymappfrontendui.network.dto.response.UserMeResponse

import org.json.JSONObject


class UserRepository(context: Context){
    private val userDao = AppDb.getInstance(context).userDao()
    private val api = ApiClient.create(context)


    private val tokenManager = TokenManager(context)
    companion object {
        private const val TAG = "UserRepository"
    }

    suspend fun getUserInfo(): UserMeResponse? {
        return try {
            api.getUserInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Get User Info failed: ${e.message}")
            null
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = api.login(LoginRequest(username = username, password = password))
            tokenManager.saveToken(response.token)
            val userInfo = getUserInfo()
            if (userInfo != null) {

                val localUser = userDao.getUserByUsernameAndType(username=username, accountType = AccountType.CLASSIC)
                if (localUser != null) {
                    userDao.updateUser(localUser.copy(
                        userName = userInfo.userName,
                        email = userInfo.email,
                        emailVerified = userInfo.emailVerified,
                        isLoggedIn = true
                    ))
                } else {
                    userDao.insertUser(
                        User(
                            email = userInfo.email,
                            userName = userInfo.userName,
                            emailVerified = userInfo.emailVerified,
                            isLoggedIn = true,
                            accountType = AccountType.CLASSIC
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")

            false
        }
    }


    suspend fun loginWithGoogle(idToken: String): Boolean {
        return try {

            val parts = idToken.split(".")
            val payload = String(decode(parts[1], URL_SAFE))
            val json = JSONObject(payload)
            val email = json.getString("email")
            val username = json.getString("name")

            val response = api.loginWithGoogle(GoogleLoginRequest(idToken))
            tokenManager.saveToken(response.token)

            val localUser = userDao.getUserByUsernameAndType(username = username, accountType = AccountType.GOOGLE)
            if (localUser != null) {
                userDao.updateUser(localUser.copy(isLoggedIn = true))
            } else {
                userDao.insertUser(
                    User(
                        email = email,
                        userName = username,
                        isLoggedIn = true,
                        emailVerified = true,
                        accountType = AccountType.GOOGLE
                    )
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Google Login failed: ${e.message}")
            false
        }
    }

    suspend fun register(username: String, password: String): Boolean{
        return try {
            val response = api.register(RegisterRequest(
                username = username,
                password = password
            ))
            tokenManager.saveToken(response.token)
            userDao.insertUser(User(

                email = null,
                userName = username,
                isLoggedIn = true,
                accountType = AccountType.CLASSIC
            ))
            true
        }catch (e: Exception){
            Log.e(TAG, "Register failed: ${e.message}")
            false
        }

    }
    suspend fun changeUserName(currentPassword: String,username: String): Boolean{
        return try {
                api.changeUserName(ChangeUsernameRequest(
                    username = username,
                    password = currentPassword
                ))
            val user = userDao.getLoggedInUser()
            userDao.updateUser(user!!.copy(userName = username))

            true
        }catch (e: Exception){
            Log.e(TAG, "Change User Name failed: ${e.message}")
            false
        }

    }
    suspend fun getEmailWithForgotPasswordCode(username: String): Boolean{
        return try {
            api.getEmailForgotPasswordCode(ForgotPasswordRequest(
                username = username
            ))
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
    suspend fun changeUserPassword(currentPassword: String,newPassword: String): Boolean{
        return try {
            api.changeUserPassword(ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            ))
            true

        }catch (e: Exception){
            Log.e(TAG, "Change User password failed: ${e.message}")
            false
        }
    }
    suspend fun verifyResetPasswordCode(userName: String, code: String): Boolean{
        return try {
            val response = api.verifyResetPasswordCode(VerifyResetCodeRequest(
                username = userName,
                code = code
            ))
            tokenManager.saveTemporaryToken(response.token)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Verify Reset Password Code failed: ${e.message}")
            false
        }
    }

    suspend fun resetPassword(password:String): Boolean{
        return try {
            val tempToken = tokenManager.getTemporaryToken() ?: return false
            api.resetPassword(token = "Bearer $tempToken", request = ResetPasswordRequest(
                password = password
            ))
            tokenManager.clearTemporaryToken()
            true
        }catch (e: Exception){
            Log.e(TAG, "Reset Password failed: ${e.message}")
            false
        }
    }
    suspend fun getEmailVerificationCode(): Boolean{
        return try {
            api.getEmailVerificationCode()
            true
        }catch (e: Exception){
            Log.e(TAG, "Get Email Verification Code failed: ${e.message}")
            false
        }
    }
    suspend fun addEmail(currentPassword:String,email: String): Boolean{
        return try {
            api.addEmail(AddEmailRequest(
                email = email,
                password = currentPassword
            ))
            val user = userDao.getLoggedInUser()
            userDao.updateUser(user!!.copy(email = email))
            true
        }catch (e: Exception){
            Log.e(TAG, "Add Email failed: ${e.message}")
            false
        }
        
    }
    suspend fun verifyEmail(code: String): Boolean{
        return try {
            api.verifyEmail(VerifyEmailRequest(
                code = code
            ))
            val user = userDao.getLoggedInUser()
            userDao.updateUser(user!!.copy(emailVerified = true))
            true
        }catch (e: Exception){
            Log.e(TAG, "Verify Email failed: ${e.message}")
            false
        }
    }
    suspend fun deleteUserEmail(password: String): Boolean{
        return try {
            api.deleteUserEmail(DeleteEmailRequest(
                password = password
            ))
            val user = userDao.getLoggedInUser()
            userDao.updateUser(user!!.copy(email = null))
            true
        }catch (e: Exception){
            Log.e(TAG, "Delete User Email failed: ${e.message}")
            false
        }
    }

    suspend fun logout(): Boolean {
        return try {
            api.logout()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed: ${e.message}")
            false
        } finally {
            tokenManager.clearToken()
            val user = userDao.getLoggedInUser()
            userDao.updateUser(user!!.copy(isLoggedIn = false))
        }
    }
    fun getGuestUserIdFlow(): Flow<Int?>  {
        return userDao.getGuestUserIdFlow()
    }

    suspend fun getLoggedInUserId(): Int? {
        return userDao.getLoggedInUserId()
    }
    suspend fun getGuestUserId(): Int {
        return userDao.getGuestUserId()
    }

    suspend fun updateLastSync(lastSync: String) {
        userDao.updateLastSync(lastSync)
    }

     fun getLoggedInUserIdFlow(): Flow<Int?>  {
        return userDao.getLoggedInUserIdFlow()
    }


    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO){
        val newId = userDao.insertUser(user)
        newId
    }

    suspend fun insertAllUsers(users: List<User>): List<Long> = withContext(Dispatchers.IO){
        val newId = userDao.insertAllUsers(users)
        newId
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO){
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO){
        userDao.deleteUser(user)
    }

    fun getUserById(id: Int): Flow<User> {
        return userDao.getUserById(id)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    fun getUserWithWorkoutsById(id: Int): Flow<UserWithWorkouts> {
        return userDao.getUserWithWorkoutsById(id)
    }

    fun getUserWithExercisesById(id: Int): Flow<UserWithExercises> {
        return userDao.getUserWithExercisesById(id)
    }

    fun getUserWithWorkoutTemplatesById(id: Int): Flow<UserWithWorkoutTemplates> {
        return userDao.getUserWithWorkoutTemplatesById(id)
    }

    fun getUserWithBodyMeasurementsById(id: Int): Flow<UserWithBodyMeasurements> {
        return userDao.getUserWithBodyMeasurementsById(id)
    }


    fun getUserWithExercisesAndMuscleGroupsById(id: Int): Flow<UserWithExercisesAndMuscleGroups> {
        return userDao.getUserWithExercisesAndMuscleGroupsById(id)
    }

    fun getUserWithExercisesAndSetsById(id: Int): Flow<UserWithExercisesAndSets> {
        return userDao.getUserWithExercisesAndSetsById(id)
    }
}
