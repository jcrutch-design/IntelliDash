package com.example.intellidash.ui.media

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.example.intellidash.data.MediaItem
import com.example.intellidash.ui.theme.CyberBlue
import com.example.intellidash.ui.theme.CyberDark
import com.example.intellidash.ui.theme.CyberGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaIndexerScreen(
    viewModel: MediaIndexerViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val navigator = rememberListDetailPaneScaffoldNavigator<MediaItem>()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CyberDark)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "MEDIA INDEXER",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = CyberBlue
                            )
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = CyberDark
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberDark,
                    contentColor = CyberBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("DASHBOARD") },
                        icon = { Icon(Icons.Rounded.Dashboard, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("SEARCH / GALLERY") },
                        icon = { Icon(Icons.Rounded.Search, null) }
                    )
                }
            }
        },
        containerColor = CyberDark
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> IndexerDashboard(viewModel)
                1 -> GalleryContent(viewModel, navigator)
            }
        }
    }
}

@Composable
fun IndexerDashboard(viewModel: MediaIndexerViewModel) {
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val indexedCount by viewModel.indexedCount.collectAsStateWithLifecycle()
    val workInfo by viewModel.workInfo.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "INDEXING PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { if (totalCount > 0) indexedCount.toFloat() / totalCount else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberGreen,
                    trackColor = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Indexed $indexedCount / Total $totalCount images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        "${if (totalCount > 0) (indexedCount * 100 / totalCount) else 0}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberGreen
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "DISCOVERY",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberBlue
                    )
                    Text(
                        "Scan for new media",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { viewModel.refreshDiscovery() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, tint = CyberBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("SCAN NOW", color = CyberBlue)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "DISCOVERY",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberBlue
                    )
                    Text(
                        "Scan for new media",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { viewModel.refreshDiscovery() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, tint = CyberBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("SCAN NOW", color = CyberBlue)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "WORKMANAGER STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberBlue
                    )
                    Text(
                        text = workInfo?.state?.name ?: "IDLE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (workInfo?.state == WorkInfo.State.RUNNING) CyberGreen else Color.White
                    )
                }
                Switch(
                    checked = workInfo != null && workInfo?.state != WorkInfo.State.CANCELLED,
                    onCheckedChange = { viewModel.toggleIndexing(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberGreen,
                        checkedTrackColor = CyberGreen.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GalleryContent(
    viewModel: MediaIndexerViewModel,
    navigator: ThreePaneScaffoldNavigator<MediaItem>
) {
    val items by viewModel.mediaItems.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search tags, categories, summaries...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberBlue,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            MediaThumbnail(item, viewModel) {
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                                }
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.contentKey?.let { item ->
                    MediaDetailView(item, viewModel) {
                        scope.launch {
                            navigator.navigateBack()
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MediaThumbnail(
    item: MediaItem,
    viewModel: MediaIndexerViewModel,
    onClick: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(item.path) {
        withContext(Dispatchers.IO) {
            bitmap = viewModel.loadBitmap(item.path)
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (item.summary == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = CyberBlue)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailView(
    item: MediaItem,
    viewModel: MediaIndexerViewModel,
    onClose: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(item.path) {
        withContext(Dispatchers.IO) {
            bitmap = viewModel.loadBitmap(item.path)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "IMAGE DETAIL",
                style = MaterialTheme.typography.labelLarge,
                color = CyberBlue
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = CyberBlue)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "SUMMARY",
            style = MaterialTheme.typography.labelSmall,
            color = CyberGreen
        )
        Text(
            text = item.summary ?: "No summary available.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            color = CyberGreen
        )
        Text(
            text = item.category ?: "Unknown",
            style = MaterialTheme.typography.titleMedium,
            color = CyberBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "TAGS",
            style = MaterialTheme.typography.labelSmall,
            color = CyberGreen
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.tags.forEach { tag ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(tag) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = CyberBlue.copy(alpha = 0.1f),
                        labelColor = CyberBlue
                    ),
                    border = null // Fix border issue by setting null or a proper BorderStroke
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "PATH: ${item.path}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
