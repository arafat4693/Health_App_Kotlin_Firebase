package com.example.myhealth.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myhealth.viewModels.WaterTrackerViewModel

@Composable
fun WaterTrackerScreen(viewModel: WaterTrackerViewModel) {
    var errorMessage by remember { mutableStateOf("") }

    // Fetch water intake data when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.fetchWaterIntake { error ->
            errorMessage = error
        }
    }

    val totalVolume by remember { derivedStateOf { viewModel.totalVolume } }
    val totalGlasses by remember { derivedStateOf { viewModel.totalGlasses } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Total Glasses: $totalGlasses")
        Text("Total Volume: ${totalVolume} ml")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            viewModel.addWaterIntake({
                viewModel.fetchWaterIntake { error ->
                    errorMessage = error
                }
            }, {
                errorMessage = it
            })
        }) {
            Text("Add a Glass")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val lastIntake = viewModel.waterIntakeList.value.lastOrNull()
            if (lastIntake != null) {
                viewModel.deleteWaterIntake(lastIntake.id, {
                    viewModel.fetchWaterIntake { error ->
                        errorMessage = error
                    }
                }, {
                    errorMessage = it
                })
            }
        }) {
            Text("Delete a Glass")
        }
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = androidx.compose.ui.graphics.Color.Red)
        }

        // Display total volume of water tracked
        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Water Tracked Today: $totalVolume ml")
    }
}