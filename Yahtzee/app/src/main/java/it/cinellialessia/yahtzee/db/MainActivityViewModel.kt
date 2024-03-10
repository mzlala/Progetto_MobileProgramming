package it.cinellialessia.yahtzee.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// In all functions we use the coroutines so that the code blocks
// are executed asynchronously without blocking the thread from which they are launched.
class MainActivityViewModel(app: Application): AndroidViewModel(app) {
    private var allUsers : MutableLiveData<List<User>> = MutableLiveData()

    init{
        getAllUsers()
    }

    fun getAllUsersObservers(): MutableLiveData<List<User>> {
        return allUsers
    }

    //it communicates with UserDatabase and UserDao to get a user's dataset
    private fun getAllUsers() {
        val userDao = UserDatabase.getAppDatabase((getApplication()))?.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val list = userDao?.getAllUserInfo()
            allUsers.postValue(list) // sets the value specified by the list variable in the list
        }
    }

    // communicate with UserDatabase and UserDao to insert datasets
    fun insertUserInfo(entity: User){
        val userDao = UserDatabase.getAppDatabase(getApplication())?.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            userDao?.insertUser(entity)
            getAllUsers()
        }
    }

    // communicate with UserDatabase and UserDao to delete datasets
    fun deleteUserInfo(entity: User){
        val userDao = UserDatabase.getAppDatabase(getApplication())?.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            userDao?.deleteUser(entity)
            getAllUsers()
        }
    }

    // communicate with UserDatabase and UserDao for
    // be able to filter by player name and game mode
    fun filterMod(p0: String?) {
        val userDao = UserDatabase.getAppDatabase(getApplication())?.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val list = userDao?.loadMod(String.format("%%%s%%", p0))
            allUsers.postValue(list)
        }
    }

}