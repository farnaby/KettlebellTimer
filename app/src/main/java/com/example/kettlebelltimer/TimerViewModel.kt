package com.example.kettlebelltimer

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds
import com.example.kettlebelltimer.AudioManager

class TimerViewModel(private val totalRounds: Int, context: Context) : ViewModel() {
    private lateinit var audioManager: AudioManager

    // --- Workout State ---
    private var currentRound = 1
    private var currentExercise = 1
    private var totalSecondsElapsed = -1
    private var currentPhaseTimeLeft = INITIAL_COUNTDOWN_SECONDS
    private var currentPhase = WorkoutPhase.INITIAL_COUNTDOWN

    // --- UI State Flows ---
    private val _timeDisplay = MutableStateFlow(INITIAL_COUNTDOWN_SECONDS.toString())
    val timeDisplay: StateFlow<String> = _timeDisplay

    private val _currentRoundDisplay = MutableStateFlow("Round: $currentRound / $totalRounds")
    val currentRoundDisplay: StateFlow<String> = _currentRoundDisplay

    private val _currentExerciseDisplay = MutableStateFlow("Get Ready!")
    val currentExerciseDisplay: StateFlow<String> = _currentExerciseDisplay

    private val _totalTimeDisplay = MutableStateFlow("Total: 00:00")
    val totalTimeDisplay: StateFlow<String> = _totalTimeDisplay

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // --- Timer constants ---
    companion object {
        const val INITIAL_COUNTDOWN_SECONDS = 3
        const val EXERCISE_DURATION_SECONDS = 30
        const val EXERCISES_PER_ROUND = 8
        const val REST_DURATION_SECONDS = 60
    }

    // --- CountDownTimer instances ---
    private var initialCountdownTimer: CountDownTimer? = null
    private var mainTimer: CountDownTimer? = null
    private var countdownTimer: CountDownTimer? = null

    init {
        audioManager = KBTimerApp.getAudioManager(context)
        startInitialCountdown()
    }

    private fun startInitialCountdown() {
        cancelTimers()
        currentPhase = WorkoutPhase.INITIAL_COUNTDOWN
        currentPhaseTimeLeft = INITIAL_COUNTDOWN_SECONDS
        _isPlaying.value = true
        _currentExerciseDisplay.value = "Get Ready!"
        
        initialCountdownTimer = object : CountDownTimer((INITIAL_COUNTDOWN_SECONDS * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                _timeDisplay.value = secondsLeft.toString()
                currentPhaseTimeLeft = secondsLeft
                audioManager.playCountdownBeep()
                updateTotalTimeDisplay()
            }

            override fun onFinish() {
                // Transition to first exercise
                currentPhase = WorkoutPhase.EXERCISE
                currentExercise = 1
                currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                _timeDisplay.value = formatTime(EXERCISE_DURATION_SECONDS)
                _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                _currentRoundDisplay.value = "Round: $currentRound / $totalRounds"
                // Start main timer immediately after initial countdown
                startMainTimer()
            }
        }.start()
    }

    fun togglePlayPause() {
        // Play button tap sound
        audioManager.playButtonTapSound()
        
        _isPlaying.value = !_isPlaying.value
        
        if (_isPlaying.value) {
            when (currentPhase) {
                WorkoutPhase.INITIAL_COUNTDOWN -> startInitialCountdown()
                else -> startMainTimer(currentPhaseTimeLeft)
            }
        } else {
            cancelTimers()
        }
    }

    private fun startMainTimer(resumeAtTime : Int? = null) {
        cancelTimers()
        _isPlaying.value = true

        // Calculate the time for the current phase
        val timeForCurrentPhase = resumeAtTime ?: when (currentPhase) {
            WorkoutPhase.EXERCISE -> EXERCISE_DURATION_SECONDS
            WorkoutPhase.REST -> REST_DURATION_SECONDS
            else -> 0
        }

        if(resumeAtTime == null) {
            when (currentPhase) {
                WorkoutPhase.EXERCISE -> audioManager.playExerciseStartSound()
                WorkoutPhase.REST -> audioManager.playRestStartSound()
                else -> { /* No sound for other phases or handle as needed */
                }
            }
        }
        
        mainTimer = object : CountDownTimer((timeForCurrentPhase * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                if(secondsLeft <= 3) {
                    audioManager.playCountdownBeep()
                }
                currentPhaseTimeLeft = secondsLeft
                _timeDisplay.value = formatTime(currentPhaseTimeLeft)
                totalSecondsElapsed++
                updateTotalTimeDisplay()
            }

            override fun onFinish() {
                finishCurrentExcerciseOrRest()
            }
        }.start()
    }

    private fun finishCurrentExcerciseOrRest() {
        when (currentPhase) {
            WorkoutPhase.EXERCISE -> {
                // Move to next exercise or rest
                if (currentExercise < EXERCISES_PER_ROUND) {
                    // Next exercise in same round
                    currentExercise++
                    currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                    _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                    startMainTimer() // Restart timer for next exercise
                } else if (currentRound < totalRounds) {
                    currentPhase = WorkoutPhase.REST
                    currentPhaseTimeLeft = REST_DURATION_SECONDS
                    _currentExerciseDisplay.value = "Rest"
                    startMainTimer()
                    // Play sound for rest start
                } else {
                    // Workout complete
                    _currentExerciseDisplay.value = "Workout Complete!"
                    _isPlaying.value = false
                    currentPhaseTimeLeft = 0
                    // Play sound for workout completion
                    audioManager.playWorkoutCompleteSound()
                }
                _timeDisplay.value = formatTime(currentPhaseTimeLeft)
            }

            WorkoutPhase.REST -> {
                // Start next round
                currentRound++
                currentExercise = 1
                currentPhase = WorkoutPhase.EXERCISE
                currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                _currentRoundDisplay.value = "Round: $currentRound / $totalRounds"
                _timeDisplay.value = formatTime(currentPhaseTimeLeft)
                startMainTimer() // Restart timer for next round
            }

            else -> {
                // This shouldn't happen
            }
        }
    }

    private fun cancelTimers() {
        initialCountdownTimer?.cancel()
        mainTimer?.cancel()
    }

    private fun updateTotalTimeDisplay() {
        if(totalSecondsElapsed < 0) {
            _totalTimeDisplay.value = "Total: --:--"
        } else {
            val minutes = totalSecondsElapsed / 60
            val seconds = totalSecondsElapsed % 60
            _totalTimeDisplay.value = String.format("Total: %02d:%02d", minutes, seconds)
        }
    }

    private fun formatTime(seconds: Int): String {
        return if (seconds < 0) "0" else seconds.toString()
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimers()
        countdownTimer?.cancel()
    }

    private enum class WorkoutPhase {
        INITIAL_COUNTDOWN,
        EXERCISE,
        REST
    }
}
