package com.example.if3210_2024_android_ppl.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_table",
    indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "token") val token: String?,
    @ColumnInfo(name = "isActive") val isActive: Boolean? = true
)