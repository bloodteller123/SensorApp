package com.example.sensorapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "log_table")
data class LogTable(
//    @PrimaryKey(autoGenerate = true)
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "activityId")
    val activityId: Int = 0,

    val date: String,
    val time: String,
    val duration: String,
    @ColumnInfo(name = "activity")
    val activity: String
)