package com.example.sensorapplication

import android.app.Application
import com.example.sensorapplication.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SensorApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database is only created when it's needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this) }
}