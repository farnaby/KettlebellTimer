package com.example.kettbelltimer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.kettbelltimer.ui.theme.KettbellTimerTheme

class TimerActivity : ComponentActivity() {

    private val totalRounds: Int by lazy {
        intent.getIntExtra(EXTRA_TOTAL_ROUNDS, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KettbellTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(
                        totalRounds = totalRounds,
                        onStopClicked = {
                            finish() // Go back to the previous activity (MainActivity)
                        }
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TOTAL_ROUNDS = "com.example.kettbelltimer.EXTRA_TOTAL_ROUNDS"

        fun newIntent(context: Context, totalRounds: Int): Intent {
            return Intent(context, TimerActivity::class.java).apply {
                putExtra(EXTRA_TOTAL_ROUNDS, totalRounds)
            }
        }
    }
}
