package com.example.myhealth.views

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealth.viewModels.AuthenticationViewModel
import com.example.myhealth.viewModels.HealthData
import com.example.myhealth.viewModels.HealthViewModel
import com.google.firebase.BuildConfig
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    authViewModel: AuthenticationViewModel,
    toGoalsSettings: () -> Unit,
    onLogOutSuccess: () -> Unit,
) {
    val healthData by viewModel.healthData.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Check permissions after launcher is initialized
    LaunchedEffect(Unit) {
        viewModel.checkAndRequestPermissions()
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("My Health Data") },
            navigationIcon = {
                IconButton(onClick = { authViewModel.logout(onLogOutSuccess) }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout")
                }
            }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (!permissionsGranted) {
            PermissionRequest(viewModel)
        } else {
            Column {
                healthData.firstOrNull()?.let { latestData ->
                    StreakDisplay(streak = latestData.streak, toGoalsSettings = toGoalsSettings)
                }

                // Development/testing
                //if (BuildConfig.DEBUG) {  // Only show in debug builds
                    Button(
                        onClick = { viewModel.writeTestData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Add Test Data")
                    }
                //}

                HealthDataDisplay(healthData)
            }
        }
    }
}

@Composable
fun PermissionRequest(viewModel: HealthViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Health Connect permissions are required",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.checkAndRequestPermissions()
            }
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
fun HealthDataDisplay(healthData: List<HealthData>) {
    LazyColumn {
        items(healthData) { data ->
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Date: ${data.date}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Steps",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${data.steps}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column {
                            Text(
                                text = "Calories",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${data.calories.roundToInt()} kcal",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakDisplay(streak: Int, toGoalsSettings: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Streak",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$streak days",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Complete goals before midnight to maintain streak!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button (onClick = { toGoalsSettings() }) {
                Text("Settings")
            }
        }
    }
}