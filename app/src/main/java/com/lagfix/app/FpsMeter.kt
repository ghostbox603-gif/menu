package com.lagfix.app

import android.view.Choreographer

class FpsMeter(private val onFps: (Int) -> Unit) : Choreographer.FrameCallback {

    private var running = false
    private var frames = 0
    private var windowStartNs = 0L

    fun start() {
        if (running) return
        running = true
        frames = 0
        windowStartNs = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (windowStartNs == 0L) windowStartNs = frameTimeNanos
        frames++

        val elapsed = frameTimeNanos - windowStartNs
        if (elapsed >= 1_000_000_000L) {
            onFps((frames * 1_000_000_000L / elapsed).toInt())
            frames = 0
            windowStartNs = frameTimeNanos
        }
        Choreographer.getInstance().postFrameCallback(this)
    }
}
