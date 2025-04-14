package com.example.app_a_void

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment

class AppLimitSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppLimitSettingsUI()
            }
        }
    }

    @Composable
    fun AppLimitSettingsUI() {
        // Sample data: list of apps
        val apps = listOf("App 1", "App 2", "App 3")
        val limits = remember { mutableStateMapOf<String, TextFieldValue>() }
        apps.forEach { app ->
            if (!limits.containsKey(app)) {
                limits[app] = TextFieldValue("0")
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Set App Usage Limits", style = MaterialTheme.typography.headlineMedium)

            apps.forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(app, style = MaterialTheme.typography.bodyLarge)

                    TextField(
                        value = limits[app]!!,
                        onValueChange = { newValue -> limits[app] = newValue },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Handle save logic here */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Settings")
            }
        }
    }
}
