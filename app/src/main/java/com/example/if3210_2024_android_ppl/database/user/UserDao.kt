package com.example.if3210_2024_android_ppl.database.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun addUser(user: User)

    @Query("SELECT * FROM user_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<User>>

    @Query("SELECT token FROM user_table WHERE email = :email")
    suspend fun getTokenByEmail(email: String): String?

    @Query("UPDATE user_table SET isActive = CASE WHEN id = :userId THEN 1 ELSE 0 END")
    suspend fun setActiveUser(userId: Int)
}