package com.example.myhealth.viewModels

import androidx.lifecycle.ViewModel
import com.example.myhealth.repositories.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val healthRepository: HealthRepository
) : ViewModel() {
    private val _waterGlasses = MutableStateFlow<List<WaterGlass>>(emptyList())
    val waterGlasses: StateFlow<List<WaterGlass>> = _waterGlasses.asStateFlow()

    private val _userGoals = MutableStateFlow(UserGoals(0, 0.0))
    val userGoals: StateFlow<UserGoals> = _userGoals.asStateFlow()

    fun addWaterGlass() {
        val now = System.currentTimeMillis()
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        healthRepository.saveWaterGlass(date, now) {
            loadWaterGlasses()
        }
    }

    fun deleteWaterGlass(glassId: String) {
        healthRepository.deleteWaterGlass(glassId) {
            loadWaterGlasses()
        }
    }

    fun getWaterGlasses() {
        loadUserGoals {
            loadWaterGlasses()
        }
    }

    private fun loadWaterGlasses() {
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        healthRepository.getWaterGlasses(date) { glasses ->
            _waterGlasses.value = glasses
        }
    }

    private fun loadUserGoals(onComplete: () -> Unit = {}) {
        healthRepository.getUserGoals { goals ->
            _userGoals.value = goals
            onComplete()
        }
    }
}

data class WaterGlass(
    val id: String,
    val timestamp: Long,
    val date: String
)