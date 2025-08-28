package com.example.kettlebelltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.kettlebelltimer.ui.theme.KettlebellTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val audioManager = KBTimerApp.getAudioManager(this)

        setContent {
            KettlebellTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerSetupScreen(onStartClicked = { rounds ->
                        val intent = TimerActivity.newIntent(this, rounds)
                        audioManager.playButtonTapSound()
                        startActivity(intent)
                    })
                }
            }
        }
    }
}
