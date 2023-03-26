package com.example.sensorapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.sensorapplication.db.LogTable
import com.example.sensorapplication.db.Repository
import com.example.sensorapplication.db.TrackViewModel
import com.example.sensorapplication.db.TrackViewModelFactory
import com.example.sensorapplication.media.MusicPlayer
//import com.example.sensorapplication.media.MusicService
import com.google.android.gms.location.*
import java.lang.Math.abs
import java.text.DateFormat
import java.util.*
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var image: ImageView
    private lateinit var activityDesc: TextView
    private lateinit var encouragingMsg: TextView
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var svc: Intent
    private var mAccelCurrent = 0.0
    private var mAccelPrevious = 0.0
    private val trackViewModel: TrackViewModel by viewModels {
        TrackViewModelFactory(Repository((application as SensorApplication).database.trackDao()))
    }
    private val TAG: String = "com.example.sensorapplication.broadcast"
//    private lateinit var mInfoReceiver: InfoReceiver
    private lateinit var mSensorMgr: SensorManager
    private lateinit var mSensor: Sensor
    private var currActivity = "STILL"
    private var prevActivity = ""
    private var mAccel: Double = 0.0
    private lateinit var player: MusicPlayer
    private val THRESHOLDWALKING = 1
    private val THRESHOLDRUNNING = 5
    private val THRESHOLDDRIVING = 15
    private val INTERVAL = 150
    private var count = 0
    private var sum = 0.0


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "here")
//        client = ActivityRecognition.getClient(this)
//        mInfoReceiver = InfoReceiver()
//        if(!activityRecognitionPermissionApproved()){
//            var intent = Intent(this, Permission::class.java)
//            startActivity(intent)
//        }
        player = MusicPlayer(this)
        mSensorMgr = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        image = findViewById(R.id.imageView)
        activityDesc = findViewById(R.id.activityDesc)
        encouragingMsg = findViewById(R.id.msg1)

        mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        this.deleteDatabase("appDatabase.db")
    }

//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun activityRecognitionPermissionApproved(): Boolean {
//
//        // TODO: Review permission check for 29+.
//        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACTIVITY_RECOGNITION)
//    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
//            buildTransitions()
        mSensorMgr.registerListener(this,
            mSensor,
            SensorManager.SENSOR_DELAY_FASTEST,
            SensorManager.SENSOR_DELAY_FASTEST)


//        val request = ActivityTransitionRequest(transitions)
//        Log.d("MainActivity", transitions.toString())
//        registerReceiver(mInfoReceiver, IntentFilter(TAG))

//        if (activityRecognitionPermissionApproved()) {
//            client.requestActivityTransitionUpdates(request, getPendingIntent())
//                .addOnSuccessListener {
//                    Log.d("MainActivity", "success")
////                    sendFakeActivityTransitionEvent()
//                }
//                .addOnFailureListener {
//                    Log.d("MainActivity", "failed")
//                }
//            }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStop() {
        super.onStop()

        mSensorMgr.unregisterListener(this)
        Log.d("onStop", "cancel")
    }


