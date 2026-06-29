package com.example.intellidash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.work.WorkManager
import com.example.intellidash.data.MediaDatabase
import com.example.intellidash.ui.home.HomeScreen
import com.example.intellidash.ui.media.MediaIndexerScreen
import com.example.intellidash.ui.media.MediaIndexerViewModel
import com.example.intellidash.ui.navigation.IntelliDashKey
import com.example.intellidash.ui.theme.IntelliDashTheme
import com.example.intellidash.ui.engineer.LocalContextEngineerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntelliDashTheme {
                val backStack = rememberNavBackStack(IntelliDashKey.Home)
                
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = { backStack.removeLastOrNull() }
                ) { key ->
                    NavEntry(key) {
                        when (key) {
                            is IntelliDashKey.Home -> {
                                HomeScreen(
                                    onNavigateToAuditor = { backStack.add(IntelliDashKey.ContextAuditor) },
                                    onNavigateToMediaIndexer = { backStack.add(IntelliDashKey.MediaIndexer) }
                                )
                            }
                            is IntelliDashKey.ContextAuditor -> {
                                LocalContextEngineerScreen()
                            }
                            is IntelliDashKey.MediaIndexer -> {
                                val context = LocalContext.current
                                val application = context.applicationContext as android.app.Application
                                val db = remember { MediaDatabase.getDatabase(context) }
                                val dao = remember { db.mediaDao() }
                                val ucDao = remember { db.userCollectionDao() }
                                val wm = remember { WorkManager.getInstance(context) }
                                val viewModel: MediaIndexerViewModel = viewModel(
                                    factory = MediaIndexerViewModel.Factory(application, dao, ucDao, wm)
                                )
                                MediaIndexerScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
