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
fun SettingsScreen(onBackClick: () -> Unit) {
    val preferencesManager = PreferencesManager(LocalContext.current)
    val sessionDuration = remember { mutableStateOf(preferencesManager.getSessionDuration()) }
    val breakDuration = remember { mutableStateOf(preferencesManager.getBreakDuration()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Configurar duración de la sesión
        Text("Duración de la sesión (minutos):")
        TextField(
            value = sessionDuration.value.toString(),
            onValueChange = { sessionDuration.value = it.toIntOrNull() ?: sessionDuration.value },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configurar duración del descanso
        Text("Duración del descanso (minutos):")
        TextField(
            value = breakDuration.value.toString(),
            onValueChange = { breakDuration.value = it.toIntOrNull() ?: breakDuration.value },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones para guardar o volver
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBackClick) {
                Text("Volver")
            }
            Button(onClick = {
                preferencesManager.setSessionDuration(sessionDuration.value)
                preferencesManager.setBreakDuration(breakDuration.value)
                onBackClick()
            }) {
                Text("Guardar")
            }
        }
    }
}