package com.example.sensorapplication.db

class Repository(private val dao: TrackDao) {
    suspend fun insertTrack(track: LogTable) = dao.inserTrack(track)
}