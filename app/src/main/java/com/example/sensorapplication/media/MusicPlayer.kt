package com.example.sensorapplication.media

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.util.Log
import com.example.sensorapplication.R


// handle background music
class MusicPlayer(c: Context){
    var context: Context = c
    private var player: MediaPlayer? = null

    fun start() {
        stop()
//        player =
        player = MediaPlayer.create(context,R.raw.god)
        Log.d("Music", context.toString())
        player?.setOnPreparedListener(OnPreparedListener {
            Log.i(
                "Music",
                "player: $player"
            )
        })
        player?.isLooping = false
        player?.setVolume(1F, 1F)
        Log.d("Music", "CREATE")
//        player.prepare()
        player?.start()
        Log.d("Music", "AFTER CREATE")
    }
    fun stop() {
        Log.d("Music", "STOP")

        if(player!=null && player?.isPlaying == true){
            Log.d("Music", "ENTER STOP")
            player?.stop()
            player?.release()
            player = null
        }
    }
}