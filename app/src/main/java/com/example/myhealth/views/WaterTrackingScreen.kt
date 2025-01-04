package com.example.myhealth.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealth.viewModels.WaterViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    waterViewModel: WaterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val waterGlasses by waterViewModel.waterGlasses.collectAsState()
    val userGoals by waterViewModel.userGoals.collectAsState()

    LaunchedEffect(Unit) {
        waterViewModel.getWaterGlasses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Water Tracking") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )

        // Progress Circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { waterGlasses.size.toFloat() / userGoals.waterGlassesGoal },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${waterGlasses.size}/${userGoals.waterGlassesGoal}",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "glasses",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${waterGlasses.size * 250}ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Add Glass Button
        Button(
            onClick = { waterViewModel.addWaterGlass() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add glass",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Glass (250ml)")
        }

        // Today's History
        Text(
            text = "Today's Glasses",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(waterGlasses.sortedByDescending { it.timestamp }) { glass ->
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(glass.timestamp),
                                ZoneId.systemDefault()
                            ).format(DateTimeFormatter.ofPattern("HH:mm"))
                        )
                        IconButton(
                            onClick = {
                                waterViewModel.deleteWaterGlass(glass.id)
                            }
                        ) {
                            Icon(Icons.Default.Delete, "Delete glass")
                        }
                    }
                }
            }
        }
    }
}