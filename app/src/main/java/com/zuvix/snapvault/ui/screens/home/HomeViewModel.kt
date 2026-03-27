package com.snaphubpro.zuvixapp.ui.screens.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaphubpro.zuvixapp.ads.AdManager
import com.zuvix.snapvault.data.model.MediaType
import com.zuvix.snapvault.data.model.StatusItem
import com.zuvix.snapvault.data.repository.StatusRepository
import com.zuvix.snapvault.data.repository.Stats
import com.zuvix.snapvault.service.notification.NotificationEngine
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val statuses: List<StatusItem> = emptyList(),
    val imageStatuses: List<StatusItem> = emptyList(),
    val videoStatuses: List<StatusItem> = emptyList(),
    val selectedTab: MediaType = MediaType.IMAGE,
    val selectedItems: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val newStatusCount: Int = 0,
    val showNewStatusSnackbar: Boolean = false,
    val newStatusIds: Set<String> = emptySet(),
    val isBulkSaving: Boolean = false,
    val bulkSaveProgress: Int = 0,
    val bulkSaveTotal: Int = 0,
    val bulkSaveResult: BulkSaveResult? = null,
    val error: String? = null
)

data class BulkSaveResult(
    val successCount: Int,
    val failedCount: Int,
    val totalItems: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val preferencesManager: PreferencesManager,
    private val notificationEngine: NotificationEngine,
    private val adManager: AdManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    val isPremium = preferencesManager.isPremium.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    val isAdShowing = adManager.isAdShowing.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    private var activityRef: Activity? = null
    
    init {
        // Create notification channels
        notificationEngine.createNotificationChannels()
        
        // Load statuses
        loadStatuses()
        
        // Observe new status IDs from repository
        viewModelScope.launch {
            repository.newStatusIds.collect { newIds ->
                _uiState.value = _uiState.value.copy(
                    newStatusIds = newIds,
                    newStatusCount = newIds.size
                )
            }
        }
    }
    
    fun setActivity(activity: Activity) {
        activityRef = activity
    }
    
    fun loadStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val statuses = repository.loadStatuses()
                val imageStatuses = statuses.filter { it.type == MediaType.IMAGE }
                val videoStatuses = statuses.filter { it.type == MediaType.VIDEO }
                val newCount = repository.getNewStatusCount()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statuses = statuses,
                    imageStatuses = imageStatuses,
                    videoStatuses = videoStatuses,
                    newStatusCount = newCount,
                    showNewStatusSnackbar = newCount > 0,
                    error = null
                )
                
                // Schedule notification for new statuses
                if (newCount > 0) {
                    // Note: In production, you'd get bitmap from first new status
                    // notificationEngine.showNewStatusNotification(newCount, null, prefs)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refreshStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val (statuses, newCount) = repository.refreshStatuses()
                val imageStatuses = statuses.filter { it.type == MediaType.IMAGE }
                val videoStatuses = statuses.filter { it.type == MediaType.VIDEO }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statuses = statuses,
                    imageStatuses = imageStatuses,
                    videoStatuses = videoStatuses,
                    newStatusCount = newCount,
                    showNewStatusSnackbar = newCount > 0,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun dismissNewStatusSnackbar() {
        _uiState.value = _uiState.value.copy(
            showNewStatusSnackbar = false
        )
    }
    
    fun isNewStatus(statusId: String): Boolean {
        return repository.isNewStatus(statusId)
    }
    
    fun markStatusAsSeen(statusId: String) {
        repository.markAsSeen(statusId)
    }
    
    fun clearAllNewBadges() {
        repository.clearAllNewBadges()
        _uiState.value = _uiState.value.copy(
            newStatusCount = 0,
            newStatusIds = emptySet()
        )
    }
    
    fun selectTab(tab: MediaType) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun toggleItemSelection(itemId: String) {
        val currentSelected = _uiState.value.selectedItems
        val newSelected = if (itemId in currentSelected) {
            currentSelected - itemId
        } else {
            currentSelected + itemId
        }
        
        _uiState.value = _uiState.value.copy(
            selectedItems = newSelected,
            isMultiSelectMode = newSelected.isNotEmpty()
        )
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedItems = emptySet(),
            isMultiSelectMode = false
        )
    }
    
    fun getSelectedStatuses(): List<StatusItem> {
        val allStatuses = _uiState.value.statuses
        return allStatuses.filter { it.id in _uiState.value.selectedItems }
    }
    
    fun getStatusById(id: String): StatusItem? {
        return repository.getStatusById(id)
    }
    
    fun isRewardedAdReady(): Boolean = adManager.isRewardedReady()
    
    fun isInterstitialReady(): Boolean = adManager.isInterstitialReady()
    
    /**
     * Save selected items with rewarded ad for non-premium users
     */
    fun saveSelectedItems(
        toVault: Boolean = false,
        onAdRequired: (Int) -> Unit = {},
        onComplete: (BulkSaveResult) -> Unit = {}
    ) {
        val selectedStatuses = getSelectedStatuses()
        if (selectedStatuses.isEmpty()) return
        
        viewModelScope.launch {
            val premium = isPremium.first()
            
            if (premium) {
                // Premium users skip ads
                performBulkSave(selectedStatuses, toVault, onComplete)
            } else {
                // Show ad first, then save
                activityRef?.let { activity ->
                    adManager.showRewardedAd(
                        activity = activity,
                        onRewarded = {
                            viewModelScope.launch {
                                performBulkSave(selectedStatuses, toVault, onComplete)
                            }
                        },
                        onFailed = {
                            // Still allow save even if ad fails (better UX)
                            viewModelScope.launch {
                                performBulkSave(selectedStatuses, toVault, onComplete)
                            }
                        }
                    )
                } ?: run {
                    // No activity reference, just save
                    performBulkSave(selectedStatuses, toVault, onComplete)
                }
            }
        }
    }
    
    private suspend fun performBulkSave(
        statuses: List<StatusItem>,
        toVault: Boolean,
        onComplete: (BulkSaveResult) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(
            isBulkSaving = true,
            bulkSaveProgress = 0,
            bulkSaveTotal = statuses.size
        )
        
        var successCount = 0
        var failedCount = 0
        
        statuses.forEachIndexed { index, status ->
            val result = repository.saveStatus(status, toVault)
            if (result != null) {
                successCount++
                // Show progress notification
                notificationEngine.showDownloadProgressNotification(
                    current = index + 1,
                    total = statuses.size,
                    fileName = status.fileName
                )
            } else {
                failedCount++
            }
            
            _uiState.value = _uiState.value.copy(
                bulkSaveProgress = index + 1
            )
        }
        
        // Clear selection
        _uiState.value = _uiState.value.copy(
            isBulkSaving = false,
            selectedItems = emptySet(),
            isMultiSelectMode = false,
            bulkSaveResult = BulkSaveResult(successCount, failedCount, statuses.size)
        )
        
        // Show completion notification
        if (successCount > 0) {
            notificationEngine.cancelDownloadNotification()
            notificationEngine.showDownloadCompleteNotification(
                fileName = if (successCount == 1) statuses.first().fileName else "${successCount} items",
                savedCount = successCount
            )
        }
        
        onComplete(BulkSaveResult(successCount, failedCount, statuses.size))
    }
    
    fun clearBulkSaveResult() {
        _uiState.value = _uiState.value.copy(bulkSaveResult = null)
    }
    
    /**
     * Save all statuses at once
     */
    fun saveAllStatuses(
        onAdRequired: (Int) -> Unit = {},
        onComplete: (BulkSaveResult) -> Unit = {}
    ) {
        val allStatuses = _uiState.value.statuses
        if (allStatuses.isEmpty()) return
        
        viewModelScope.launch {
            val premium = isPremium.first()
            
            if (premium) {
                performBulkSave(allStatuses, false, onComplete)
            } else {
                activityRef?.let { activity ->
                    adManager.showRewardedAd(
                        activity = activity,
                        onRewarded = {
                            viewModelScope.launch {
                                performBulkSave(allStatuses, false, onComplete)
                            }
                        },
                        onFailed = {
                            viewModelScope.launch {
                                performBulkSave(allStatuses, false, onComplete)
                            }
                        }
                    )
                } ?: performBulkSave(allStatuses, false, onComplete)
            }
        }
    }
}
