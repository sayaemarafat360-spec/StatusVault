package com.zuvix.snapvault.ui.screens.saved

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

import com.zuvix.snapvault.R
import com.zuvix.snapvault.data.model.SavedStatus
import com.zuvix.snapvault.ui.theme.Accent
import com.zuvix.snapvault.ui.theme.Background
import com.zuvix.snapvault.ui.theme.PremiumEnd
import com.zuvix.snapvault.ui.theme.PremiumStart
import com.zuvix.snapvault.ui.theme.Surface
import com.zuvix.snapvault.ui.theme.TextPrimary
import com.zuvix.snapvault.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onBack: () -> Unit,
    onVaultClick: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle(lifecycle = lifecycle)
    
    val tabs = listOf(
        stringResource(R.string.saved_title),
        stringResource(R.string.favorites_title),
        stringResource(R.string.vault_title)
    )
    
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.saved_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Background,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = Accent,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (index == 2) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(title)
                                }
                            },
                            selectedContentColor = if (index == 2 && !isPremium) PremiumStart else Accent,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.selectedTab) {
                0 -> SavedItemsGrid(
                    items = uiState.savedItems,
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onMoveToVault = { viewModel.moveToVault(it) },
                    onDelete = { viewModel.deleteItem(it) },
                    isPremium = isPremium
                )
                1 -> SavedItemsGrid(
                    items = uiState.favorites,
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onMoveToVault = { viewModel.moveToVault(it) },
                    onDelete = { viewModel.deleteItem(it) },
                    isPremium = isPremium,
                    emptyMessage = stringResource(R.string.no_favorites),
                    emptyHint = stringResource(R.string.mark_as_favorite)
                )
                2 -> {
                    if (isPremium) {
                        SavedItemsGrid(
                            items = uiState.vaultItems,
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            onMoveToVault = { viewModel.moveToVault(it) },
                            onDelete = { viewModel.deleteItem(it) },
                            isPremium = isPremium,
                            emptyMessage = stringResource(R.string.no_vault_items),
                            emptyHint = stringResource(R.string.move_to_vault_hint)
                        )
                    } else {
                        PremiumVaultPrompt(onUpgradeClick = { /* TODO */ })
                    }
                }
            }
        }
    }
}

@Composable
fun SavedItemsGrid(
    items: List<SavedStatus>,
    onFavoriteClick: (SavedStatus) -> Unit,
    onMoveToVault: (SavedStatus) -> Unit,
    onDelete: (SavedStatus) -> Unit,
    isPremium: Boolean,
    emptyMessage: String = stringResource(R.string.no_saved_items),
    emptyHint: String = stringResource(R.string.save_some_statuses)
) {
    if (items.isEmpty()) {
        EmptySavedState(
            message = emptyMessage,
            hint = emptyHint
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                SavedItemCard(
                    item = item,
                    isPremium = isPremium,
                    onFavoriteClick = { onFavoriteClick(item) },
                    onMoveToVault = { onMoveToVault(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedItemCard(
    item: SavedStatus,
    isPremium: Boolean,
    onFavoriteClick: () -> Unit,
    onMoveToVault: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { showMenu = true },
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.savedUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Video indicator
            if (item.type == com.zuvix.snapvault.data.model.MediaType.VIDEO) {
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
            
            // Favorite indicator
            if (item.isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(Accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = Background,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // Vault indicator
            if (item.isVault) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PremiumStart, PremiumEnd)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Vault",
                        tint = Background,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // Action menu overlay
            if (showMenu) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Accent else TextPrimary
                        )
                    }
                    
                    if (isPremium && !item.isVault) {
                        IconButton(onClick = onMoveToVault) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Move to Vault",
                                tint = PremiumStart
                            )
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Image, // Replace with delete icon
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                    
                    IconButton(onClick = { showMenu = false }) {
                        Text("✕", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySavedState(
    message: String,
    hint: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PremiumVaultPrompt(
    onUpgradeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = PremiumStart
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.premium_feature),
            style = MaterialTheme.typography.headlineSmall,
            color = PremiumStart
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Upgrade to Premium to access the Hidden Vault",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        androidx.compose.material3.Button(
            onClick = onUpgradeClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = PremiumStart
            ),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.upgrade_to_premium),
                color = Background
            )
        }
    }
}
