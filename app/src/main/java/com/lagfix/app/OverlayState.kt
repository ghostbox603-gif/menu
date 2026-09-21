package com.lagfix.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class OverlayState {
    var expanded by mutableStateOf(false)
    var performanceMode by mutableStateOf(false)
    var gameMode by mutableStateOf(false)
    var showFps by mutableStateOf(false)
    var fps by mutableStateOf(0)
    var cleanupResult by mutableStateOf<String?>(null)
    var cleaning by mutableStateOf(false)
}
