package com.example.sensorapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity(){

    private lateinit var image: ImageView
    private lateinit var activityDesc: TextView
    private lateinit var transitions: MutableList<ActivityTransition>
    private lateinit var client: ActivityRecognitionClient
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("here", "here")
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION)
        ){
            var intent = Intent(this, Permission::class.java)
            startActivity(intent)
        }
        Log.d("check", "check")

        image = findViewById(R.id.imageView)
        activityDesc = findViewById(R.id.activityDesc)
        buildTransitions()

        val request = ActivityTransitionRequest(transitions)

        client = ActivityRecognition.getClient(this)
        client.requestActivityTransitionUpdates(request, getPendingIntent())
                .addOnSuccessListener {
                    Log.d("success", "success")
                }
                .addOnFailureListener {
                    Log.d("failed", "failed")
                }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStop() {
        super.onStop()
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION)
        ){
            Log.d("onStopClient", "cancel")
            client
                .removeActivityTransitionUpdates(getPendingIntent())
                .addOnSuccessListener {
                    getPendingIntent().cancel()
                }
        }
        Log.d("onStop", "cancel")

    }

//https://developer.android.com/reference/android/app/PendingIntent#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int)
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
    Log.d("MainActivity", "Broadcast")
        return PendingIntent.getBroadcast(this, 0,
            Intent(this, InfoReceiver::class.java),PendingIntent.FLAG_IMMUTABLE)
        }

    private fun buildTransitions(){
        transitions = mutableListOf()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
    }


    @SuppressLint("SetTextI18n")
    private fun setActivity(activity: ActivityTransitionEvent){
        Log.d("MainActivity", "setActivity")
        when(activity.activityType){
            DetectedActivity.STILL -> {
                activityDesc.text = "STILL"
                image.setImageResource(R.drawable.still)
            }
            DetectedActivity.WALKING -> {
                activityDesc.text = "WALKING"
                image.setImageResource(R.drawable.walking)
            }
            DetectedActivity.IN_VEHICLE -> {
                activityDesc.text = "IN_VEHICLE"
                image.setImageResource(R.drawable.driving)
            }
            DetectedActivity.RUNNING -> {
                activityDesc.text = "RUNNING"
                image.setImageResource(R.drawable.running)
            }
        }
    }

    inner class InfoReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity", "onReceive")
            if (ActivityTransitionResult.hasResult(intent)) {
                val result = intent?.let { ActivityTransitionResult.extractResult(it) }
                Log.d("MainActivity", "Notnull")
                if (result != null) {
                    for (event in result.transitionEvents) {
                        setActivity(event)
                    }
                }
            }
        }
    }
}