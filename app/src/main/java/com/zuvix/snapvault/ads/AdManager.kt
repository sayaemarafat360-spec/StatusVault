package com.snaphubpro.zuvixapp.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.zuvix.snapvault.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Complete AdMob Integration
 * - Interstitial Ads (App Open - 3/day limit)
 * - Rewarded Ads (After save)
 * - Preloading for instant display
 * - Error handling and retry logic
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AdManager"
        
        // Test Ad IDs (replace with real IDs in production)
        private const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
    }
    
    // Ad states
    private val _interstitialAd = MutableStateFlow<InterstitialAd?>(null)
    val interstitialAd: StateFlow<InterstitialAd?> = _interstitialAd.asStateFlow()
    
    private val _rewardedAd = MutableStateFlow<RewardedAd?>(null)
    val rewardedAd: StateFlow<RewardedAd?> = _rewardedAd.asStateFlow()
    
    private val _isInterstitialLoading = MutableStateFlow(false)
    val isInterstitialLoading: StateFlow<Boolean> = _isInterstitialLoading.asStateFlow()
    
    private val _isRewardedLoading = MutableStateFlow(false)
    val isRewardedLoading: StateFlow<Boolean> = _isRewardedLoading.asStateFlow()
    
    private val _isAdShowing = MutableStateFlow(false)
    val isAdShowing: StateFlow<Boolean> = _isAdShowing.asStateFlow()
    
    // Callbacks
    private var onRewardedComplete: (() -> Unit)? = null
    private var onRewardedFailed: (() -> Unit)? = null
    private var onInterstitialClosed: (() -> Unit)? = null
    
    init {
        // Preload ads on init
        loadInterstitialAd()
        loadRewardedAd()
    }
    
    // ==================== INTERSTITIAL AD ====================
    
    fun loadInterstitialAd() {
        if (_interstitialAd.value != null || _isInterstitialLoading.value) {
            return
        }
        
        _isInterstitialLoading.value = true
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    _interstitialAd.value = ad
                    _isInterstitialLoading.value = false
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            _interstitialAd.value = null
                            _isAdShowing.value = false
                            loadInterstitialAd() // Preload next
                            onInterstitialClosed?.invoke()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                            _interstitialAd.value = null
                            _isAdShowing.value = false
                            loadInterstitialAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad showed")
                            _isAdShowing.value = true
                        }
                        
                        override fun onAdClicked() {
                            Log.d(TAG, "Interstitial ad clicked")
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "Interstitial ad impression")
                        }
                    }
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${adError.message}")
                    _interstitialAd.value = null
                    _isInterstitialLoading.value = false
                    
                    // Retry after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadInterstitialAd()
                    }, 30000) // 30 second retry
                }
            }
        )
    }
    
    fun showInterstitialAd(
        activity: Activity,
        onClosed: () -> Unit = {}
    ) {
        onInterstitialClosed = onClosed
        
        val ad = _interstitialAd.value
        if (ad != null) {
            Log.d(TAG, "Showing interstitial ad")
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready, loading...")
            loadInterstitialAd()
            onClosed() // Proceed without ad
        }
    }
    
    fun isInterstitialReady(): Boolean = _interstitialAd.value != null
    
    // ==================== REWARDED AD ====================
    
    fun loadRewardedAd() {
        if (_rewardedAd.value != null || _isRewardedLoading.value) {
            return
        }
        
        _isRewardedLoading.value = true
        
        RewardedAd.load(
            context,
            REWARDED_AD_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
                    _rewardedAd.value = ad
                    _isRewardedLoading.value = false
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed")
                            _rewardedAd.value = null
                            _isAdShowing.value = false
                            loadRewardedAd() // Preload next
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Rewarded failed to show: ${adError.message}")
                            _rewardedAd.value = null
                            _isAdShowing.value = false
                            loadRewardedAd()
                            onRewardedFailed?.invoke()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad showed")
                            _isAdShowing.value = true
                        }
                        
                        override fun onAdClicked() {
                            Log.d(TAG, "Rewarded ad clicked")
                        }
                        
                        override fun onAdImpression() {
                            Log.d(TAG, "Rewarded ad impression")
                        }
                    }
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded failed to load: ${adError.message}")
                    _rewardedAd.value = null
                    _isRewardedLoading.value = false
                    
                    // Retry after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadRewardedAd()
                    }, 30000) // 30 second retry
                }
            }
        )
    }
    
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: () -> Unit = {}
    ) {
        onRewardedComplete = onRewarded
        onRewardedFailed = onFailed
        
        val ad = _rewardedAd.value
        if (ad != null) {
            Log.d(TAG, "Showing rewarded ad")
            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
                onRewardedComplete?.invoke()
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready")
            // Proceed without showing ad (user gets reward anyway for test)
            onRewarded()
            loadRewardedAd()
        }
    }
    
    fun isRewardedReady(): Boolean = _rewardedAd.value != null
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if ad should be shown based on premium status and daily limit
     */
    fun shouldShowInterstitialAd(
        isPremium: Boolean,
        dailyCount: Int,
        maxDaily: Int = 3
    ): Boolean {
        if (isPremium) return false
        if (dailyCount >= maxDaily) return false
        return isInterstitialReady()
    }
    
    /**
     * Preload all ads
     */
    fun preloadAllAds() {
        loadInterstitialAd()
        loadRewardedAd()
    }
    
    /**
     * Force reload ads (e.g., after network reconnection)
     */
    fun forceReloadAds() {
        _interstitialAd.value = null
        _rewardedAd.value = null
        loadInterstitialAd()
        loadRewardedAd()
    }
}
