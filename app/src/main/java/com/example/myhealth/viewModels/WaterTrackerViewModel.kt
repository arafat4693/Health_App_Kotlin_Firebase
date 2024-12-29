package com.example.myhealth.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WaterTrackerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    var waterIntakeList = mutableStateOf<List<WaterIntake>>(emptyList())
        private set

    private var isRequestInProgress = mutableStateOf(false)

    val totalVolume: Int
        get() = waterIntakeList.value.sumOf { it.volume }

    val totalGlasses: Int
        get() = waterIntakeList.value.sumOf { it.glasses }

    fun addWaterIntake(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (isRequestInProgress.value) return

        val userId = auth.currentUser?.uid ?: return
        val date = getCurrentDate()
        val volume = 250 // Volume per glass in ml
        val waterIntake = WaterIntake(userId, date, 1, volume)

        isRequestInProgress.value = true
        viewModelScope.launch {
            firestore.collection("waterIntake")
                .add(waterIntake)
                .addOnSuccessListener {
                    isRequestInProgress.value = false
                    onSuccess()
                }
                .addOnFailureListener {
                    isRequestInProgress.value = false
                    onError(it.message ?: "Error adding water intake")
                }
        }
    }

    fun deleteWaterIntake(waterIntakeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (isRequestInProgress.value) return

        isRequestInProgress.value = true
        viewModelScope.launch {
            firestore.collection("waterIntake")
                .document(waterIntakeId)
                .delete()
                .addOnSuccessListener {
                    isRequestInProgress.value = false
                    onSuccess()
                }
                .addOnFailureListener {
                    isRequestInProgress.value = false
                    onError(it.message ?: "Error deleting water intake")
                }
        }
    }

    fun fetchWaterIntake(onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val date = getCurrentDate()

        viewModelScope.launch {
            firestore.collection("waterIntake")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { documents ->
                    val waterIntakeList = documents.map { it.toObject(WaterIntake::class.java).copy(id = it.id) }
                    this@WaterTrackerViewModel.waterIntakeList.value = waterIntakeList
                }
                .addOnFailureListener { onError(it.message ?: "Error fetching water intake") }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

data class WaterIntake(
    val userId: String = "",
    val date: String = "",
    val glasses: Int = 1,
    val volume: Int = 250, // Default volume per glass in ml
    val id: String = "" // Firestore document ID
)