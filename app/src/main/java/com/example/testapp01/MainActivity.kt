package com.example.testapp01

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapp01.ui.theme.Testapp01Theme
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.ContentResolver
import android.content.ContentValues
import androidx.navigation.compose.rememberNavController
import android.net.Uri
import android.provider.MediaStore

class MainActivity : ComponentActivity() {
    private var isRecording by mutableStateOf(false)
    private lateinit var mediaRecorder: MediaRecorder
    private var currentRecordingUri: Uri? = null
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize permission launcher
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            val writeStorageGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if (recordAudioGranted && writeStorageGranted) {
                // Permissions granted
            } else {
                Toast.makeText(this, "Permissions are required to record audio", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            // Initialize navController

            val recordingViewModel: RecordingViewModel = viewModel()

            Testapp01Theme {
                val navController = rememberNavController()
                // Pass navController and recording functions to AppNavigation
                AppNavigation(
                    navController = navController,  // Pass navController here
                    onStartRecording = { startRecording(recordingViewModel) },
                    onStopRecording = { stopRecording(recordingViewModel) },
                    recordingViewModel = recordingViewModel
                )
            }
        }
    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun startRecording(recordingViewModel: RecordingViewModel) {
        if (isRecording) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }

        val contentResolver: ContentResolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "recording_${System.currentTimeMillis()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Music/Recordings")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            currentRecordingUri = it
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(contentResolver.openFileDescriptor(it, "w")?.fileDescriptor)
                prepare()
                start()
            }
            isRecording = true
        } ?: run {
            Toast.makeText(this, "Error accessing media store", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording(recordingViewModel: RecordingViewModel) {
        if (!isRecording) return

        try {
            mediaRecorder.apply {
                stop()
                release()
            }
            isRecording = false

            currentRecordingUri?.let { uri ->
                val filePath = uri.toString()
                val recordingTitle = "Recording ${System.currentTimeMillis()}"
                recordingViewModel.addRecording(Recording(filePath = filePath, title = recordingTitle))
            }

            Toast.makeText(this, "Recording stopped and saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error stopping recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}