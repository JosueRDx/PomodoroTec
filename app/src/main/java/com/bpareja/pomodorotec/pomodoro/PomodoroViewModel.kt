package com.bpareja.pomodorotec.pomodoro

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.CountDownTimer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bpareja.pomodorotec.MainActivity
import com.bpareja.pomodorotec.PomodoroReceiver
import com.bpareja.pomodorotec.R

enum class Phase {
    FOCUS, BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    init {
        instance = this
    }

    companion object {
        var instance: PomodoroViewModel? = null // Cambiado a público
            private set // Evitar que otros puedan modificarla directamente

        fun skipBreak() {
            instance?.startFocusSession() // Saltar el descanso
        }
    }

    private val context = getApplication<Application>().applicationContext

    private val _timeLeft = MutableLiveData("25:00")
    val timeLeft: LiveData<String> = _timeLeft

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _currentPhase = MutableLiveData(Phase.FOCUS)
    val currentPhase: LiveData<Phase> = _currentPhase

    private val _isSkipBreakButtonVisible = MutableLiveData(false)
    val isSkipBreakButtonVisible: LiveData<Boolean> = _isSkipBreakButtonVisible

    private var countDownTimer: CountDownTimer? = null
    private var timeRemainingInMillis: Long = 25 * 60 * 1000L // Tiempo inicial para FOCUS

    // Función para iniciar la sesión de concentración
    fun startFocusSession() {
        countDownTimer?.cancel() // Cancela cualquier temporizador en ejecución
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L // Restablece el tiempo de enfoque a 25 minutos
        _timeLeft.value = "25:00"
        _isSkipBreakButtonVisible.value = false // Ocultar el botón si estaba visible
        showNotification("Inicio de Concentración", "La sesión de concentración ha comenzado.")
        startTimer() // Inicia el temporizador con el tiempo de enfoque actualizado
    }

    // Función para iniciar la sesión de descanso
    private fun startBreakSession() {
        _currentPhase.value = Phase.BREAK
        timeRemainingInMillis = 5 * 60 * 1000L // 5 minutos para descanso
        _timeLeft.value = "05:00"
        _isSkipBreakButtonVisible.value = true // Mostrar el botón durante el descanso
        showNotification("Inicio de Descanso", "La sesión de descanso ha comenzado.")
        startTimer()
    }

    // Inicia o reanuda el temporizador
    fun startTimer() {
        countDownTimer?.cancel() // Cancela cualquier temporizador en ejecución antes de iniciar uno nuevo
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(timeRemainingInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _isRunning.value = false
                when (_currentPhase.value ?: Phase.FOCUS) { // Si es null, se asume FOCUS
                    Phase.FOCUS -> startBreakSession()
                    Phase.BREAK -> startFocusSession()
                }
            }
        }.start()
    }

    // Pausa el temporizador
    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    // Restablece el temporizador
    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _currentPhase.value = Phase.FOCUS
        timeRemainingInMillis = 25 * 60 * 1000L // Restablece a 25 minutos
        _timeLeft.value = "25:00"
        _isSkipBreakButtonVisible.value = false // Ocultar el botón al restablecer
    }

    // Muestra la notificación personalizada
    private fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // Reabre la actividad si ya está en el stack
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Determinar el ícono dinámico según la fase
        val smallIcon = if (_currentPhase.value == Phase.FOCUS) R.drawable.focus else R.drawable.resource_break

        // Crear las acciones interactivas
        val skipBreakIntent = Intent(context, PomodoroReceiver::class.java).apply {
            action = "SKIP_BREAK"
        }
        val skipBreakPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, 0, skipBreakIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(context, PomodoroReceiver::class.java).apply {
            action = "PAUSE_TIMER"
        }
        val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(smallIcon) // Ícono dinámico
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Acceso rápido a la actividad principal
            .setAutoCancel(true)
            .addAction(R.drawable.ic_skip, "Saltar descanso", skipBreakPendingIntent)
            .addAction(R.drawable.ic_skip, "Pausar", pausePendingIntent) // Reutilizando el ícono de "skip" para "pausa"

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }
}
