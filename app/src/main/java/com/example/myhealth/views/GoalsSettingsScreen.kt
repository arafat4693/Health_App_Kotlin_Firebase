package com.example.myhealth.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealth.viewModels.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var stepsGoal by remember { mutableStateOf("") }
    var caloriesGoal by remember { mutableStateOf("") }

    val currentGoals by viewModel.userGoals.collectAsState()

    LaunchedEffect(currentGoals) {
        stepsGoal = currentGoals.stepsGoal.toString()
        caloriesGoal = currentGoals.caloriesGoal.toString()
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Goals Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )

        OutlinedTextField(
            value = stepsGoal,
            onValueChange = { stepsGoal = it },
            label = { Text("Daily Steps Goal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = caloriesGoal,
            onValueChange = { caloriesGoal = it },
            label = { Text("Daily Calories Goal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                viewModel.saveUserGoals(
                    stepsGoal.toLongOrNull() ?: 0,
                    caloriesGoal.toDoubleOrNull() ?: 0.0
                )
                onNavigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Save Goals")
        }
    }
}