package com.example.myhealth.viewModels

import android.util.Log
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
import java.time.LocalDateTime
import java.time.ZoneId
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

    private val _userGoals = MutableStateFlow(UserGoals(0, 0.0))
    val userGoals: StateFlow<UserGoals> = _userGoals.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserGoals()
    }

    private fun loadUserGoals(onComplete: () -> Unit = {}) {
        healthRepository.getUserGoals { goals ->
            _userGoals.value = goals
            onComplete()
        }
    }

    fun saveUserGoals(stepsGoal: Long, caloriesGoal: Double) {
        healthRepository.saveUserGoals(stepsGoal, caloriesGoal) {
            _userGoals.value = UserGoals(stepsGoal, caloriesGoal)
            // Refresh health data to update streak calculations
            // fetchAndSaveHealthData()
        }
    }

    fun checkAndRequestPermissions() {
        Log.d("HealthViewModel", "checkAndRequestPermissions called")
        viewModelScope.launch {
            _permissionsGranted.value = healthConnectManager.checkPermissions()
            if (!_permissionsGranted.value) {
                healthConnectManager.requestPermissions { granted ->
                    _permissionsGranted.value = granted
                    if (granted) {
                        loadUserGoals {
                            loadHealthHistory {
                                fetchAndSaveHealthData()
                            }
                        }
                    }
                }
            } else {
                loadUserGoals {
                    loadHealthHistory {
                        fetchAndSaveHealthData()
                    }
                }
            }
        }
    }

    private fun isWithinMidnightDeadline(timestamp: Long): Boolean {
        // Get the completion time
        val completionDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )

        // Get midnight of the next day for the completion date
        val deadlineDateTime = completionDateTime.toLocalDate()
            .plusDays(1)                    // Move to next day
            .atStartOfDay()                 // Get midnight (00:00:00)

        // Check if completion time was before the midnight deadline
        return completionDateTime.isBefore(deadlineDateTime)
    }

    private fun calculateStreak(
        currentSteps: Long,
        currentCalories: Double,
        previousData: HealthData?,
        todayData: HealthData?
    ): Pair<Int, Long?> {
        // if already completed today goal
        Log.d("HealthViewModel", _healthData.value.toString())
        Log.d("HealthViewModel", _userGoals.value.toString())

        if(todayData?.goalsCompletedTimestamp != null) return Pair(todayData.streak, todayData.goalsCompletedTimestamp)
        Log.d("HealthViewModel", "Inside")

        val goals = _userGoals.value
        val goalsCompleted = currentSteps >= goals.stepsGoal && currentCalories >= goals.caloriesGoal

        if (!goalsCompleted) {
            return Pair(0, null)
        }

        // Get current time
        val now = System.currentTimeMillis()

        // Check if the previous day's goals were completed before midnight
        val previousStreak = previousData?.let { prevData ->
            if (prevData.goalsCompletedTimestamp != null &&
                isWithinMidnightDeadline(prevData.goalsCompletedTimestamp)) {
                prevData.streak
            } else {
                0
            }
        } ?: 0

        return Pair(previousStreak + 1, now)
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
                val today = LocalDate.now().format(dateFormatter)
                val previousDay = LocalDate.now().minusDays(1).format(dateFormatter)

                // Get today's and previous day's data
                val todayData = _healthData.value.find { it.date == today }
                val previousData = _healthData.value.find { it.date == previousDay }

                // Calculate new streak and completion timestamp
                val (newStreak, completionTimestamp) = calculateStreak(
                    totalSteps,
                    totalCalories,
                    previousData,
                    todayData
                )

                healthRepository.saveHealthData(
                    date = today,
                    steps = totalSteps,
                    calories = totalCalories,
                    stepsGoal = _userGoals.value.stepsGoal,
                    caloriesGoal = _userGoals.value.caloriesGoal,
                    streak = newStreak,
                    goalsCompletedTimestamp = completionTimestamp
                )
                loadHealthHistory()
            }catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadHealthHistory(onComplete: () -> Unit = {}) {
        healthRepository.getHealthData { healthDataList ->
            _healthData.value = healthDataList
            onComplete()
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
    val calories: Double,
    val stepsGoal: Long = 0,
    val caloriesGoal: Double = 0.0,
    val streak: Int = 0,
    val goalsCompletedTimestamp: Long? = null  // Timestamp when goals were completed
)

data class UserGoals(
    val stepsGoal: Long,
    val caloriesGoal: Double
)