package com.zuvix.snapvault.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val onboardingShown = preferencesManager.onboardingShown
    
    fun setOnboardingShown() {
        viewModelScope.launch {
            preferencesManager.setOnboardingShown(true)
        }
    }
}
