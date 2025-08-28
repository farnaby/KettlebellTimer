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

    // --- Progress State Flows ---
    private val _currentPhaseProgress = MutableStateFlow(0f)
    val currentPhaseProgress: StateFlow<Float> = _currentPhaseProgress

    private val _totalWorkoutProgress = MutableStateFlow(0f)
    val totalWorkoutProgress: StateFlow<Float> = _totalWorkoutProgress

    private val _currentPhase = MutableStateFlow(WorkoutPhase.INITIAL_COUNTDOWN)
    val currentPhaseState: StateFlow<WorkoutPhase> = _currentPhase

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
        _currentPhase.value = WorkoutPhase.INITIAL_COUNTDOWN
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
                _currentPhase.value = WorkoutPhase.EXERCISE
                currentExercise = 1
                currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                _timeDisplay.value = formatTime(EXERCISE_DURATION_SECONDS)
                _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                _currentRoundDisplay.value = "Round: $currentRound / $totalRounds"
                updateProgressValues() // Update progress for new phase
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
            when (_currentPhase.value) {
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
        val timeForCurrentPhase = resumeAtTime ?: when (_currentPhase.value) {
            WorkoutPhase.EXERCISE -> EXERCISE_DURATION_SECONDS
            WorkoutPhase.REST -> REST_DURATION_SECONDS
            else -> 0
        }

        if(resumeAtTime == null) {
            when (_currentPhase.value) {
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
        when (_currentPhase.value) {
            WorkoutPhase.EXERCISE -> {
                // Move to next exercise or rest
                if (currentExercise < EXERCISES_PER_ROUND) {
                    // Next exercise in same round
                    currentExercise++
                    currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                    _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                    startMainTimer() // Restart timer for next exercise
                } else if (currentRound < totalRounds) {
                    _currentPhase.value = WorkoutPhase.REST
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
                updateProgressValues() // Update progress for phase transitions
            }

            WorkoutPhase.REST -> {
                // Start next round
                currentRound++
                currentExercise = 1
                _currentPhase.value = WorkoutPhase.EXERCISE
                currentPhaseTimeLeft = EXERCISE_DURATION_SECONDS
                _currentExerciseDisplay.value = "Exercise: $currentExercise / $EXERCISES_PER_ROUND"
                _currentRoundDisplay.value = "Round: $currentRound / $totalRounds"
                _timeDisplay.value = formatTime(currentPhaseTimeLeft)
                updateProgressValues() // Update progress for new phase
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
        updateProgressValues()
    }

    private fun updateProgressValues() {
        // Update current phase progress
        val phaseDuration = when (_currentPhase.value) {
            WorkoutPhase.INITIAL_COUNTDOWN -> INITIAL_COUNTDOWN_SECONDS.toFloat()
            WorkoutPhase.EXERCISE -> EXERCISE_DURATION_SECONDS.toFloat()
            WorkoutPhase.REST -> REST_DURATION_SECONDS.toFloat()
        }
        val phaseProgress = if (phaseDuration > 0) {
            1f - (currentPhaseTimeLeft.toFloat() / phaseDuration)
        } else {
            0f
        }
        _currentPhaseProgress.value = phaseProgress.coerceIn(0f, 1f)

        // Update total workout progress
        val totalWorkoutDuration = calculateTotalWorkoutDuration()
        val workoutProgress = if (totalWorkoutDuration > 0 && totalSecondsElapsed >= 0) {
            (totalSecondsElapsed.toFloat() / totalWorkoutDuration.toFloat())
        } else {
            0f
        }
        _totalWorkoutProgress.value = workoutProgress.coerceIn(0f, 1f)
    }

    private fun calculateTotalWorkoutDuration(): Int {
        val timePerRound = EXERCISES_PER_ROUND * EXERCISE_DURATION_SECONDS
        return totalRounds * timePerRound + (totalRounds - 1) * REST_DURATION_SECONDS
    }

    private fun formatTime(seconds: Int): String {
        return if (seconds < 0) "0" else seconds.toString()
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimers()
        countdownTimer?.cancel()
    }

    enum class WorkoutPhase {
        INITIAL_COUNTDOWN,
        EXERCISE,
        REST
    }
}
