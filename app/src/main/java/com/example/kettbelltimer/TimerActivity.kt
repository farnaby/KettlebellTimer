package com.example.kettbelltimer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kettbelltimer.ui.theme.KettbellTimerTheme

class TimerActivity : ComponentActivity() {

    private val totalRounds: Int by lazy {
        intent.getIntExtra(EXTRA_TOTAL_ROUNDS, 0)
    }
    
    private lateinit var viewModel: TimerViewModel
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        audioManager = KBTimerApp.getAudioManager(this)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TimerViewModel(totalRounds, this@TimerActivity) as T
            }
        })[TimerViewModel::class.java]
        
        setContent {
            KettbellTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(
                        viewModel = viewModel,
                        onStopClicked = {
                            audioManager.playButtonTapSound()
                            finish() // Go back to the previous activity (MainActivity)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
