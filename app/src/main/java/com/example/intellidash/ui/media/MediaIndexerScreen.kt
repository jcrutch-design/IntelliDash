package com.example.intellidash.ui.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
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
    val workInfo by viewModel.workInfo.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importMedia(it, "Uncategorized") }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CyberDark)) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "MEDIA INDEXER",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = CyberBlue
                                )
                            )
                            if (workInfo?.state == WorkInfo.State.RUNNING) {
                                Text(
                                    statusMessage ?: "Indexing...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberGreen
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = CyberDark
                    ),
                    actions = {
                        if (workInfo?.state == WorkInfo.State.RUNNING) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 16.dp).size(20.dp),
                                strokeWidth = 2.dp,
                                color = CyberGreen
                            )
                        }
                    }
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
                        text = { Text("COLLECTIONS") },
                        icon = { Icon(Icons.Rounded.Collections, null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("GALLERY") },
                        icon = { Icon(Icons.Rounded.Search, null) }
                    )
                }
                if (workInfo?.state == WorkInfo.State.RUNNING) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(1.dp),
                        color = CyberGreen,
                        trackColor = Color.Transparent
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> {
                    FloatingActionButton(
                        onClick = { showCreateCollectionDialog = true },
                        containerColor = CyberBlue,
                        contentColor = CyberDark
                    ) {
                        Icon(Icons.Rounded.Add, "Create Collection")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        containerColor = CyberBlue,
                        contentColor = CyberDark
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, "Import Image")
                    }
                }
            }
        },
        containerColor = CyberDark
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardContent(viewModel)
                1 -> CollectionsContent(viewModel) {
                    selectedTab = 2
                }
                2 -> GalleryContent(viewModel, navigator)
            }
        }
    }

    if (showCreateCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            title = { Text("CREATE COLLECTION", color = CyberBlue) },
            text = {
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCollectionName.isNotBlank()) {
                            viewModel.createCollection(newCollectionName)
                            newCollectionName = ""
                            showCreateCollectionDialog = false
                        }
                    }
                ) {
                    Text("CREATE", color = CyberBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCollectionDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}

@Composable
fun DashboardContent(viewModel: MediaIndexerViewModel) {
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val indexedCount by viewModel.indexedCount.collectAsStateWithLifecycle()
    val workInfo by viewModel.workInfo.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (statusMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (workInfo?.state == WorkInfo.State.FAILED) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        CyberBlue.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (workInfo?.state == WorkInfo.State.FAILED) {
                        Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error)
                    } else if (workInfo?.state == WorkInfo.State.RUNNING) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CyberBlue)
                    }
                    Text(
                        text = statusMessage!!,
                        color = if (workInfo?.state == WorkInfo.State.FAILED) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

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
                        "INDEXING",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberBlue
                    )
                    Text(
                        "Analyze images with AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { viewModel.startIndexing() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, tint = CyberBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("INDEX NOW", color = CyberBlue)
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

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "POWERED BY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                "GEMINI NANO (AICORE)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = CyberBlue
            )
        }
    }
}

@Composable
fun CollectionsContent(
    viewModel: MediaIndexerViewModel,
    onCollectionClick: () -> Unit
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()

    if (collections.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No collections yet. Index some images to see them here.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(collections) { collection ->
                CollectionCard(collection, viewModel) {
                    viewModel.selectCollection(collection.name)
                    onCollectionClick()
                }
            }
        }
    }
}

@Composable
fun CollectionCard(
    collection: com.example.intellidash.data.CollectionInfo,
    viewModel: MediaIndexerViewModel,
    onClick: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(collection.coverPath) {
        collection.coverPath?.let { path ->
            withContext(Dispatchers.IO) {
                bitmap = viewModel.loadBitmap(path)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.DarkGray)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Rounded.Folder,
                        null,
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
                        tint = CyberBlue.copy(alpha = 0.5f)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (collection.count == 1) "1 item" else "${collection.count} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberBlue
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
    val selectedCollection by viewModel.selectedCollection.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.onSearchQueryChanged(it)
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberBlue,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                    
                    if (selectedCollection != null) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.selectCollection(null) },
                            label = { Text(selectedCollection!!) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.Close,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = CyberBlue.copy(alpha = 0.2f),
                                selectedLabelColor = CyberBlue,
                                selectedTrailingIconColor = CyberBlue
                            ),
                            border = null
                        )
                    }

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
    val userCollections by viewModel.userCollections.collectAsStateWithLifecycle()
    var showCollectionMenu by remember { mutableStateOf(false) }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "COLLECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGreen
                )
                Box {
                    TextButton(onClick = { showCollectionMenu = true }) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp), tint = CyberBlue)
                        Spacer(Modifier.width(8.dp))
                        Text(item.collectionName ?: "None", color = Color.White)
                    }
                    DropdownMenu(
                        expanded = showCollectionMenu,
                        onDismissRequest = { showCollectionMenu = false },
                        modifier = Modifier.background(Color(0xFF2A2A2A))
                    ) {
                        userCollections.forEach { collection ->
                            DropdownMenuItem(
                                text = { Text(collection.name, color = Color.White) },
                                onClick = {
                                    viewModel.addMediaToCollection(item.id, collection.name)
                                    showCollectionMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

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
