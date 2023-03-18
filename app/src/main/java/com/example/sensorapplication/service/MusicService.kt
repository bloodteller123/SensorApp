package com.example.sensorapplication.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.sensorapplication.R


// handle background music
class MusicService : Service(){
    lateinit var player: MediaPlayer

    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }
    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.bg)
        player.isLooping = true
        player.setVolume(1F, 1F)
        Log.d("Music", "CREATE")
    }
    //Called by the system every time a client explicitly starts the service by calling Context.startService(Intent),
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Music", "Music")
        player.start()
        return START_STICKY
    }
    override fun onDestroy() {
        Log.d("Music", "onDestroy")
        player.stop()
        player.release()
    }
}