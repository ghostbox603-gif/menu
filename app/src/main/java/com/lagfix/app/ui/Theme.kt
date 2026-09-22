package com.lagfix.app.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LagFixColors {
    val BgDeep = Color(0xFF0B0D12)
    val Surface = Color(0xFF171A22)
    val SurfaceAlt = Color(0xFF1E2230)
    val Border = Color(0xFF2A2F3D)

    val TextMain = Color(0xFFEDEFF5)
    val TextDim = Color(0xFF8B92A5)

    val Green = Color(0xFF3DDC84)
    val Blue = Color(0xFF3B82F6)
    val Purple = Color(0xFF8B5CF6)
    val Danger = Color(0xFFEF4444)

    val brandGradient = Brush.linearGradient(listOf(Green, Blue, Purple))
    val buttonGradient = Brush.linearGradient(listOf(Green, Blue))
    val glowGradient = Brush.radialGradient(
        listOf(Green.copy(alpha = 0.25f), Color.Transparent)
    )
}
