package com.example.intellidash.ui.engineer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intellidash.AuditResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalContextEngineerScreen(
    viewModel: LocalContextEngineerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentDestination by remember { mutableStateOf(EngineerTab.CODE_AUDIT) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentDestination == EngineerTab.CODE_AUDIT,
                onClick = { currentDestination = EngineerTab.CODE_AUDIT },
                icon = { Icon(Icons.Rounded.Code, null) },
                label = { Text("CODE_WORKBENCH") }
            )
            item(
                selected = currentDestination == EngineerTab.SYSTEM_AUDITOR,
                onClick = { currentDestination = EngineerTab.SYSTEM_AUDITOR },
                icon = { Icon(Icons.Rounded.Security, null) },
                label = { Text("SYSTEM_AUDITOR") }
            )
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            currentDestination.name.replace("_", " "),
                            style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 4.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            uiState.currentModelVariant?.let { variant ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(variant) },
                                    leadingIcon = { Icon(Icons.Rounded.Memory, null, Modifier.size(16.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            AiCoreStatusIndicator(uiState.aiCoreStatus)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    EngineerTab.CODE_AUDIT -> CodeAuditSection(uiState, viewModel)
                    EngineerTab.SYSTEM_AUDITOR -> SystemAuditorSection(uiState, viewModel)
                }
            }
        }
    }
}

enum class EngineerTab {
    CODE_AUDIT, SYSTEM_AUDITOR
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CodeAuditSection(
    uiState: EngineerUiState,
    viewModel: LocalContextEngineerViewModel
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val coroutineScope = rememberCoroutineScope()

    // Fix Phone UI: If history is empty and we are on a single pane, show the workbench (Detail)
    LaunchedEffect(uiState.auditHistory.isEmpty()) {
        if (uiState.auditHistory.isEmpty()) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                AuditHistoryList(
                    history = uiState.auditHistory,
                    onRecordSelected = { record ->
                        viewModel.selectAuditRecord(record)
                        coroutineScope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detailPane = {
            AnimatedPane {
                DynamicCodeWorkbench(
                    uiState = uiState,
                    onCodeChanged = viewModel::onCodeChanged,
                    onRunAudit = viewModel::runAudit,
                    onBack = {
                        coroutineScope.launch {
                            navigator.navigateBack()
                        }
                    },
                    showBackButton = navigator.canNavigateBack(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun SystemAuditorSection(
    uiState: EngineerUiState,
    viewModel: LocalContextEngineerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "SYSTEM SECURITY AUDITOR",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Analyze device security posture using root-level access and AI heuristics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = viewModel::runSystemAudit,
                    enabled = !uiState.isSystemAuditing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSystemAuditing) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("INITIATE SYSTEM AUDIT")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = uiState.systemAuditResponse,
            label = "SystemAuditResult"
        ) { response ->
            if (response != null) {
                AuditResultView(response, isSystemAudit = true)
            } else if (uiState.systemAuditError != null) {
                ErrorView(uiState.systemAuditError)
            }
        }
    }
}

@Composable
fun AiCoreStatusIndicator(status: AiCoreStatus) {
    val color = when (status) {
        AiCoreStatus.AVAILABLE -> MaterialTheme.colorScheme.secondary
        AiCoreStatus.CHECKING -> MaterialTheme.colorScheme.primary
        AiCoreStatus.DOWNLOADING -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    val label = status.name.uppercase()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 16.dp)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun AuditHistoryList(
    history: List<AuditRecord>,
    onRecordSelected: (AuditRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Text(
            "AUDIT LOG",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp)
        )
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No audits yet", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(history) { record ->
                    AuditHistoryItem(record, onClick = { onRecordSelected(record) })
                }
            }
        }
    }
}

@Composable
fun AuditHistoryItem(record: AuditRecord, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = sdf.format(Date(record.timestamp))

    ListItem(
        headlineContent = { Text(record.fileName) },
        supportingContent = { Text("Complexity: ${record.response.cyclomaticComplexity}") },
        overlineContent = { Text(time) },
        leadingContent = {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun DynamicCodeWorkbench(
    uiState: EngineerUiState,
    onCodeChanged: (String) -> Unit,
    onRunAudit: () -> Unit,
    onBack: () -> Unit = {},
    showBackButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Code Editor Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showBackButton) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        }
                        Text(
                            "SOURCE_EDITOR",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                    Button(
                        onClick = onRunAudit,
                        enabled = !uiState.isAuditing && uiState.codeSnippet.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        if (uiState.isAuditing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("RUN AUDIT")
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                TextField(
                    value = uiState.codeSnippet,
                    onValueChange = onCodeChanged,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            "Paste code snippet here...",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Results Section
        AnimatedContent(
            targetState = uiState.auditResponse,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AuditResult"
        ) { response ->
            if (response != null) {
                AuditResultView(response)
            } else if (uiState.auditError != null) {
                ErrorView(uiState.auditError)
            } else {
                EmptyResultView()
            }
        }
    }
}

@Composable
fun AuditResultView(response: AuditResponse, isSystemAudit: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isSystemAudit) Icons.Rounded.Security else Icons.Rounded.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isSystemAudit) "SECURITY POSTURE" else "AUDIT RESULTS",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                if (isSystemAudit) "RISK LEVEL" else "COMPLEXITY",
                response.cyclomaticComplexity.toString(),
                Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MetricCard("VULNERABILITIES", response.vulnerabilities.size.toString(), Modifier.weight(1f))
        }

        if (response.vulnerabilities.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("VULNERABILITIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            response.vulnerabilities.forEach { v ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 8.dp))
                    Text(v, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            if (isSystemAudit) "RECOMMENDATIONS" else "REFACTORED SOLUTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            Text(
                response.refactoredSolution,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun EmptyResultView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Waiting for audit...", color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ErrorView(error: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun LocalContextEngineerScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LocalContextEngineerScreen()
    }
}
