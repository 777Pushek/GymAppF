package com.example.gymappfrontendui.network

import com.example.gymappfrontendui.network.dto.reguest.AddEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.BodyMeasurementRequest
import com.example.gymappfrontendui.network.dto.reguest.ChangePasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.ChangeUsernameRequest
import com.example.gymappfrontendui.network.dto.reguest.DeleteEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.ExerciseRequest
import com.example.gymappfrontendui.network.dto.reguest.ForgotPasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.GoogleLoginRequest
import com.example.gymappfrontendui.network.dto.reguest.LoginRequest
import com.example.gymappfrontendui.network.dto.response.LoginResponse
import com.example.gymappfrontendui.network.dto.response.LogoutResponse
import com.example.gymappfrontendui.network.dto.response.RefreshResponse
import com.example.gymappfrontendui.network.dto.reguest.RegisterRequest
import com.example.gymappfrontendui.network.dto.reguest.ResetPasswordRequest
import com.example.gymappfrontendui.network.dto.reguest.VerifyEmailRequest
import com.example.gymappfrontendui.network.dto.reguest.VerifyResetCodeRequest
import com.example.gymappfrontendui.network.dto.reguest.WeekScheduleRequest
import com.example.gymappfrontendui.network.dto.reguest.WorkoutRequest
import com.example.gymappfrontendui.network.dto.reguest.WorkoutTemplatesRequest
import com.example.gymappfrontendui.network.dto.response.AddEmailResponse
import com.example.gymappfrontendui.network.dto.response.AddResponse
import com.example.gymappfrontendui.network.dto.response.UpdateResponse
import com.example.gymappfrontendui.network.dto.response.ChangePasswordResponse
import com.example.gymappfrontendui.network.dto.response.ChangeUsernameResponse
import com.example.gymappfrontendui.network.dto.response.DeleteEmailResponse
import com.example.gymappfrontendui.network.dto.response.DeleteResponse
import com.example.gymappfrontendui.network.dto.response.ForgotPasswordResponse
import com.example.gymappfrontendui.network.dto.response.GetBodyMeasurementsResponse
import com.example.gymappfrontendui.network.dto.response.GetExercisesResponse
import com.example.gymappfrontendui.network.dto.response.GetWeekSchedulesResponse
import com.example.gymappfrontendui.network.dto.response.GetWorkoutTemplatesResponse
import com.example.gymappfrontendui.network.dto.response.GetWorkoutsResponse
import com.example.gymappfrontendui.network.dto.response.LoginWithGoogleResponse
import com.example.gymappfrontendui.network.dto.response.RegisterResponse
import com.example.gymappfrontendui.network.dto.response.ResetPasswordResponse
import com.example.gymappfrontendui.network.dto.response.UserLastSyncResponse
import com.example.gymappfrontendui.network.dto.response.UserMeResponse
import com.example.gymappfrontendui.network.dto.response.VerifyEmailResponse
import com.example.gymappfrontendui.network.dto.response.VerifyResetCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/logout")
    suspend fun logout(): LogoutResponse
    @POST("auth/refresh")
    fun refreshToken( ): Call<RefreshResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginWithGoogleResponse

    @PATCH("users/me/password")
    suspend fun changeUserPassword(@Body request: ChangePasswordRequest): ChangePasswordResponse

    @PATCH("users/me/username")
    suspend fun changeUserName(@Body request: ChangeUsernameRequest): ChangeUsernameResponse

    @GET("auth/verify-email")
    suspend fun getEmailVerificationCode()

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): VerifyEmailResponse

    @POST("auth/verify-reset-code")
    suspend fun verifyResetPasswordCode(@Body request: VerifyResetCodeRequest): VerifyResetCodeResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Header("Authorization") token: String,
                              @Body request: ResetPasswordRequest): ResetPasswordResponse

    @POST("auth/forgot-password")
    suspend fun getEmailForgotPasswordCode(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @PATCH("users/me/email")
    suspend fun addEmail(@Body request: AddEmailRequest): AddEmailResponse

    @DELETE("users/me/email")
    suspend fun deleteUserEmail(@Body request: DeleteEmailRequest): DeleteEmailResponse


    @GET("users/me/last-sync")
    suspend fun getUserLastSync(): UserLastSyncResponse

    @GET("users/me")
    suspend fun getUserInfo(): UserMeResponse

    @POST("bodyMeasurements")
    suspend fun addBodyMeasurement(
        @Body request: BodyMeasurementRequest,
        @Header("X-Last-Sync") lastSync: String
    ): AddResponse

    @PUT("bodyMeasurements/{id}")
    suspend fun updateBodyMeasurement(
        @Path("id") id: Int,
        @Body request: BodyMeasurementRequest,
        @Header("X-Last-Sync") lastSync: String
    ): UpdateResponse

    @DELETE("bodyMeasurements/{id}")
    suspend fun deleteBodyMeasurement(
        @Path("id") id: Int,
        @Header("X-Last-Sync") lastSync: String
    ): DeleteResponse

    @GET("bodyMeasurements")
    suspend fun getUserMeasurements(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String
    ): GetBodyMeasurementsResponse

    //========================================= exercises =====================================
    @POST("exercises")
    suspend fun addExercise(
        @Body request: ExerciseRequest,
        @Header("X-Last-Sync") lastSync: String
    ): AddResponse

    @PUT("exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: Int,
        @Body request: ExerciseRequest,
        @Header("X-Last-Sync") lastSync: String
    ): UpdateResponse

    @DELETE("exercises/{id}")
    suspend fun deleteExercise(
        @Path("id") id: Int,
        @Header("X-Last-Sync") lastSync: String
    ): DeleteResponse

    @GET("exercises")
    suspend fun getExercises(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String
    ): GetExercisesResponse



    //========================================= workoutTemplate =====================================
    @POST("workoutTemplates")
    suspend fun addWorkoutTemplate(
        @Body request: WorkoutTemplatesRequest,
        @Header("X-Last-Sync") lastSync: String
    ): AddResponse

    @PUT("workoutTemplates/{id}")
    suspend fun updateWorkoutTemplate(
        @Path("id") id: Int,
        @Body request: WorkoutTemplatesRequest,
        @Header("X-Last-Sync") lastSync: String
    ): UpdateResponse

    @DELETE("workoutTemplates/{id}")
    suspend fun deleteWorkoutTemplate(
        @Path("id") id: Int,
        @Header("X-Last-Sync") lastSync: String
    ): DeleteResponse

    @GET("workoutTemplates")
    suspend fun getUserWorkoutTemplates(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String
    ): GetWorkoutTemplatesResponse

    //========================================= workout =====================================
    @POST("workouts")
    suspend fun addWorkout(
        @Body request: WorkoutRequest,
        @Header("X-Last-Sync") lastSync: String
    ): AddResponse

    @PUT("workouts/{id}")
    suspend fun updateWorkout(
        @Path("id") id: Int,
        @Body request: WorkoutRequest,
        @Header("X-Last-Sync") lastSync: String
    ): UpdateResponse

    @DELETE("workouts/{id}")
    suspend fun deleteWorkout(
        @Path("id") id: Int,
        @Header("X-Last-Sync") lastSync: String
    ): DeleteResponse

    @GET("workouts")
    suspend fun getUserWorkouts(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 5,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String
    ): GetWorkoutsResponse
    //========================================= week schedule =====================================
    @POST("weekSchedules")
    suspend fun addWeekSchedule(
        @Body request: WeekScheduleRequest,
        @Header("X-Last-Sync") lastSync: String
    ): AddResponse

    @PUT("weekSchedules/{id}")
    suspend fun updateWeekSchedule(
        @Path("id") id: Int,
        @Body request: WeekScheduleRequest,
        @Header("X-Last-Sync") lastSync: String
    ): UpdateResponse

    @DELETE("weekSchedules/{id}")
    suspend fun deleteWeekSchedule(
        @Path("id") id: Int,
        @Header("X-Last-Sync") lastSync: String
    ): DeleteResponse

    @GET("weekSchedules")
    suspend fun getUserWeekSchedules(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String
    ): GetWeekSchedulesResponse



}