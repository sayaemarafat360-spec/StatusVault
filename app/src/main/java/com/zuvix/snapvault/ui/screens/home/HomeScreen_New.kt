package com.snaphubpro.zuvixapp.ui.screens.home

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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.snaphubpro.zuvixapp.R
import com.snaphubpro.zuvixapp.data.model.MediaType
import com.snaphubpro.zuvixapp.data.model.StatusItem
import com.snaphubpro.zuvixapp.ui.theme.Accent
import com.snaphubpro.zuvixapp.ui.theme.Background
import com.snaphubpro.zuvixapp.ui.theme.TextPrimary
import com.snaphubpro.zuvixapp.ui.theme.TextSecondary
import com.snaphubpro.zuvixapp.util.openWhatsApp

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
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = context as? Activity

    var showBulkSaveDialog by remember { mutableStateOf(false) }
    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            viewModel.loadStatuses()
        }
    }

    LaunchedEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(getRequiredPermissions())
        }
    }

    LaunchedEffect(uiState.showNewStatusSnackbar) {
        if (uiState.showNewStatusSnackbar) {
            snackbarHostState.showSnackbar(
                message = "✨ ${uiState.newStatusCount} new status found",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissNewStatusSnackbar()
        }
    }

    if (showBulkSaveDialog && uiState.isMultiSelectMode) {
        AlertDialog(
            onDismissRequest = { showBulkSaveDialog = false },
            title = { Text("Save ${uiState.selectedItems.size} items?") },
            text = { Text("Tap to save all selected statuses") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBulkSaveDialog = false
                        viewModel.saveSelectedItems { _ -> }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.isBulkSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Saving...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = uiState.bulkSaveProgress.toFloat() / uiState.bulkSaveTotal.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${uiState.bulkSaveProgress}/${uiState.bulkSaveTotal}", fontSize = 12.sp)
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { context.openWhatsApp() },
                containerColor = Accent,
                contentColor = Background
            ) {
                Text("WA", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        when {
            !hasPermissions -> PermissionContent(
                onAllow = { permissionLauncher.launch(getRequiredPermissions()) },
                modifier = Modifier.padding(paddingValues)
            )
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(paddingValues))
            uiState.statuses.isEmpty() -> EmptyContent(
                onOpenWhatsApp = { context.openWhatsApp() },
                onRefresh = { viewModel.refreshStatuses() },
                modifier = Modifier.padding(paddingValues)
            )
            else -> HomeContent(
                uiState = uiState,
                viewModel = viewModel,
                onStatusClick = onStatusClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onStatusClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (uiState.selectedTab == MediaType.IMAGE) 0 else 1,
            containerColor = Background,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.selectedTab == MediaType.IMAGE) 0 else 1]),
                    color = Accent,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = uiState.selectedTab == MediaType.IMAGE,
                onClick = { viewModel.selectTab(MediaType.IMAGE) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Image, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${stringResource(R.string.images_tab)} (${uiState.imageStatuses.size})")
                    }
                },
                selectedContentColor = Accent,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = uiState.selectedTab == MediaType.VIDEO,
                onClick = { viewModel.selectTab(MediaType.VIDEO) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.VideoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${stringResource(R.string.videos_tab)} (${uiState.videoStatuses.size})")
                    }
                },
                selectedContentColor = Accent,
                unselectedContentColor = TextSecondary
            )
        }

        val currentStatuses = if (uiState.selectedTab == MediaType.IMAGE) uiState.imageStatuses else uiState.videoStatuses

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentStatuses, key = { it.id }) { status ->
                StatusCard(
                    status = status,
                    isSelected = status.id in uiState.selectedItems,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    isNew = status.id in uiState.newStatusIds,
                    onClick = {
                        if (uiState.isMultiSelectMode) {
                            viewModel.toggleItemSelection(status.id)
                        } else {
                            viewModel.markStatusAsSeen(status.id)
                            onStatusClick(status.id)
                        }
                    },
                    onLongClick = {
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
private fun StatusCard(
    status: StatusItem,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isNew: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 0.95f else 1f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        AsyncImage(
            model = status.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (status.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        AnimatedVisibility(
            visible = isNew && !isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).background(Accent, CircleShape))
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Accent.copy(alpha = 0.3f)))
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.size(24.dp).background(Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Background, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    isMultiSelectMode: Boolean,
    selectedCount: Int,
    onRefresh: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearSelection: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = isMultiSelectMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Close, null, tint = Accent)
                    }
                    Text("$selectedCount", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                }
            }

            AnimatedVisibility(visible = !isMultiSelectMode) {
                Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            }

            Spacer(Modifier.weight(1f))

            if (isMultiSelectMode && selectedCount > 0) {
                IconButton(onClick = onSaveClick) {
                    Icon(Icons.Default.Save, null, tint = Accent)
                }
            }

            if (!isMultiSelectMode) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, null, tint = TextPrimary)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, null, tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun PermissionContent(
    onAllow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Close, null, modifier = Modifier.size(80.dp), tint = Accent)
        Spacer(Modifier.height(20.dp))
        Text("Media Permission Required", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Grant access to view statuses", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onAllow, colors = androidx.compose.material3.ButtonDefaults.buttonColors(Accent)) {
            Text("Allow", color = Background)
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Accent, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Loading statuses...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyContent(
    onOpenWhatsApp: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(80.dp), tint = Accent.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.no_statuses_title), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.no_statuses_desc), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.material3.OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.refresh))
            }
            androidx.compose.material3.Button(onClick = onOpenWhatsApp, colors = androidx.compose.material3.ButtonDefaults.buttonColors(Accent)) {
                Text(stringResource(R.string.open_whatsapp), color = Background)
            }
        }
    }
}

private fun checkPermissions(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

private fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
