package com.zuvix.snapvault.ui.screens.home

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zuvix.snapvault.R
import com.zuvix.snapvault.data.model.MediaType
import com.zuvix.snapvault.data.model.StatusItem
import com.zuvix.snapvault.ui.theme.Accent
import com.zuvix.snapvault.ui.theme.Background
import com.zuvix.snapvault.ui.theme.Surface
import com.zuvix.snapvault.ui.theme.TextPrimary
import com.zuvix.snapvault.ui.theme.TextSecondary
import com.zuvix.snapvault.util.openWhatsApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStatusClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get Activity reference for ads
    val activity = context as? Activity
    
    var showBulkSaveDialog by remember { mutableStateOf(false) }
    
    // Set activity reference in ViewModel for ads
    LaunchedEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
    }
    
    // Permission handling
    var hasPermissions by remember { 
        mutableStateOf(checkPermissions(context)) 
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            viewModel.loadStatuses()
        }
    }
    
    LaunchedEffect(hasPermissions) {
        if (!hasPermissions) {
            permissionLauncher.launch(getRequiredPermissions())
        }
    }
    
    // Handle new status snackbar
    LaunchedEffect(uiState.showNewStatusSnackbar) {
        if (uiState.showNewStatusSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.new_statuses_found, uiState.newStatusCount),
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.Dismissed -> viewModel.dismissNewStatusSnackbar()
                SnackbarResult.ActionPerformed -> viewModel.dismissNewStatusSnackbar()
            }
        }
    }
    
    // Handle bulk save result
    LaunchedEffect(uiState.bulkSaveResult) {
        uiState.bulkSaveResult?.let { result ->
            val message = if (result.failedCount > 0) {
                "Saved ${result.successCount} of ${result.totalItems} items"
            } else {
                "Saved ${result.successCount} items successfully!"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearBulkSaveResult()
        }
    }
    
    // Bulk save dialog
    if (showBulkSaveDialog && uiState.isMultiSelectMode) {
        AlertDialog(
            onDismissRequest = { showBulkSaveDialog = false },
            title = { Text("Save ${uiState.selectedItems.size} items?") },
            text = { 
                Text(if (isPremium) "Save all selected items?" else "Watch a short ad to save all selected items") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBulkSaveDialog = false
                        viewModel.saveSelectedItems { _ ->
                            // Bulk save complete
                        }
                    }
                ) {
                    Text(if (isPremium) "Save" else "Watch Ad & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Bulk save progress dialog
    if (uiState.isBulkSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Saving...") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = uiState.bulkSaveProgress.toFloat() / uiState.bulkSaveTotal.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${uiState.bulkSaveProgress} of ${uiState.bulkSaveTotal}")
                }
            },
            confirmButton = { }
        )
    }
    
    Scaffold(
        topBar = {
            HomeTopBar(
                isMultiSelectMode = uiState.isMultiSelectMode,
                selectedCount = uiState.selectedItems.size,
                onRefresh = { viewModel.refreshStatuses() },
                onSettingsClick = onSettingsClick,
                onClearSelection = { viewModel.clearSelection() },
                onSaveClick = { showBulkSaveDialog = true }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { context.openWhatsApp() },
                containerColor = Accent,
                contentColor = Background
            ) {
                Text("WA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (!hasPermissions) {
            PermissionRequestContent(
                onAllowClick = {
                    permissionLauncher.launch(getRequiredPermissions())
                }
            )
        } else if (uiState.isLoading) {
            LoadingContent()
        } else if (uiState.statuses.isEmpty()) {
            EmptyContent(
                onOpenWhatsApp = { context.openWhatsApp() },
                onRefresh = { viewModel.refreshStatuses() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab row
                TabRow(
                    selectedTabIndex = if (uiState.selectedTab == MediaType.IMAGE) 0 else 1,
                    containerColor = Background,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(
                                tabPositions[if (uiState.selectedTab == MediaType.IMAGE) 0 else 1]
                            ),
                            color = Accent,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == MediaType.IMAGE,
                        onClick = { viewModel.selectTab(MediaType.IMAGE) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${stringResource(R.string.images_tab)} (${uiState.imageStatuses.size})"
                                )
                            }
                        },
                        selectedContentColor = Accent,
                        unselectedContentColor = TextSecondary
                    )
                    
                    Tab(
                        selected = uiState.selectedTab == MediaType.VIDEO,
                        onClick = { viewModel.selectTab(MediaType.VIDEO) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VideoFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${stringResource(R.string.videos_tab)} (${uiState.videoStatuses.size})"
                                )
                            }
                        },
                        selectedContentColor = Accent,
                        unselectedContentColor = TextSecondary
                    )
                }
                
                // Status grid
                val currentStatuses = if (uiState.selectedTab == MediaType.IMAGE) {
                    uiState.imageStatuses
                } else {
                    uiState.videoStatuses
                }
                
                StatusGrid(
                    statuses = currentStatuses,
                    selectedItems = uiState.selectedItems,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    newStatusIds = uiState.newStatusIds,
                    onItemClick = { status ->
                        if (uiState.isMultiSelectMode) {
                            viewModel.toggleItemSelection(status.id)
                        } else {
                            viewModel.markStatusAsSeen(status.id)
                            onStatusClick(status.id)
                        }
                    },
                    onItemLongClick = { status ->
                        if (!uiState.isMultiSelectMode) {
                            viewModel.toggleItemSelection(status.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeTopBar(
    isMultiSelectMode: Boolean,
    selectedCount: Int,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearSelection: () -> Unit,
    onSaveClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isMultiSelectMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Clear selection",
                            tint = Accent
                        )
                    }
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }
            
            AnimatedVisibility(
                visible = !isMultiSelectMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button in multi-select mode
            AnimatedVisibility(
                visible = isMultiSelectMode && selectedCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save selected",
                        tint = Accent
                    )
                }
            }
            
            AnimatedVisibility(
                visible = !isMultiSelectMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = TextPrimary
                    )
                }
            }
            
            AnimatedVisibility(
                visible = !isMultiSelectMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusGrid(
    statuses: List<StatusItem>,
    selectedItems: Set<String>,
    isMultiSelectMode: Boolean,
    newStatusIds: Set<String>,
    onItemClick: (StatusItem) -> Unit,
    onItemLongClick: (StatusItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses, key = { it.id }) { status ->
            StatusItemCard(
                status = status,
                isSelected = status.id in selectedItems,
                isMultiSelectMode = isMultiSelectMode,
                isNew = status.id in newStatusIds,
                onClick = { onItemClick(status) },
                onLongClick = { onItemLongClick(status) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusItemCard(
    status: StatusItem,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isNew: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "selection_scale"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(selectionScale)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        // Image thumbnail
        AsyncImage(
            model = status.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Video indicator
        if (status.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // NEW badge (green dot)
        AnimatedVisibility(
            visible = isNew && !isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(12.dp)
                    .background(Accent, CircleShape)
            )
        }
        
        // Selection overlay
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Accent.copy(alpha = 0.3f))
            )
        }
        
        // Selection checkmark
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .background(Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Background,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionRequestContent(
    onAllowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("permission.json")
        )
        val progress by animateLottieCompositionAsState(composition)
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.permission_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        androidx.compose.material3.Button(
            onClick = onAllowClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Accent
            )
        ) {
            Text(
                text = stringResource(R.string.allow),
                color = Background
            )
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("loading.json")
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1
        )
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(150.dp)
        )
    }
}

@Composable
fun EmptyContent(
    onOpenWhatsApp: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("empty_state.json")
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1
        )
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.no_statuses_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.no_statuses_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.refresh))
            }
            
            androidx.compose.material3.Button(
                onClick = onOpenWhatsApp,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Accent
                )
            ) {
                Text(
                    text = stringResource(R.string.open_whatsapp),
                    color = Background
                )
            }
        }
    }
}

// Helper functions
private fun checkPermissions(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
