package com.example.sensorapplication


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class Permission : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        var button = findViewById<Button>(R.id.approve_permission_request)
        button.setOnClickListener {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.activity_rationale),
                88,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        val permissionResult = "Request code: " + requestCode.toString() + ", Permissions: " +
                permissions.contentToString() + ", Results: " + grantResults.contentToString()

        Log.d("Permission", "onRequestPermissionsResult(): $permissionResult")

        if (requestCode == 88) {
            Log.d("requestCode", "OK")
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        TODO("Not yet implemented")
        Log.d("permission", "OK")
        finish()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
//        TODO("Not yet implemented")
        Log.d("permission", "no")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

}