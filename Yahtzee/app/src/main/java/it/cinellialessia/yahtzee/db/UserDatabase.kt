package it.cinellialessia.yahtzee.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//The "Database" annotation marks the class as a database.
//To use the Room library, we acquire an instance of the RoomDatabase class via Room.databaseBuilder
@Database(entities = [User::class], version = 1)
abstract class UserDatabase: RoomDatabase() {

    // Abstract method with no arguments that returns the class annotated with @Dao.
    abstract fun userDao(): UserDao?

    companion object {
        private var INSTANCE: UserDatabase?= null

        fun getAppDatabase(context: Context): UserDatabase? {
            if(INSTANCE == null ) {

                INSTANCE = Room.databaseBuilder(
                    context.applicationContext, UserDatabase::class.java, "App"
                ).build()

            }
            return INSTANCE
        }
    }
}