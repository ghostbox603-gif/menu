package com.lagfix.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagfix.app.ui.LagFixColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Session {
    private const val PREFS = "lagfix_session"
    private const val KEY_LOGGED_IN = "logged_in"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOGGED_IN, value)
            .apply()
    }

    fun checkCredentials(username: String, password: String): Boolean =
        username == "admin" && password == "123456"
}

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Session.isLoggedIn(this)) {
            goToMain()
            return
        }

        setContent {
            LoginScreen(onLoginSuccess = {
                Session.setLoggedIn(this, true)
                goToMain()
            })
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
private fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var leaving by remember { mutableStateOf(false) }

    // Nền gradient chuyển động rất nhẹ, chu kỳ dài để không tốn CPU
    val infinite = rememberInfiniteTransition(label = "bg")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val shakeX = remember { Animatable(0f) }
    val scope = rememberCoroutineScopeCompat()

    var logoVisible by remember { mutableStateOf(false) }
    var fieldsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        logoVisible = true
        delay(150)
        fieldsVisible = true
    }

    val bg = Brush.linearGradient(
        colors = listOf(
            LagFixColors.BgDeep,
            Color(0xFF10131C).copy(alpha = 0.9f + 0.1f * shift),
            LagFixColors.BgDeep
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeX.value.dp),
            horizontalAlignment = Alignment.Start
        ) {

            AnimatedVisibility(visible = logoVisible, enter = fadeIn(tween(500))) {
                Column {
                    Text(
                        "LagFix",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = LagFixColors.brandGradient,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Optimize your gaming experience",
                        color = LagFixColors.TextDim,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(
                visible = fieldsVisible,
                enter = fadeIn(tween(450)) + slideInVertically(
                    animationSpec = tween(450),
                    initialOffsetY = { it / 4 }
                )
            ) {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword)
                            VisualTransformation.None else PasswordVisualTransformation('•'),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Text(
                                text = if (showPassword) "Ẩn" else "Hiện",
                                color = LagFixColors.TextDim,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { showPassword = !showPassword }
                                    .padding(12.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors()
                    )

                    if (errorMessage != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(errorMessage ?: "", color = LagFixColors.Danger, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(26.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(LagFixColors.buttonGradient)
                            .clickable {
                                if (Session.checkCredentials(username, password)) {
                                    leaving = true
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Sai tài khoản hoặc mật khẩu"
                                    scope.launch {
                                        for (i in 0 until 3) {
                                            shakeX.animateTo(10f, tween(50))
                                            shakeX.animateTo(-10f, tween(50))
                                        }
                                        shakeX.animateTo(0f, tween(50))
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Đăng nhập",
                            color = Color(0xFF06210F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LagFixColors.TextMain,
    unfocusedTextColor = LagFixColors.TextMain,
    focusedBorderColor = LagFixColors.Green,
    unfocusedBorderColor = LagFixColors.Border,
    focusedLabelColor = LagFixColors.Green,
    unfocusedLabelColor = LagFixColors.TextDim,
    cursorColor = LagFixColors.Green,
    focusedContainerColor = LagFixColors.Surface,
    unfocusedContainerColor = LagFixColors.Surface
)
