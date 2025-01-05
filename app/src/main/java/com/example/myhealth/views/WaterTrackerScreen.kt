package com.example.myhealth.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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

    val waterIntakeList by remember { viewModel.waterIntakeList }
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
            val lastIntake = waterIntakeList.lastOrNull()
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

        // Display list of glasses
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(waterIntakeList) { waterIntake ->
                GlassShape()
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun GlassShape() {
    Canvas(modifier = Modifier.size(50.dp, 100.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 20.dp.toPx())
            lineTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, 20.dp.toPx())
            lineTo(size.width - 10.dp.toPx(), 0f)
            lineTo(10.dp.toPx(), 0f)
            close()
        }

        // Draw the glass outline
        drawPath(
            path = path,
            color = Color.Gray,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
        )

        // Draw the bottle neck
        drawRoundRect(
            color = Color.Gray,
            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
            topLeft = androidx.compose.ui.geometry.Offset(size.width / 2 - 10.dp.toPx(), 0f)
        )

        // Draw the water level
        drawRoundRect(
            color = Color.Blue,
            size = androidx.compose.ui.geometry.Size(size.width, size.height / 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height / 2)
        )
    }
}