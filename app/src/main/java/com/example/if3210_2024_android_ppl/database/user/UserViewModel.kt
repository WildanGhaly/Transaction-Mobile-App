package com.example.if3210_2024_android_ppl.database.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application): AndroidViewModel(application) {

    private val readAllData: LiveData<List<User>>
    private  val repository: UserRepository

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllData = repository.readAllData
    }

    fun addUser(user: User){
        viewModelScope.launch(Dispatchers.IO){
            repository.addUser(user)
        }
    }

    fun getTokenByEmail(email: String, callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = repository.getTokenByEmail(email)
            withContext(Dispatchers.Main) {
                callback(token)
            }
        }
    }

    fun getActiveUserEmail(callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = repository.getActiveUserEmail()
            withContext(Dispatchers.Main) {
                callback(email)
            }
        }
    }

}