package com.example.intellidash.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intellidash.ui.theme.CyberBlue
import com.example.intellidash.ui.theme.CyberDark
import com.example.intellidash.ui.theme.CyberGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAuditor: () -> Unit,
    onNavigateToMediaIndexer: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "INTELLIDASH",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = CyberBlue
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberDark
                )
            )
        },
        containerColor = CyberDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "SELECT MODULE",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    color = Color.Gray
                )
            )

            ModuleCard(
                title = "CONTEXT AUDITOR",
                description = "AI-powered code & system security auditing.",
                icon = Icons.Rounded.Lock,
                accentColor = CyberGreen,
                onClick = onNavigateToAuditor
            )

            ModuleCard(
                title = "MEDIA INDEXER",
                description = "Offline multimodal image tagging & searchable gallery.",
                icon = Icons.Rounded.PhotoLibrary,
                accentColor = CyberBlue,
                onClick = onNavigateToMediaIndexer
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "SYSTEM STATUS: OPTIMAL",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyberGreen.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(2.dp) // Glow effect border placeholder
            .background(CyberDark, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}
