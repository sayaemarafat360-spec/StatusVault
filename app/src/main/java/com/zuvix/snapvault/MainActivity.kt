package com.snaphubpro.zuvixapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.zuvix.snapvault.ads.AdManager
import com.zuvix.snapvault.ui.navigation.SnapVaultNavigation
import com.zuvix.snapvault.ui.theme.SnapVaultTheme
import com.zuvix.snapvault.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var adManager: AdManager
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Show app open interstitial ad (3/day limit for non-premium)
        showAppOpenAdIfNeeded()
        
        setContent {
            SnapVaultTheme {
                val navController = rememberNavController()
                SnapVaultNavigation(navController = navController)
            }
        }
    }
    
    private fun showAppOpenAdIfNeeded() {
        lifecycleScope.launch {
            val isPremium = preferencesManager.isPremium.first()
            val shouldShow = preferencesManager.shouldShowInterstitial()
            
            if (!isPremium && shouldShow && adManager.isInterstitialReady()) {
                adManager.showInterstitialAd(this@MainActivity) {
                    // Record that we showed an ad
                    lifecycleScope.launch {
                        preferencesManager.recordInterstitialShown()
                    }
                }
            }
        }
    }
}
