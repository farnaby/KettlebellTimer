package com.example.kettbelltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.kettbelltimer.ui.theme.KettbellTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val audioManager = KBTimerApp.getAudioManager(this)

        setContent {
            KettbellTimerTheme {
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
