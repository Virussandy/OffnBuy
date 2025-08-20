package com.ozonic.offnbuy.util

import android.app.Activity
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Define states for the UI to observe
sealed interface UpdateState {
    object Idle : UpdateState
    object UpdateNotAvailable : UpdateState
    data class UpdateReadyToInstall(val manager: AppUpdateManager) : UpdateState
}

class InAppUpdateManager(private val activity: Activity) : DefaultLifecycleObserver {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, notify the UI that it's ready to be installed.
            Log.d("InAppUpdate", "Update downloaded. Ready to install.")
            _updateState.value = UpdateState.UpdateReadyToInstall(appUpdateManager)
        }
    }

    // This is called when the Activity's onResume is triggered
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Check for updates that were accepted but not installed.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.UpdateReadyToInstall(appUpdateManager)
            }
        }
    }

    // This is called when the Activity's onCreate is triggered
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        appUpdateManager.registerListener(installListener)
        checkForUpdate()
    }

    // This is called when the Activity's onDestroy is triggered
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager.unregisterListener(installListener)
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isFlexibleUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (isUpdateAvailable && isFlexibleUpdateAllowed) {
                // An update is available and the flexible flow is allowed.
                Log.d("InAppUpdate", "Flexible update available.")
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    UPDATE_REQUEST_CODE
                )
            } else {
                Log.d("InAppUpdate", "No flexible update available.")
                _updateState.value = UpdateState.UpdateNotAvailable
            }
        }
    }

    companion object {
        const val UPDATE_REQUEST_CODE = 123
    }
}