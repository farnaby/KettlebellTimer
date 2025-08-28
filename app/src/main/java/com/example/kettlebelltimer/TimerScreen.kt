package com.example.kettlebelltimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kettlebelltimer.ui.theme.KettlebellTimerTheme
import com.example.kettlebelltimer.ui.theme.SportyGreen
import com.example.kettlebelltimer.ui.theme.OnSportyGreen
import com.example.kettlebelltimer.ui.theme.PauseYellow
import com.example.kettlebelltimer.ui.theme.OnPauseYellow
import com.example.kettlebelltimer.ui.theme.LightSportyGreen
import com.example.kettlebelltimer.ui.theme.RestBlue
import com.example.kettlebelltimer.ui.theme.RestBlueDark
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
// It's good practice to ensure this import is present if R is not automatically resolved.
// import com.example.kettlebelltimer.R

@Composable
fun CircularProgressTimer(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val timeDisplay by viewModel.timeDisplay.collectAsState()
    val currentPhaseProgress by viewModel.currentPhaseProgress.collectAsState()
    val totalWorkoutProgress by viewModel.totalWorkoutProgress.collectAsState()
    val currentPhase by viewModel.currentPhaseState.collectAsState()

    // Smooth animations for progress values
    val animatedPhaseProgress by animateFloatAsState(
        targetValue = currentPhaseProgress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "phase_progress"
    )

    val animatedWorkoutProgress by animateFloatAsState(
        targetValue = totalWorkoutProgress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "workout_progress"
    )

    val textMeasurer = rememberTextMeasurer()

    // Determine colors based on current phase
    val isRestPhase = currentPhase == TimerViewModel.WorkoutPhase.REST
    val isInitialCountdown = currentPhase == TimerViewModel.WorkoutPhase.INITIAL_COUNTDOWN
    val primaryColor = if (isRestPhase) RestBlue else SportyGreen // Blue for rest, green for others
    val ringColor = if (isRestPhase) RestBlueDark else SportyGreen.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Keep it square for perfect circles
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            // Calculate radii for concentric design with no gap
            val maxRadius = minOf(canvasWidth, canvasHeight) / 2 * 0.85f
            val ringStrokeWidth = maxRadius * 0.07f // Ring (5% of radius)
            val innerRadius = maxRadius - 0.5f * ringStrokeWidth // Inner circle fills the space inside the ring

            // Only draw circles if not in initial countdown
            val shouldDrawCircles = !isInitialCountdown

            if (shouldDrawCircles) {
                // Draw inner circular sector (current phase progress) - fills the center
                if (animatedPhaseProgress > 0f) {
                    drawArc(
                        color = primaryColor.copy(alpha = 0.3f),
                        startAngle = -90f, // Start from top
                        sweepAngle = animatedPhaseProgress * 360f,
                        useCenter = true,
                        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                        size = Size(innerRadius * 2, innerRadius * 2)
                    )
                }

                // Draw outer annular ring (total workout progress)
                if (animatedWorkoutProgress > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f, // Start from top
                        sweepAngle = animatedWorkoutProgress * 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                        size = Size(maxRadius * 2, maxRadius * 2),
                        style = Stroke(width = ringStrokeWidth)
                    )
                }
            }

            // Measure and draw text with shadow
            val textStyle = TextStyle(
                fontSize = (innerRadius * 0.50f).sp,
                color = primaryColor
            )

            val textLayoutResult = textMeasurer.measure(
                text = timeDisplay,
                style = textStyle
            )

            val textOffset = Offset(
                center.x - textLayoutResult.size.width / 2,
                center.y - textLayoutResult.size.height / 2
            )

            // Draw text shadow
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textOffset + Offset(2f, 2f),
                color = Color.Black.copy(alpha = 0.3f)
            )

            // Draw main text
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = textOffset,
                color = primaryColor
            )
        }
    }
}

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

        // Main Section: Circular progress timer
        CircularProgressTimer(
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f) // Takes up available space
                .fillMaxWidth()
        )

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
