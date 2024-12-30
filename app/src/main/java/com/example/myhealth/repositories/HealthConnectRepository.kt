package com.example.myhealth.repositories

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

@Singleton
class HealthConnectRepository  @Inject constructor(
    private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    private var permissionLauncher: ActivityResultLauncher<Set<String>>? = null

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
        Log.d("HealthConnectRepository", availability.value.toString())
    }

    private fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class)
    )

    suspend fun checkPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun initializePermissionLauncher(activity: ComponentActivity) {
        permissionLauncher = activity.registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            // Will handle the result where I make the request
        }
    }

    fun requestPermissions(onComplete: (Boolean) -> Unit) {
        Log.d("HealthConnectRepository", "Requesting permissions")
        permissionLauncher?.launch(permissions) ?: onComplete(false)
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): List<StepsRecord> {
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return healthConnectClient.readRecords(request).records
    }

    suspend fun readDailyCalories(startTime: Instant, endTime: Instant): List<ActiveCaloriesBurnedRecord> {
        val request = ReadRecordsRequest(
            recordType = ActiveCaloriesBurnedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return healthConnectClient.readRecords(request).records
    }

    suspend fun writeTestData() {
        val now = Instant.now()
        val startTime = now.minus(1, ChronoUnit.HOURS)

        // Write test steps
        val stepsRecord = androidx.health.connect.client.records.StepsRecord(
            count = 1000,
            startTime = startTime,
            endTime = now,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC
        )

        // Write test calories
        val caloriesRecord = androidx.health.connect.client.records.ActiveCaloriesBurnedRecord(
            energy = Energy.calories(100.0),
            startTime = startTime,
            endTime = now,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC
        )

        healthConnectClient.insertRecords(listOf(stepsRecord, caloriesRecord))
    }
}

enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}