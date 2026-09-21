package com.lagfix.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.lagfix.app.ui.OverlayPanel
import kotlinx.coroutines.launch

class OverlayService : android.app.Service(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    private val state = OverlayState()
    private val fpsMeter = FpsMeter { state.fps = it }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startAsForeground()
        addOverlay()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun addOverlay() {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 320
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                OverlayPanel(
                    state = state,
                    onDrag = { dx, dy -> moveBy(dx, dy) },
                    onCleanup = { runCleanup() },
                    onPerformanceToggle = { setPerformanceMode(it) },
                    onGameToggle = { setGameMode(it) },
                    onFpsToggle = { setFps(it) },
                    onClose = { stopSelf() }
                )
            }
        }

        composeView = view
        windowManager.addView(view, params)
    }

    private fun moveBy(dx: Float, dy: Float) {
        params.x += dx.toInt()
        params.y += dy.toInt()
        composeView?.let {
            if (it.isAttachedToWindow) windowManager.updateViewLayout(it, params)
        }
    }

    private fun runCleanup() {
        if (state.cleaning) return
        state.cleaning = true
        state.cleanupResult = null
        lifecycleScope.launch {
            val report = Optimizer.cleanup(applicationContext)
            state.cleanupResult = report.toDisplayText()
            state.cleaning = false
        }
    }

    private fun setPerformanceMode(enabled: Boolean) {
        state.performanceMode = enabled
        if (enabled) setFps(false)
    }

    private fun setGameMode(enabled: Boolean) {
        state.gameMode = enabled
        if (enabled) {
            state.expanded = false
            state.performanceMode = true
            setFps(false)
            state.cleanupResult = null
        }
    }

    private fun setFps(enabled: Boolean) {
        state.showFps = enabled
        if (enabled) fpsMeter.start() else fpsMeter.stop()
    }

    private fun startAsForeground() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "LagFix", NotificationManager.IMPORTANCE_MIN)
            )
        }

        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("LagFix đang chạy")
            .setContentText("Chạm Tắt để gỡ avatar nổi")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .addAction(0, "Tắt", stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIF_ID, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onDestroy() {
        fpsMeter.stop()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        composeView?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        composeView = null
        store.clear()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.lagfix.app.STOP"
        private const val CHANNEL_ID = "lagfix_overlay"
        private const val NOTIF_ID = 1

        fun start(context: Context) {
            val i = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
    }
