package com.example.testapp01

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecordingViewModel : ViewModel() {

    // The MutableStateFlow will hold the list of recordings.
    private val _recordings = MutableStateFlow<List<Recording>>(emptyList())
    val recordings: StateFlow<List<Recording>> = _recordings

    // Function to add a new recording to the list
    fun addRecording(recording: Recording) {
        _recordings.value = _recordings.value + recording
    }

    // Optionally, you could add functions to remove or update recordings, etc.
}