package com.ozonic.offnbuy.util

import android.app.Activity
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
            _updateState.value = UpdateState.UpdateReadyToInstall(appUpdateManager)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.UpdateReadyToInstall(appUpdateManager)
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        appUpdateManager.registerListener(installListener)
        checkForUpdate()
    }

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
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    UPDATE_REQUEST_CODE
                )
            } else {
                _updateState.value = UpdateState.UpdateNotAvailable
            }
        }
    }

    companion object {
        const val UPDATE_REQUEST_CODE = 123
    }
}