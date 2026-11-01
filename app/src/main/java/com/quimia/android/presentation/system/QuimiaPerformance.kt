package com.quimia.android.presentation.system

import android.app.ActivityManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import motion.LocalQuimiaRenderingBudget
import motion.QuimiaRenderingBudget

internal fun renderingBudget(lowRam: Boolean, memoryClassMb: Int, totalMemoryBytes: Long): QuimiaRenderingBudget {
    val constrainedHeap = memoryClassMb in 1..192 && totalMemoryBytes in 1..(4L * 1024 * 1024 * 1024)
    return if (lowRam || constrainedHeap) QuimiaRenderingBudget.Compact else QuimiaRenderingBudget.Standard
}

@Composable
internal fun QuimiaPerformance(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val budget = remember(context) {
        val manager = context.getSystemService(ActivityManager::class.java)
        val memory = ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        renderingBudget(manager.isLowRamDevice, manager.memoryClass, memory.totalMem)
    }
    CompositionLocalProvider(LocalQuimiaRenderingBudget provides budget, content = content)
}
