package com.bpareja.pomodorotec.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bpareja.pomodorotec.utils.PreferencesManager

@Composable
fun SettingsScreen(onBackClick: () -> Unit, onSaveClick: (Int, Int) -> Unit) {
    val preferencesManager = PreferencesManager(LocalContext.current)
    var sessionDuration by remember { mutableStateOf(preferencesManager.getSessionDuration().toString()) }
    var breakDuration by remember { mutableStateOf(preferencesManager.getBreakDuration().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Configurar duración de la sesión
        Text("Duración de la sesión (minutos):")
        TextField(
            value = sessionDuration,
            onValueChange = { sessionDuration = it.filter { char -> char.isDigit() } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configurar duración del descanso
        Text("Duración del descanso (minutos):")
        TextField(
            value = breakDuration,
            onValueChange = { breakDuration = it.filter { char -> char.isDigit() } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones para guardar o volver
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBackClick) {
                Text("Volver")
            }
            Button(onClick = {
                val sessionMinutes = sessionDuration.toIntOrNull() ?: 25
                val breakMinutes = breakDuration.toIntOrNull() ?: 5
                onSaveClick(sessionMinutes, breakMinutes)
                onBackClick()
            }) {
                Text("Guardar")
            }
        }
    }
}