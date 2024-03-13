package com.example.if3210_2024_android_ppl.database.user

import androidx.lifecycle.LiveData

class UserRepository(private  val userDao: UserDao) {

    val readAllData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User){
        userDao.addUser(user)
    }
}