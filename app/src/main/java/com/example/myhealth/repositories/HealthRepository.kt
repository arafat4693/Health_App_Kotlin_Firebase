package com.example.myhealth.repositories

import com.example.myhealth.viewModels.HealthData
import com.example.myhealth.viewModels.UserGoals
import com.example.myhealth.viewModels.WaterGlass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class HealthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
){
    fun saveHealthData(
        date: String,
        steps: Long,
        calories: Double,
        stepsGoal: Long,
        caloriesGoal: Double,
        streak: Int,
        goalsCompletedTimestamp: Long?
    ) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val healthData = hashMapOf(
            "date" to date,
            "steps" to steps,
            "calories" to calories,
            "stepsGoal" to stepsGoal,
            "caloriesGoal" to caloriesGoal,
            "streak" to streak,
            "goalsCompletedTimestamp" to goalsCompletedTimestamp,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .collection("health_data")
            .document(date)
            .set(healthData)
    }

    fun getHealthData(onSuccess: (List<HealthData>) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("health_data")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(7)
            .get()
            .addOnSuccessListener { documents ->
                val healthDataList = documents.mapNotNull { doc ->
                    HealthData(
                        date = doc.getString("date") ?: "",
                        steps = doc.getLong("steps") ?: 0,
                        calories = doc.getDouble("calories") ?: 0.0,
                        stepsGoal = doc.getLong("stepsGoal") ?: 0,
                        caloriesGoal = doc.getDouble("caloriesGoal") ?: 0.0,
                        streak = doc.getLong("streak")?.toInt() ?: 0,
                        goalsCompletedTimestamp = doc.getLong("goalsCompletedTimestamp")
                    )
                }
                onSuccess(healthDataList)
            }
    }

    fun saveUserGoals(stepsGoal: Long, caloriesGoal: Double, waterGlassesGoal: Int, onSuccess: () -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val goals = hashMapOf(
            "stepsGoal" to stepsGoal,
            "caloriesGoal" to caloriesGoal,
            "waterGlassesGoal" to waterGlassesGoal
        )

        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("goals")
            .set(goals)
            .addOnSuccessListener { onSuccess() }
    }

    fun getUserGoals(onSuccess: (UserGoals) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("goals")
            .get()
            .addOnSuccessListener { document ->
                val goals = UserGoals(
                    stepsGoal = document.getLong("stepsGoal") ?: 0,
                    caloriesGoal = document.getDouble("caloriesGoal") ?: 0.0,
                    waterGlassesGoal = document.getLong("waterGlassesGoal")?.toInt() ?: 8
                )
                onSuccess(goals)
            }
    }

    fun saveWaterGlass(date: String, timestamp: Long, onSuccess: () -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val waterGlass = hashMapOf(
            "date" to date,
            "timestamp" to timestamp
        )

        firestore.collection("users")
            .document(userId)
            .collection("water_glasses")
            .add(waterGlass)
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteWaterGlass(glassId: String, onSuccess: () -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("water_glasses")
            .document(glassId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }

    fun getWaterGlasses(date: String, onSuccess: (List<WaterGlass>) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("water_glasses")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                val waterGlasses = documents.mapNotNull { doc ->
                    doc.getLong("timestamp")?.let { timestamp ->
                        WaterGlass(
                            id = doc.id,
                            timestamp = timestamp,
                            date = date
                        )
                    }
                }
                onSuccess(waterGlasses)
            }
    }

    suspend fun getWaterGlassesSuspend(date: String): List<WaterGlass> =
        suspendCancellableCoroutine { continuation ->
            getWaterGlasses(date) { glasses ->
                continuation.resume(glasses)
            }
        }
}