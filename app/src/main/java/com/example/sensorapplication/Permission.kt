package com.example.sensorapplication


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class Permission : AppCompatActivity(),ActivityCompat.OnRequestPermissionsResultCallback {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        var button = findViewById<Button>(R.id.approve_permission_request)
        button.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                88
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val permissionResult = "Request code: " + requestCode.toString() + ", Permissions: " +
                permissions.contentToString() + ", Results: " + grantResults.contentToString()

        Log.d("Permission", "onRequestPermissionsResult(): $permissionResult")

        if (requestCode == 88) {
            Log.d("requestCode", "OK")
            finish()
        }
    }

}