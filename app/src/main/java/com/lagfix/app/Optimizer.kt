package com.lagfix.app

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class CleanupReport(
    val freeBeforeMb: Long,
    val freeAfterMb: Long,
    val totalMb: Long,
    val lowMemory: Boolean
) {
    val deltaMb get() = freeAfterMb - freeBeforeMb

    fun toDisplayText(): String {
        val delta = when {
            deltaMb > 0 -> "+$deltaMb MB"
            deltaMb < 0 -> "$deltaMb MB"
            else -> "không đổi"
        }
        val note = if (lowMemory) "\nRAM đang thấp, hãy đóng bớt app." else ""
        return "Trống: $freeBeforeMb → $freeAfterMb MB ($delta)\n" +
                "Tổng: $totalMb MB$note"
    }
}

object Optimizer {

    private fun readMemory(context: Context): Triple<Long, Long, Boolean> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return Triple(info.availMem / MB, info.totalMem / MB, info.lowMemory)
    }

    suspend fun cleanup(context: Context): CleanupReport = withContext(Dispatchers.Default) {
        val (before, total, _) = readMemory(context)

        System.gc()
        Runtime.getRuntime().gc()
        delay(400)

        val (after, _, low) = readMemory(context)
        CleanupReport(before, after, total, low)
    }

    private const val MB = 1024L * 1024L
}
