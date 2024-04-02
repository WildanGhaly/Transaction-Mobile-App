package com.example.if3210_2024_android_ppl.database.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.if3210_2024_android_ppl.api.LoginRequest
import com.example.if3210_2024_android_ppl.api.RetrofitInstance
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

    fun getActiveUserEmail(callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = repository.getActiveUserEmail()
            withContext(Dispatchers.Main) {
                callback(email)
            }
        }
    }

    fun getActiveUserId(callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.getActiveUserId()
            withContext(Dispatchers.Main){
                callback(id)
            }
        }
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO){
            repository.logout()
        }
    }

}