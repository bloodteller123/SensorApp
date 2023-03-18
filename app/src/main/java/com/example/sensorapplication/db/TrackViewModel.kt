package com.example.sensorapplication.db

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class TrackViewModel(private val repository: Repository) : ViewModel(){
    fun insertTrack(track: LogTable){
        viewModelScope.launch{
            repository.insertTrack(track)
        }
    }

}

class TrackViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}