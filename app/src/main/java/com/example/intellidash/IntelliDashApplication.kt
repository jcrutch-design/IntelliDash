package com.example.intellidash

import android.app.Application
import androidx.appfunctions.service.AppFunctionConfiguration
import com.example.intellidash.worker.DiscoveryWorker
import com.example.intellidash.worker.MediaIndexerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class that provides the AppFunctionConfiguration.
 */
class IntelliDashApplication : Application(), AppFunctionConfiguration.Provider {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            AICoreClient(this@IntelliDashApplication).warmup()
        }
        
        // Start media discovery and indexing immediately on app start
        DiscoveryWorker.enqueue(this)
        MediaIndexerWorker.enqueueOneTimeWork(this)
        MediaIndexerWorker.enqueuePeriodicWork(this)
    }

    override val appFunctionConfiguration: AppFunctionConfiguration by lazy {
        val factory = LocalContextAppFunctionFactory(this)
        AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(AuditFunctions::class.java) {
                factory.createEnclosingClass(AuditFunctions::class.java)!!
            }
            .build()
    }
}
