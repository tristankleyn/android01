package com.example.testapp01

import android.media.MediaPlayer
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.clickable
import android.net.Uri
import java.io.IOException
import android.util.Log
import java.io.File



@Composable
fun HomeScreen(
    navController: NavHostController,
    recordingViewModel: RecordingViewModel,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val recordings = recordingViewModel.recordings.collectAsState()

    Column {
        LazyColumn {
            items(recordings.value) { recording ->
                val recordingPath = recording.filePath

                Button(onClick = {
                    // Navigate to the playback screen with the recording path
                    navController.navigate("playback/$recordingPath")
                }) {
                    Text(text = "Play ${recording.title}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons to start and stop recording
        Button(onClick = { onStartRecording() }) {
            Text(text = "Start Recording")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onStopRecording() }) {
            Text(text = "Stop Recording")
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    recordingViewModel: RecordingViewModel
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                navController = navController,
                recordingViewModel = recordingViewModel,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
        }
        composable("recordings") {
            SecondPage(
                recordingViewModel = recordingViewModel,
                navController = navController
            )
        }
        composable(
            route = "playback/{recordingPath}",
            arguments = listOf(navArgument("recordingPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordingPath = backStackEntry.arguments?.getString("recordingPath")
            PlaybackScreen(recordingPath = recordingPath)
        }
    }
}

@Composable
fun PlaybackScreen(recordingPath: String?) {
    val mediaPlayer = remember { MediaPlayer() }

    if (recordingPath != null && File(recordingPath).exists()) {
        try {
            mediaPlayer.setDataSource(recordingPath)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } else {
        Text("Invalid recording path or file not found", color = Color.Red)
    }

    Button(onClick = {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }) {
        Text("Play Recording")
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
}

@Composable
fun SecondPage(
    recordingViewModel: RecordingViewModel, // Keep this parameter
    navController: NavHostController // Keep this parameter
) {
    val recordings by recordingViewModel.recordings.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Saved Recordings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(recordings) { recording ->
                    RecordingItem(recording = recording, navController = navController)
                }
            }
        }
    }
}

@Composable
fun RecordingItem(
    recording: Recording,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Navigate to playback screen
                val encodedFilePath = Uri.encode(recording.filePath)
                navController.navigate("playback/$encodedFilePath")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.padding(end = 8.dp))
        Text(text = recording.title)
    }
}