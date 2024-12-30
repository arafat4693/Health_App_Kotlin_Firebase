package com.example.myhealth.repositories

import com.example.myhealth.viewModels.HealthData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
){
    fun saveHealthData(date: String, steps: Long, calories: Double) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val healthData = hashMapOf(
            "date" to date,
            "steps" to steps,
            "calories" to calories,
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
                        calories = doc.getDouble("calories") ?: 0.0
                    )
                }
                onSuccess(healthDataList)
            }
    }
}