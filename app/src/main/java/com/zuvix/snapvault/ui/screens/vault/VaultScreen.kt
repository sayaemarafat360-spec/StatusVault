package com.zuvix.snapvault.ui.screens.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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
fun VaultScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle(lifecycle = lifecycle)
    
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = PremiumStart,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.vault_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (!isPremium) {
            PremiumRequiredContent()
        } else if (uiState.isLocked) {
            PinEntryContent(
                isPinSetup = uiState.isPinSetup,
                isConfirmingPin = uiState.isConfirmingPin,
                enteredPin = uiState.enteredPin,
                pinError = uiState.pinError,
                onDigitEntered = { viewModel.onPinDigitEntered(it) },
                onDelete = { viewModel.onPinDelete() },
                modifier = Modifier.padding(paddingValues)
            )
        } else if (uiState.isLoading) {
            LoadingContent()
        } else {
            VaultGrid(
                items = uiState.vaultItems,
                onDelete = { viewModel.deleteItem(it) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun PinEntryContent(
    isPinSetup: Boolean,
    isConfirmingPin: Boolean,
    enteredPin: String,
    pinError: String?,
    onDigitEntered: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("vault_lock.json")
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1
        )
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (!isPinSetup) {
                if (isConfirmingPin) stringResource(R.string.vault_confirm_pin)
                else stringResource(R.string.vault_setup_title)
            } else {
                stringResource(R.string.vault_pin_title)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (!isPinSetup) {
                stringResource(R.string.vault_setup_desc)
            } else {
                stringResource(R.string.vault_pin_desc)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                PinDot(
                    isFilled = index < enteredPin.length,
                    isError = pinError != null
                )
            }
        }
        
        // Error message
        AnimatedVisibility(
            visible = pinError != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Text(
                text = pinError ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Numeric keypad
        NumericKeypad(
            onDigitClick = onDigitEntered,
            onDelete = onDelete,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun PinDot(
    isFilled: Boolean,
    isError: Boolean
) {
    val color by animateColorAsState(
        targetValue = when {
            isError -> Color.Red
            isFilled -> PremiumStart
            else -> TextSecondary.copy(alpha = 0.3f)
        },
        label = "pin_dot_color"
    )
    
    val size by animateDpAsState(
        targetValue = if (isFilled) 16.dp else 14.dp,
        label = "pin_dot_size"
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if (isFilled) color else Color.Transparent,
                shape = CircleShape
            )
            .then(
                if (!isFilled) {
                    Modifier.clip(CircleShape).background(
                        color = color,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "del")
        )
        
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(64.dp))
                    } else if (key == "del") {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Text(
                                text = "⌫",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .clickable { onDigitClick(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultGrid(
    items: List<SavedStatus>,
    onDelete: (SavedStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Asset("empty_vault.json")
            )
            val progress by animateLottieCompositionAsState(composition)
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(160.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.no_vault_items),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.move_to_vault_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                VaultItemCard(
                    item = item,
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
fun VaultItemCard(
    item: SavedStatus,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { showMenu = !showMenu },
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
            
            // Vault badge
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
            
            // Delete menu
            this@Box.AnimatedVisibility(
                visible = showMenu,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
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
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumRequiredContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("premium_lock.json")
        )
        val progress by animateLottieCompositionAsState(composition)
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(160.dp)
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
            iterations = Int.MAX_VALUE
        )
        
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(100.dp)
        )
    }
}
