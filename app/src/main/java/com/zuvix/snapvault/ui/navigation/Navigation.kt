package com.zuvix.snapvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.zuvix.snapvault.ui.screens.home.HomeScreen
import com.zuvix.snapvault.ui.screens.home.HomeViewModel
import com.zuvix.snapvault.ui.screens.onboarding.OnboardingScreen
import com.zuvix.snapvault.ui.screens.onboarding.OnboardingViewModel
import com.zuvix.snapvault.ui.screens.preview.PreviewScreen
import com.zuvix.snapvault.ui.screens.preview.PreviewViewModel
import com.zuvix.snapvault.ui.screens.saved.SavedScreen
import com.zuvix.snapvault.ui.screens.saved.SavedViewModel
import com.zuvix.snapvault.ui.screens.settings.SettingsScreen
import com.zuvix.snapvault.ui.screens.vault.VaultScreen
import com.zuvix.snapvault.ui.screens.vault.VaultViewModel

sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Preview : Screen("preview/{statusId}") {
        fun createRoute(statusId: String) = "preview/$statusId"
    }
    object Saved : Screen("saved")
    object Vault : Screen("vault")
    object Settings : Screen("settings")
}

@Composable
fun SnapVaultNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val onboardingShown by viewModel.onboardingShown.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)
            
            LaunchedEffect(onboardingShown) {
                if (onboardingShown) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }
            
            if (!onboardingShown) {
                OnboardingScreen(
                    onComplete = {
                        viewModel.setOnboardingShown()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStatusClick = { statusId ->
                    navController.navigate(Screen.Preview.createRoute(statusId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("statusId") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val viewModel: PreviewViewModel = hiltViewModel()
            val statusId = backStackEntry.arguments?.getString("statusId") ?: ""
            
            PreviewScreen(
                viewModel = viewModel,
                statusId = statusId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Saved.route) {
            val viewModel: SavedViewModel = hiltViewModel()
            SavedScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onVaultClick = { navController.navigate(Screen.Vault.route) }
            )
        }
        
        composable(Screen.Vault.route) {
            val viewModel: VaultViewModel = hiltViewModel()
            VaultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