//https://developer.android.com/reference/android/app/PendingIntent#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int)
//    @RequiresApi(Build.VERSION_CODES.S)
//    @SuppressLint("UnspecifiedImmutableFlag")
//    private fun getPendingIntent(): PendingIntent {
//    Log.d("MainActivity", "PendingIntent")
//        return PendingIntent.getBroadcast(this, 0,
//            Intent(TAG), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
//        }
//
//    private fun buildTransitions(){
//        transitions = mutableListOf()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.IN_VEHICLE)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.IN_VEHICLE)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.RUNNING)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.RUNNING)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.WALKING)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.WALKING)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.STILL)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//            .build()
//        transitions += ActivityTransition.Builder()
//            .setActivityType(DetectedActivity.STILL)
//            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
//            .build()
//    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun finishActivity(){
        if(currActivity != prevActivity){
            endTime = System.currentTimeMillis()
            val timeDiff = endTime - startTime
            Log.d("TimeDiff", timeDiff.toString())

            var minutes: Long = timeDiff / 1000 / 60
            if(minutes < 1) minutes = 0L

            val seconds = (timeDiff / 1000 % 60)
            val milseconds = (timeDiff / 1000 )

            val str = "You just did $prevActivity for $minutes minutes $seconds seconds"
            Log.d("String", str)
            makeToast(str)
//            val sdf = SimpleDateFormat("MMM dd,yyyy", )
            val date = DateFormat.getDateInstance().format( startTime);
            val time = DateFormat.getTimeInstance().format( startTime);

            Log.d("TIME",time.toString())
            Log.d("TIME",date.toString())
            trackViewModel.insertTrack(LogTable(date = date.toString(), time = time.toString(), duration = "$minutes", activity = prevActivity))
        }else{
            Log.d("MainActivity", currActivity)
        }
    }
    private fun makeToast(msg:String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
//    inner class InfoReceiver : BroadcastReceiver() {
//        @RequiresApi(Build.VERSION_CODES.O)
//        override fun onReceive(context: Context?, intent: Intent?) {
//            Log.d("MainActivity", "onReceive(): $intent")
//            if (ActivityTransitionResult.hasResult(intent)) {
//                val result = intent?.let { ActivityTransitionResult.extractResult(it) }
//                Log.d("MainActivity", "Notnull")
//                if (result != null) {
//                    for (event in result.transitionEvents) {
//                        this@MainActivity.setActivity(event)
//                        this@MainActivity.activityDesc.text = this@MainActivity.getActivity(event)
//                    }
//                }
//            }
//        }
//    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent) {
        if(event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            val x_acc = event.values[0]
            val y_acc = event.values[1]
            val z_acc = event.values[2]
//            Log.d("axis", "$x_acc, $y_acc, $z_acc")
            mAccelPrevious = mAccelCurrent
            mAccelCurrent = sqrt((x_acc * x_acc + y_acc * y_acc + z_acc * z_acc).toDouble());
            val delta = abs(mAccelCurrent - mAccelPrevious)

            mAccel = mAccel * 0.9f + delta
            // need some times to record what user is doing
            if(count < INTERVAL){
                count++
                sum += mAccel
            }
            else{
                // decide the threshold
                sum /= INTERVAL
                if (sum > THRESHOLDDRIVING) {
                    encouragingMsg.text = ""
                    player.stop()
                    //when prev != curr which means it enters 'IN VEHICLE' from other activities
                    if(prevActivity != currActivity) {
                    startTime = System.currentTimeMillis()
                    image.setImageResource(R.drawable.driving)
                    activityDesc.text = "IN VEHICLE"
                }
                prevActivity = currActivity
                currActivity = "IN VEHICLE"
//              update prevA before currA so we can insert old activity in this#finishA() function
                finishActivity()
                Log.d("TAG", "DRIVING")
            } else if (sum < THRESHOLDDRIVING && sum > THRESHOLDRUNNING) {
                    //when prev != curr which means it enters 'RUNNING' from other activities
                    if(prevActivity != currActivity) {
                        encouragingMsg.text = ""
                        startTime = System.currentTimeMillis()
                        image.setImageResource(R.drawable.running)
                        activityDesc.text = "RUNNING"
                        player.start()
                }
                prevActivity = currActivity
                currActivity = "RUNNING"
                finishActivity()
                Log.d("TAG", "RUNNING")
            }else if (sum < THRESHOLDRUNNING && sum > THRESHOLDWALKING) {
                player.stop()
                    //when prev != curr which means it enters 'WALKING' from other activities
                    if(prevActivity != currActivity) {
                    startTime = System.currentTimeMillis()
                    image.setImageResource(R.drawable.walking)
                    activityDesc.text = "WALKING"
                    encouragingMsg.text = "You are doing great!"

                }
                prevActivity = currActivity
                currActivity = "WALKING"
                finishActivity()
                Log.d("TAG", "WALKING")
            }else{
                player.stop()
                 //when prev != curr which means it enters 'STILL' from other activities
                if(prevActivity != currActivity) {
                    encouragingMsg.text = ""
                    startTime = System.currentTimeMillis()
                    image.setImageResource(R.drawable.still)
                    activityDesc.text = "STILL"

                }
                prevActivity = currActivity
                currActivity = "STILL"
                finishActivity()
                Log.d("TAG", "STILL")
            }
                count = 0
                sum = 0.0
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}