package com.zuvix.snapvault.ui.screens.preview

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuvix.snapvault.ads.AdManager
import com.zuvix.snapvault.data.model.StatusItem
import com.zuvix.snapvault.data.repository.StatusRepository
import com.zuvix.snapvault.service.notification.NotificationEngine
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreviewUiState(
    val status: StatusItem? = null,
    val allStatuses: List<StatusItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val preferencesManager: PreferencesManager,
    private val adManager: AdManager,
    private val notificationEngine: NotificationEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()
    
    val isPremium = preferencesManager.isPremium.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    val isRewardedAdReady = adManager.isRewardedReady
    val isAdLoading = adManager.isRewardedLoading
    
    private var statusId: String = savedStateHandle["statusId"] ?: ""
    private var activityRef: Activity? = null
    
    init {
        loadStatus()
    }
    
    fun setActivity(activity: Activity) {
        activityRef = activity
    }
    
    private fun loadStatus() {
        viewModelScope.launch {
            val statuses = repository.statuses.value
            val index = statuses.indexOfFirst { it.id == statusId }
            
            if (index >= 0 && statuses.isNotEmpty()) {
                _uiState.value = PreviewUiState(
                    status = statuses[index],
                    allStatuses = statuses,
                    currentIndex = index,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Status not found"
                )
            }
        }
    }
    
    fun setCurrentIndex(index: Int) {
        val statuses = _uiState.value.allStatuses
        if (index in statuses.indices) {
            _uiState.value = _uiState.value.copy(
                currentIndex = index,
                status = statuses[index],
                isSaved = false
            )
        }
    }
    
    fun saveCurrentStatus() {
        viewModelScope.launch {
            val status = _uiState.value.status ?: return@launch
            val premium = isPremium.first()
            
            if (premium) {
                performSave(status)
            } else {
                // Show ad first for non-premium users
                activityRef?.let { activity ->
                    adManager.showRewardedAd(
                        activity = activity,
                        onRewarded = {
                            viewModelScope.launch {
                                performSave(status)
                            }
                        },
                        onFailed = {
                            // Still allow save even if ad fails (for better UX)
                            viewModelScope.launch {
                                performSave(status)
                            }
                        }
                    )
                } ?: run {
                    // No activity reference, just save
                    performSave(status)
                }
            }
        }
    }
    
    private suspend fun performSave(status: StatusItem) {
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        val result = repository.saveStatus(status)
        
        _uiState.value = _uiState.value.copy(
            isSaving = false,
            isSaved = result != null
        )
        
        // Show notification
        if (result != null) {
            notificationEngine.showDownloadCompleteNotification(status.fileName)
        }
    }
    
    fun shareStatus() {
        // Handled in UI
    }
    
    fun getCurrentStatusUri() = _uiState.value.status?.uri
    
    fun getCurrentStatusType() = _uiState.value.status?.type?.name?.lowercase() ?: "image"
    
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
