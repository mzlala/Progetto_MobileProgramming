package it.cinellialessia.yahtzee.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// The database table has 5 columns: id (primary key), score, player, game mode and date.
// "user" is the name of the database table
// The "@Entity" annotation creates this class as a room entity.
// The "@PrimaryKey" annotation is used to create the primary key which is self-incrementing with each new insertion
// The "@ColumnInfo" annotation allows you to specify custom column information
@Entity(tableName = "user")
data class User(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Int = 0,
        @ColumnInfo(name = "score") val score: String,
        @ColumnInfo(name = "player") val player: String,
        @ColumnInfo(name = "mod") val mod: String,
        @ColumnInfo(name = "date") val date: String
)

