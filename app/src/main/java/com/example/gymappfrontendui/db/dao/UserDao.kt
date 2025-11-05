package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.relationships.UserWithBodyMeasurements
import com.example.gymappfrontendui.db.relationships.UserWithExercises
import com.example.gymappfrontendui.db.relationships.UserWithExercisesAndMuscleGroups
import com.example.gymappfrontendui.db.relationships.UserWithExercisesAndSets
import com.example.gymappfrontendui.db.relationships.UserWithWorkoutTemplates
import com.example.gymappfrontendui.db.relationships.UserWithWorkouts
import com.example.gymappfrontendui.models.AccountType

@Dao
interface UserDao {

    @Query("SELECT last_sync FROM users WHERE is_loggedIn = 1 LIMIT 1")
    suspend fun getLastSync(): String?

    @Query("SELECT user_id FROM users WHERE account_type = 'GUEST' LIMIT 1")
    suspend fun getGuestUserId(): Int

    @Query("SELECT user_id FROM users WHERE is_loggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserId(): Int?

    @Query("SELECT user_id FROM users WHERE account_type = 'GUEST' LIMIT 1")
    fun getGuestUserIdFlow(): Flow<Int>

    @Query("SELECT user_id FROM users WHERE is_loggedIn = 1 LIMIT 1")
    fun getLoggedInUserIdFlow(): Flow<Int?>

    @Query("UPDATE users SET last_sync = :lastSync WHERE is_loggedIn = 1")
    suspend fun updateLastSync(lastSync: String)

    @Query("SELECT * FROM users WHERE is_loggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<User>): List<Long>

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserById(id: Int): Flow<User>
    @Query("SELECT * FROM users WHERE user_name = :username AND account_type = :accountType LIMIT 1")
    suspend fun getUserByUsernameAndType(username: String, accountType: AccountType): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithWorkoutsById(id: Int): Flow<UserWithWorkouts>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithExercisesById(id: Int): Flow<UserWithExercises>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithWorkoutTemplatesById(id: Int): Flow<UserWithWorkoutTemplates>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithBodyMeasurementsById(id: Int): Flow<UserWithBodyMeasurements>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithExercisesAndMuscleGroupsById(id: Int): Flow<UserWithExercisesAndMuscleGroups>

    @Transaction
    @Query("SELECT * FROM users WHERE user_id = :id")
    fun getUserWithExercisesAndSetsById(id: Int): Flow<UserWithExercisesAndSets>


}