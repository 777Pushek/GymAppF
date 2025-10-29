package com.example.gymappfrontendui.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gymappfrontendui.models.AccountType


@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val userId: Int = 0,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "is_loggedIn") val isLoggedIn: Boolean = false,
    @ColumnInfo(name = "last_sync") val lastSync: String? = null,
    @ColumnInfo(name = "email_verified") val emailVerified: Boolean = false,
    @ColumnInfo(name = "account_type") val accountType: AccountType

)

