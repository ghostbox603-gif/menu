package com.lagfix.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagfix.app.OverlayState

private val Bg = Color(0xFF15181E)
private val Surface = Color(0xFF1E232B)
private val Accent = Color(0xFF4ADE80)
private val TextMain = Color(0xFFE6E9EF)
private val TextDim = Color(0xFF8A93A3)

@Composable
fun OverlayPanel(
    state: OverlayState,
    onDrag: (Float, Float) -> Unit,
    onCleanup: () -> Unit,
    onPerformanceToggle: (Boolean) -> Unit,
    onGameToggle: (Boolean) -> Unit,
    onFpsToggle: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    val animMs = if (state.performanceMode) 0 else 220

    Column(horizontalAlignment = Alignment.Start) {

        Avatar(
            state = state,
            onDrag = onDrag,
            onTap = { state.expanded = !state.expanded }
        )

        AnimatedVisibility(
            visible = state.expanded,
            enter = if (animMs == 0) fadeIn(snap()) else
                fadeIn(tween(animMs)) + expandVertically(tween(animMs)),
            exit = if (animMs == 0) fadeOut(snap()) else
                fadeOut(tween(animMs)) + shrinkVertically(tween(animMs))
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Bg)
                    .border(1.dp, Color(0xFF2A303A), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Header(onClose)

                CleanupCard(state, onCleanup)

                ToggleRow(
                    title = "Performance Mode",
                    subtitle = "Giảm animation của LagFix",
                    checked = state.performanceMode,
                    enabled = !state.gameMode,
                    onChange = onPerformanceToggle
                )

                ToggleRow(
                    title = "Game Mode",
                    subtitle = "Thu gọn & giảm hoạt động nền",
                    checked = state.gameMode,
                    onChange = onGameToggle
                )

                ToggleRow(
                    title = "Hiện FPS",
                    subtitle = if (state.showFps) "${state.fps} FPS (đo của overlay)"
                    else "Tắt để tiết kiệm pin",
                    checked = state.showFps,
                    enabled = !state.performanceMode,
                    onChange = onFpsToggle
                )
            }
        }
    }
}

@Composable
private fun Avatar(
    state: OverlayState,
    onDrag: (Float, Float) -> Unit,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    if (state.gameMode) listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                    else listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                )
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state.showFps && !state.expanded) "${state.fps}" else "LF",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun Header(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("LagFix", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Tắt",
            color = TextDim,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClose)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CleanupCard(state: OverlayState, onCleanup: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("RAM Cleanup", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (state.cleaning) Color(0xFF2F3642) else Accent)
                .clickable(enabled = !state.cleaning, onClick = onCleanup),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.cleaning) "Đang xử lý..." else "Chạy dọn dẹp",
                color = if (state.cleaning) TextDim else Color(0xFF06210F),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Text(
            text = state.cleanupResult
                ?: "Giải phóng bộ nhớ của LagFix và đo RAM trống thực tế.",
            color = TextDim,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) TextMain else TextDim,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextDim, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Accent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2F3642),
                uncheckedThumbColor = TextDim,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
