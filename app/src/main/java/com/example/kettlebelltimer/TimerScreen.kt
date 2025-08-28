package com.example.kettlebelltimer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kettlebelltimer.ui.theme.KettlebellTimerTheme
import com.example.kettlebelltimer.ui.theme.SportyGreen
import com.example.kettlebelltimer.ui.theme.OnSportyGreen
import com.example.kettlebelltimer.ui.theme.PauseYellow
import com.example.kettlebelltimer.ui.theme.OnPauseYellow
// It's good practice to ensure this import is present if R is not automatically resolved.
// import com.example.kettlebelltimer.R

@Composable
fun TimerScreen(viewModel: TimerViewModel, onStopClicked: () -> Unit) {
    val timeDisplay by viewModel.timeDisplay.collectAsState()
    val currentRoundDisplay by viewModel.currentRoundDisplay.collectAsState()
    val currentExerciseDisplay by viewModel.currentExerciseDisplay.collectAsState()
    val totalTimeDisplay by viewModel.totalTimeDisplay.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(), // Handles insets for edge-to-edge
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes top and bottom content
    ) {
        // Top Section: Small timer info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp), // Specific padding
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentRoundDisplay, style = MaterialTheme.typography.titleMedium)
            Text(currentExerciseDisplay, style = MaterialTheme.typography.titleMedium)
            Text(totalTimeDisplay, style = MaterialTheme.typography.titleMedium)
        }

        // Main Section: Large countdown timer
        Box(
            modifier = Modifier
                .weight(1f) // Takes up available space
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeDisplay,
                fontSize = 120.sp, // Very large text
                style = MaterialTheme.typography.displayLarge,
                color = SportyGreen
            )
        }

        // Bottom Section: Play/Pause and Stop buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp), // Specific padding
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.togglePlayPause() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) PauseYellow else SportyGreen
                )
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) OnPauseYellow else OnSportyGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
            Button(
                onClick = onStopClicked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stop_24px),
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTimerScreen() {
    KettlebellTimerTheme {
        // Preview with mock data since we can't easily create a real ViewModel here
        Text("Preview not implemented")
    }
}
