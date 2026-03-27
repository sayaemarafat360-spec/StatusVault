package com.snaphubpro.zuvixapp.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaphubpro.zuvixapp.data.model.SavedStatus
import com.snaphubpro.zuvixapp.data.repository.StatusRepository
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUiState(
    val isLocked: Boolean = true,
    val isPinSetup: Boolean = false,
    val enteredPin: String = "",
    val confirmPin: String = "",
    val isConfirmingPin: Boolean = false,
    val pinError: String? = null,
    val vaultItems: List<SavedStatus> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: StatusRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()
    
    val isPremium = preferencesManager.isPremium.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )
    
    init {
        viewModelScope.launch {
            val pinSet = preferencesManager.vaultPinSet.first()
            _uiState.value = _uiState.value.copy(
                isPinSetup = pinSet,
                isLocked = pinSet
            )
        }
    }
    
    fun onPinDigitEntered(digit: String) {
        if (_uiState.value.enteredPin.length < 4) {
            val newPin = _uiState.value.enteredPin + digit
            _uiState.value = _uiState.value.copy(
                enteredPin = newPin,
                pinError = null
            )
            
            if (newPin.length == 4) {
                handlePinComplete(newPin)
            }
        }
    }
    
    fun onPinDelete() {
        val currentPin = _uiState.value.enteredPin
        if (currentPin.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                enteredPin = currentPin.dropLast(1),
                pinError = null
            )
        }
    }
    
    private fun handlePinComplete(pin: String) {
        viewModelScope.launch {
            if (!_uiState.value.isPinSetup) {
                // Setup mode
                if (!_uiState.value.isConfirmingPin) {
                    // First PIN entry
                    _uiState.value = _uiState.value.copy(
                        enteredPin = "",
                        confirmPin = pin,
                        isConfirmingPin = true
                    )
                } else {
                    // Confirm PIN
                    if (pin == _uiState.value.confirmPin) {
                        preferencesManager.setVaultPin(pin.toInt())
                        unlockVault()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            enteredPin = "",
                            pinError = "PINs don't match"
                        )
                    }
                }
            } else {
                // Verify mode
                val isCorrect = preferencesManager.verifyVaultPin(pin.toInt())
                if (isCorrect) {
                    unlockVault()
                } else {
                    _uiState.value = _uiState.value.copy(
                        enteredPin = "",
                        pinError = "Incorrect PIN"
                    )
                }
            }
        }
    }
    
    private fun unlockVault() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLocked = false,
                isLoading = true
            )
            
            val items = repository.vaultItems.first()
            
            _uiState.value = _uiState.value.copy(
                vaultItems = items,
                isLoading = false
            )
        }
    }
    
    fun deleteItem(item: SavedStatus) {
        viewModelScope.launch {
            repository.deleteSavedStatus(item)
            _uiState.value = _uiState.value.copy(
                vaultItems = repository.vaultItems.first()
            )
        }
    }
}
