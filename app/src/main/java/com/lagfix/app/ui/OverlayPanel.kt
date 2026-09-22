package com.lagfix.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagfix.app.OverlayState

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
                fadeIn(tween(animMs)) + scaleIn(tween(animMs), initialScale = 0.85f),
            exit = if (animMs == 0) fadeOut(snap()) else
                fadeOut(tween(animMs)) + scaleOut(tween(animMs), targetScale = 0.85f)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(280.dp)
            ) {
                // Glow nhẹ phía sau card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(LagFixColors.glowGradient)
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(LagFixColors.Surface)
                        .border(1.dp, LagFixColors.Border, RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Header(state.gameMode || state.performanceMode, onClose)
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
}

@Composable
private fun Avatar(
    state: OverlayState,
    onDrag: (Float, Float) -> Unit,
    onTap: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        // Glow phía sau avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LagFixColors.glowGradient)
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (state.gameMode)
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(LagFixColors.Blue, LagFixColors.Purple)
                        )
                    else LagFixColors.buttonGradient
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
                color = Color(0xFF06210F),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun Header(active: Boolean, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("LagFix", color = LagFixColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                if (active) "Đang tối ưu" else "Bình thường",
                color = if (active) LagFixColors.Green else LagFixColors.TextDim,
                fontSize = 11.sp
            )
        }
        Text(
            "Tắt",
            color = LagFixColors.TextDim,
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
            .clip(RoundedCornerShape(16.dp))
            .background(LagFixColors.SurfaceAlt)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("RAM Cleanup", color = LagFixColors.TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (state.cleaning) androidx.compose.ui.graphics.SolidColor(Color(0xFF2F3642))
                    else LagFixColors.buttonGradient
                )
                .clickable(enabled = !state.cleaning, onClick = onCleanup),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.cleaning) "Đang xử lý..." else "Chạy dọn dẹp",
                color = if (state.cleaning) LagFixColors.TextDim else Color(0xFF06210F),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Text(
            text = state.cleanupResult
                ?: "Giải phóng bộ nhớ của LagFix và đo RAM trống thực tế.",
            color = LagFixColors.TextDim,
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
            .clip(RoundedCornerShape(16.dp))
            .background(LagFixColors.SurfaceAlt)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) LagFixColors.TextMain else LagFixColors.TextDim,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = LagFixColors.TextDim, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = LagFixColors.Green,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2F3642),
                uncheckedThumbColor = LagFixColors.TextDim,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
