package com.bpareja.pomodorotec

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel

class PomodoroReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "SKIP_BREAK" -> PomodoroViewModel.skipBreak() // Saltar el descanso
            "PAUSE_TIMER" -> PomodoroViewModel.instance?.pauseTimer() // Pausar el temporizador
        }
    }
}
