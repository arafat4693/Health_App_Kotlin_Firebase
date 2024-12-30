package com.example.myhealth.viewModels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealth.repositories.HealthConnectRepository
import com.example.myhealth.repositories.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {
    private val _healthData = MutableStateFlow<List<HealthData>>(emptyList())
    val healthData: StateFlow<List<HealthData>> = _healthData.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun checkAndRequestPermissions() {
        viewModelScope.launch {
            _permissionsGranted.value = healthConnectManager.checkPermissions()
            if (!_permissionsGranted.value) {
                healthConnectManager.requestPermissions { granted ->
                    _permissionsGranted.value = granted
                    if (granted) {
                        fetchAndSaveHealthData()
                    }
                }
            } else {
                fetchAndSaveHealthData()
            }
        }
    }

    private fun fetchAndSaveHealthData() {
        viewModelScope.launch {
            _isLoading.value = true

            try{
                val endTime = Instant.now()
                val startTime = endTime.minus(1, ChronoUnit.DAYS)

                val steps = healthConnectManager.readDailySteps(startTime, endTime)
                val calories = healthConnectManager.readDailyCalories(startTime, endTime)

                val totalSteps = steps.sumOf { it.count }
                val totalCalories = calories.sumOf { it.energy.inCalories }

                val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                val date = LocalDate.now().format(dateFormatter)

                healthRepository.saveHealthData(date, totalSteps, totalCalories)
                loadHealthHistory()
            }catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadHealthHistory() {
        healthRepository.getHealthData { healthDataList ->
            _healthData.value = healthDataList
        }
    }

    fun writeTestData() {
        viewModelScope.launch {
            try {
                if (_permissionsGranted.value) {
                    healthConnectManager.writeTestData()
                    fetchAndSaveHealthData()
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}

data class HealthData(
    val date: String,
    val steps: Long,
    val calories: Double
)