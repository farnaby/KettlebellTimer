package com.example.kettlebelltimer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Removed import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kettlebelltimer.ui.theme.* // Added import for theme colors

@Composable
fun TimerSetupScreen(audioManager: AudioManager?, onStartClicked: (rounds: Int) -> Unit) {
    var selectedRounds by remember { mutableIntStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kettlebell Timer", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer

        Text(
            text = "Each round consists of 8 exercises (30s each), followed by a 60s rest.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center, // Center align the text
            modifier = Modifier.padding(horizontal = 16.dp) // Add some horizontal padding
        )
        Spacer(modifier = Modifier.height(32.dp))


        Text("How many rounds would you like to do?", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..3).forEach { roundCount ->
                Button(
                    onClick = {
                        audioManager?.playButtonTapSound()
                        selectedRounds = roundCount
                              },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRounds == roundCount) SportyGreen else LightSportyGreen,
                        contentColor = if (selectedRounds == roundCount) OnSportyGreen else OnLightSportyGreen
                    )
                ) {
                    Text(text = roundCount.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FloatingActionButton(
            onClick = { onStartClicked(selectedRounds) },
            modifier = Modifier.size(96.dp),
            containerColor = SportyGreen, // Set FAB background color
            contentColor = OnSportyGreen    // Set FAB icon color
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Go!",
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTimerSetupScreen() {
    // Wrap preview in the theme to see colors correctly
    KettlebellTimerTheme {
        TimerSetupScreen(null, onStartClicked = {})
    }
}
