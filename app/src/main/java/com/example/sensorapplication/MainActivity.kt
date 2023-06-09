package com.example.sensorapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sensorapplication.db.LogTable
import com.example.sensorapplication.db.Repository
import com.example.sensorapplication.db.TrackViewModel
import com.example.sensorapplication.db.TrackViewModelFactory
import com.example.sensorapplication.service.MusicService
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer
import com.google.android.gms.location.*
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity(){

    private lateinit var image: ImageView
    private lateinit var activityDesc: TextView
    private lateinit var encouragingMsg: TextView
    private lateinit var transitions: MutableList<ActivityTransition>
    private lateinit var client: ActivityRecognitionClient
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var svc: Intent
    private val trackViewModel: TrackViewModel by viewModels {
        TrackViewModelFactory(Repository((application as SensorApplication).database.trackDao()))
    }
    private val TAG: String = "com.example.sensorapplication.broadcast"
    private lateinit var mInfoReceiver: InfoReceiver
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "here")
        client = ActivityRecognition.getClient(this)
        mInfoReceiver = InfoReceiver()
        if(!activityRecognitionPermissionApproved()){
            var intent = Intent(this, Permission::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun activityRecognitionPermissionApproved(): Boolean {

        // TODO: Review permission check for 29+.
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()

            image = findViewById(R.id.imageView)
            activityDesc = findViewById(R.id.activityDesc)
            encouragingMsg = findViewById(R.id.msg1)
            buildTransitions()

            svc = Intent(this, MusicService::class.java)

            val request = ActivityTransitionRequest(transitions)
            Log.d("MainActivity", transitions.toString())
            registerReceiver(mInfoReceiver, IntentFilter(TAG))

        if (activityRecognitionPermissionApproved()) {
            client.requestActivityTransitionUpdates(request, getPendingIntent())
                .addOnSuccessListener {
                    Log.d("MainActivity", "success")
                    sendFakeActivityTransitionEvent()
                }
                .addOnFailureListener {
                    Log.d("MainActivity", "failed")
                }
        }

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mInfoReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStop() {
        super.onStop()
        if(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
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

    private fun sendFakeActivityTransitionEvent() {
        Log.d("MainActivity", "sendFeedback")
        // name your intended recipient class
        val intent = Intent(TAG)

        var transitionEvent: ActivityTransitionEvent
        val events: ArrayList<ActivityTransitionEvent> = arrayListOf()
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.STILL,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER, 10000000000
        )
        events.add(transitionEvent)
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.STILL,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT, 10000000000
        )
        events.add(transitionEvent)
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.WALKING,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER, 10000000000
        )
        events.add(transitionEvent)
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.WALKING,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT, 10000000000
        )
        events.add(transitionEvent)
        transitionEvent = ActivityTransitionEvent(
            DetectedActivity.RUNNING,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER, 10000000000
        )
        events.add(transitionEvent)

        // finally, serialize and send
        val result = ActivityTransitionResult(events)
        SafeParcelableSerializer.serializeToIntentExtra(
            result,
            intent,
            "com.google.android.location.internal.EXTRA_ACTIVITY_TRANSITION_RESULT"
        )
        sendBroadcast(intent)
    }

//https://developer.android.com/reference/android/app/PendingIntent#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int)
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
    Log.d("MainActivity", "PendingIntent")
        return PendingIntent.getBroadcast(this, 0,
            Intent(TAG), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
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


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setActivity(activity: ActivityTransitionEvent){
        Log.d("MainActivity", "setActivity")
        if(activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
            endTime = System.currentTimeMillis()
            val timeDiff = endTime - startTime
            Log.d("MainActivity", timeDiff.toString())

            val minutes: Long = timeDiff / 1000 / 60
            val seconds = (timeDiff / 1000 % 60)
            val act: String = getActivity(activity)
            val str = "You just did $act for $minutes:$seconds"
            makeToast(str)
//            trackViewModel.insertTrack(LogTable(day = LocalDateTime.now().toString(), time = "$minutes:$seconds", activity = act))
        }else{
            startTime = System.currentTimeMillis()
            getActivity(activity)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getActivity(activity: ActivityTransitionEvent): String{
        encouragingMsg.text = ""
        when(activity.activityType){
            DetectedActivity.STILL -> {
                if(activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) image.setImageResource(R.drawable.still)
                return "STILL"
            }
            DetectedActivity.WALKING -> {
                if(activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                    image.setImageResource(R.drawable.walking)
                    encouragingMsg.text = "You are doing great!"
                }
                return "WALKING"
            }
            DetectedActivity.IN_VEHICLE -> {
                if(activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) image.setImageResource(R.drawable.driving)
                return "IN_VEHICLE"
            }
            DetectedActivity.RUNNING -> {
                    if(activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        startService(svc)
                        image.setImageResource(R.drawable.running)
                    }else{
                        stopService(svc)
                    }
                    return "RUNNING"
            }
        }
        return ""
    }
    private fun makeToast(msg:String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
    inner class InfoReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity", "onReceive(): $intent")
            if (ActivityTransitionResult.hasResult(intent)) {
                val result = intent?.let { ActivityTransitionResult.extractResult(it) }
                Log.d("MainActivity", "Notnull")
                if (result != null) {
                    for (event in result.transitionEvents) {
                        this@MainActivity.setActivity(event)
                        this@MainActivity.activityDesc.text = this@MainActivity.getActivity(event)
                    }
                }
            }
        }
    }
}