package it.cinellialessia.yahtzee.db

import androidx.room.*

// Class that contains the methods to access the database
@Dao
interface UserDao {

    @Query("SELECT * FROM user ORDER BY id DESC")
    fun getAllUserInfo(): List<User>? // function to get the list of a user's dataset

    @Insert
    fun insertUser(user: User?) // function to insert datasets into the database

    @Delete
    fun deleteUser(user: User?) // function to delete datasets from the database

    @Query("SELECT * FROM User WHERE mod LIKE :search ORDER BY id DESC")
    fun loadMod(search: String): MutableList<User> // query to filter by game mode
}