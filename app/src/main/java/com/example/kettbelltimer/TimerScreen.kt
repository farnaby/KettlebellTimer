package com.example.kettbelltimer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kettbelltimer.ui.theme.KettbellTimerTheme
import com.example.kettbelltimer.ui.theme.SportyGreen
import com.example.kettbelltimer.ui.theme.OnSportyGreen
// It's good practice to ensure this import is present if R is not automatically resolved.
// import com.example.kettbelltimer.R

@Composable
fun TimerScreen(totalRounds: Int, onStopClicked: () -> Unit) {
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
            Text("Round: 1 / $totalRounds", style = MaterialTheme.typography.titleMedium)
            Text("Exercise: 1 / 8", style = MaterialTheme.typography.titleMedium)
            Text("Total: 00:00", style = MaterialTheme.typography.titleMedium)
        }

        // Main Section: Large countdown timer
        Box(
            modifier = Modifier
                .weight(1f) // Takes up available space
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "30", // Placeholder for countdown
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
                onClick = { /* TODO: Implement Play/Pause logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = SportyGreen)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play_arrow_24px),
                    contentDescription = "Play/Pause",
                    tint = OnSportyGreen,
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
    KettbellTimerTheme {
        TimerScreen(totalRounds = 3, onStopClicked = {})
    }
}
