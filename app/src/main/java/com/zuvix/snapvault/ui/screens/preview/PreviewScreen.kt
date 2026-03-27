package com.zuvix.snapvault.ui.screens.preview

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.zuvix.snapvault.R
import com.zuvix.snapvault.data.model.MediaType
import com.zuvix.snapvault.ui.theme.Accent
import com.zuvix.snapvault.ui.theme.Background
import com.zuvix.snapvault.ui.theme.TextPrimary
import com.zuvix.snapvault.ui.theme.TextSecondary
import com.zuvix.snapvault.util.shareFile
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel,
    statusId: String,
    onBack: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val isRewardedAdReady = viewModel.isRewardedAdReady
    val isAdLoading by viewModel.isAdLoading.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val context = LocalContext.current
    
    // Get Activity reference for ads
    val activity = context as? Activity
    
    var showControls by remember { mutableStateOf(true) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    
    // Set activity reference in ViewModel for ads
    LaunchedEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
    }
    
    val pagerState = rememberPagerState(
        initialPage = uiState.currentIndex,
        pageCount = { uiState.allStatuses.size }
    )
    
    // Sync pager state with view model
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentIndex(pagerState.currentPage)
    }
    
    // Show save success animation
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showSaveSuccess = true
            delay(1500)
            showSaveSuccess = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Media pager
        if (uiState.allStatuses.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val status = uiState.allStatuses.getOrNull(page)
                
                if (status != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls }
                                )
                            }
                    ) {
                        if (status.type == MediaType.VIDEO) {
                            VideoPlayer(
                                uri = status.uri.toString(),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = status.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
        
        // Top bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    
                    Text(
                        text = "${uiState.currentIndex + 1} / ${uiState.allStatuses.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        
        // Bottom controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewBottomControls(
                isSaving = uiState.isSaving,
                isSaved = uiState.isSaved,
                isPremium = isPremium,
                onSaveClick = { viewModel.saveCurrentStatus() },
                onShareClick = {
                    val uri = viewModel.getCurrentStatusUri()
                    val type = viewModel.getCurrentStatusType()
                    uri?.let { context.shareFile(it, type) }
                },
                onFavoriteClick = { /* TODO */ },
                modifier = Modifier.navigationBarsPadding()
            )
        }
        
        // Save success overlay
        AnimatedVisibility(
            visible = showSaveSuccess,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp)
        ) {
            SaveSuccessIndicator()
        }
        
        // Loading indicator
        if (uiState.isSaving) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Accent,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        // Initial loading
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        }
    }
}

@Composable
fun VideoPlayer(
    uri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                controllerShowTimeoutMs = 3000
                controllerAutoShow = true
            }
        },
        modifier = modifier
    )
}

@Composable
fun PreviewBottomControls(
    isSaving: Boolean,
    isSaved: Boolean,
    isPremium: Boolean,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = if (isSaved) Icons.Filled.Check else Icons.Outlined.SaveAlt,
                label = if (isSaved) stringResource(R.string.saved) else stringResource(R.string.save),
                onClick = onSaveClick,
                enabled = !isSaving && !isSaved,
                accentColor = Accent
            )
            
            ActionButton(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.share),
                onClick = onShareClick,
                enabled = true
            )
            
            ActionButton(
                icon = Icons.Filled.FavoriteBorder,
                label = stringResource(R.string.favorite),
                onClick = onFavoriteClick,
                enabled = true
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accentColor: Color = TextPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (enabled) accentColor.copy(alpha = 0.2f) else TextSecondary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) accentColor else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) accentColor else TextSecondary
        )
    }
}

@Composable
fun SaveSuccessIndicator() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("save_success.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(80.dp)
        )
        
        Text(
            text = stringResource(R.string.saved),
            style = MaterialTheme.typography.titleMedium,
            color = Accent
        )
    }
}
