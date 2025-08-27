// AudioManager.kt
package com.example.kettbelltimer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.lang.ref.WeakReference

class AudioManager(context: Context) {
    private val contextRef = WeakReference(context.applicationContext)
    private var soundPool: SoundPool? = null

    // Sound IDs
    private var exerciseStartSoundId = -1
    private var restStartSoundId = -1
    private var workoutCompleteSoundId = -1
    private var countdownBeepSoundId = -1
    private var buttonTapSoundId = -1

    init {
        initializeSoundPool()
        loadSounds()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun loadSounds() {
        contextRef.get()?.let { context ->
            try {
                exerciseStartSoundId = soundPool?.load(context, R.raw.exercise_start, 1) ?: -1
                restStartSoundId = soundPool?.load(context, R.raw.rest_start, 1) ?: -1
                workoutCompleteSoundId = soundPool?.load(context, R.raw.workout_complete, 1) ?: -1
                countdownBeepSoundId = soundPool?.load(context, R.raw.countdown_beep, 1) ?: -1
                buttonTapSoundId = soundPool?.load(context, R.raw.button_tap, 1) ?: -1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playExerciseStartSound() = playSound(exerciseStartSoundId)
    fun playRestStartSound() = playSound(restStartSoundId)
    fun playWorkoutCompleteSound() = playSound(workoutCompleteSoundId)
    fun playCountdownBeep() = playSound(countdownBeepSoundId)
    fun playButtonTapSound() = playSound(buttonTapSoundId)

    private fun playSound(soundId: Int) {
        if (soundId != -1) {
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playCountdownSound(seconds: Int) {
        if (seconds in 1..3) {
            playCountdownBeep()
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        // Log.d("AudioManager", "SoundPool released")
    }
}