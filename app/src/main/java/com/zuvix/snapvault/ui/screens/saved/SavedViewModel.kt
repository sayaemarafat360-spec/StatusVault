package com.snaphubpro.zuvixapp.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaphubpro.zuvixapp.ads.AdManager
import com.snaphubpro.zuvixapp.data.model.SavedStatus
import com.zuvix.snapvault.data.repository.StatusRepository
import com.zuvix.snapvault.data.repository.Stats
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedUiState(
    val savedItems: List<SavedStatus> = emptyList(),
    val favorites: List<SavedStatus> = emptyList(),
    val vaultItems: List<SavedStatus> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val preferencesManager: PreferencesManager,
    private val adManager: AdManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()
    
    val isPremium = preferencesManager.isPremium.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    val vaultPinSet = preferencesManager.vaultPinSet.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    init {
        loadSavedItems()
    }
    
    private fun loadSavedItems() {
        // Collect from database flows
        viewModelScope.launch {
            repository.savedStatuses.collect { items ->
                _uiState.value = _uiState.value.copy(
                    savedItems = items,
                    isLoading = false
                )
            }
        }
        
        viewModelScope.launch {
            repository.favorites.collect { items ->
                _uiState.value = _uiState.value.copy(
                    favorites = items
                )
            }
        }
        
        viewModelScope.launch {
            repository.vaultItems.collect { items ->
                _uiState.value = _uiState.value.copy(
                    vaultItems = items
                )
            }
        }
    }
    
    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun toggleFavorite(item: SavedStatus) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }
    
    fun moveToVault(item: SavedStatus) {
        viewModelScope.launch {
            repository.moveToVault(item)
        }
    }
    
    fun deleteItem(item: SavedStatus) {
        viewModelScope.launch {
            repository.deleteSavedStatus(item)
        }
    }
    
    suspend fun getStats(): Stats {
        return repository.getStats()
    }
}
