package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymappfrontendui.db.entity.Language

@Dao
interface LanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: Language): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLanguages(languages: List<Language>): List<Long>

    @Query("SELECT * FROM languages WHERE name = :name LIMIT 1")
    suspend fun getLanguageByName(name: String): Language?
}