package com.vnteam.objectdetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.vnteam.objectdetector.GraphicOverlay.Graphic

class InferenceInfoGraphic(
    private val overlay: GraphicOverlay,
    private val frameLatency: Long,
    private val detectorLatency: Long,
    private val framesPerSecond: Int?
) : Graphic(overlay) {
    private val textPaint: Paint = Paint()
    private var showLatencyInfo = true

    init {
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
        postInvalidate()
    }

    constructor(overlay: GraphicOverlay) : this(overlay, 0, 0, null) {
        showLatencyInfo = false
    }

    @Synchronized
    override fun draw(canvas: Canvas?) {
        val x = TEXT_SIZE * 0.5f
        val y = TEXT_SIZE * 1.5f
        canvas?.drawText(
            "InputImage size: " + overlay.imageHeight + "x" + overlay.imageWidth,
            x,
            y,
            textPaint
        )
        if (!showLatencyInfo) {
            return
        }
        if (framesPerSecond != null) {
            canvas?.drawText(
                "FPS: $framesPerSecond, Frame latency: $frameLatency ms",
                x,
                y + TEXT_SIZE,
                textPaint
            )
        } else {
            canvas?.drawText("Frame latency: $frameLatency ms", x, y + TEXT_SIZE, textPaint)
        }
        canvas?.drawText(
            "Detector latency: $detectorLatency ms", x, y + TEXT_SIZE * 2, textPaint
        )
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 60.0f
    }
}
