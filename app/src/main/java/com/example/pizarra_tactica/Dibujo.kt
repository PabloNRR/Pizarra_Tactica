package com.example.pizarra_tactica

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.text.color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Dibujo(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var currentPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath: Path? = null
    private var eraseMode = false
    private var eraseType = EraseType.NORMAL
    private var drawingArrow = false
    private var startPoint: PointF? = null
    private var endPoint: PointF? = null
    private val erasePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 5f // Reduced erase stroke width
    }
    private var isDrawingEnabled = false
    private var drawingBounds: RectF? = null

    init {
        // Make the view transparent
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
        isFocusable = true
    }

    enum class EraseType {
        NORMAL, FULL, ALL
    }

    enum class DrawMode {
        FREE, ARROW, ERASE
    }

    private var currentDrawMode: DrawMode = DrawMode.FREE

    fun setDrawMode(mode: DrawMode) {
        currentDrawMode = mode
        when (mode) {
            DrawMode.FREE -> {
                drawingArrow = false
                eraseMode = false
                isDrawingEnabled = true
            }
            DrawMode.ARROW -> {
                drawingArrow = true
                eraseMode = false
                isDrawingEnabled = true
            }
            DrawMode.ERASE -> {
                drawingArrow = false
                eraseMode = true
                eraseType = EraseType.NORMAL
                isDrawingEnabled = true
            }
        }
    }

    fun setEraseMode(mode: EraseType) {
        eraseType = mode
    }

    fun setStrokeWidth(width: Float) {
        currentPaint.strokeWidth = width
    }

    fun setStrokeColor(color: Int) {
        currentPaint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paths.forEach { (path, paint) -> canvas.drawPath(path, paint) }

        // Draw the current path only if not in erase mode
        if (!eraseMode && currentPath != null) {
            canvas.drawPath(currentPath!!, currentPaint)
        }


        if (drawingArrow && startPoint != null && endPoint != null) {
            drawArrow(canvas, startPoint!!, endPoint!!)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) {
            return super.dispatchTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) {
            return false
        }
        val x = event.x
        val y = event.y

        if (drawingBounds != null && !drawingBounds!!.contains(x, y)) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (drawingBounds != null && !drawingBounds!!.contains(x, y)) {
                    return true
                }
                currentPath = Path().apply {
                    if (!drawingArrow && !eraseMode) moveTo(x, y)
                }
                if (drawingArrow) {
                    startPoint = PointF(x, y)
                } else if (eraseMode) {
                    erasePath(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawingArrow && startPoint != null) {
                    endPoint = PointF(x, y)
                } else {
                    if (drawingBounds != null && !drawingBounds!!.contains(x, y)) {
                        return true
                    }
                    currentPath?.lineTo(x, y)
                    if (eraseMode) {
                        erasePath(x, y)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (drawingArrow && startPoint != null && endPoint != null) {
                    paths.add(Pair(createArrowPath(startPoint!!, endPoint!!), Paint(currentPaint)))
                } else if (!eraseMode && currentPath != null) {
                    paths.add(Pair(currentPath!!, Paint(currentPaint)))
                }
                currentPath = null
                startPoint = null
                endPoint = null
            }
        }
        invalidate()
        return true
    }

    private fun erasePath(x: Float, y: Float) {
        when (eraseType) {
            EraseType.NORMAL -> erasePathsInRadius(x, y, 15f) // Increased radius
            EraseType.FULL -> erasePathsAtPoint(x, y)
            EraseType.ALL -> paths.clear()
        }
    }

    private fun erasePathsInRadius(x: Float, y: Float, radius: Float) {
        paths.removeIf { (path, _) -> path.isInBounds(x, y, radius) }
    }

    private fun erasePathsAtPoint(x: Float, y: Float) {
        paths.removeIf { (path, _) -> path.isInBounds(x, y, 0f) }
    }

    private fun Path.isInBounds(x: Float, y: Float, radius: Float): Boolean {
        val pathMeasure = PathMeasure(this, false)
        val length = pathMeasure.length
        val step = 1f
        var distance = Float.MAX_VALUE
        var currentDistance: Float
        var i = 0f
        while (i <= length) {
            val point = FloatArray(2)
            pathMeasure.getPosTan(i, point, null)
            val dx = x - point[0]
            val dy = y - point[1]
            currentDistance = sqrt(dx * dx + dy * dy)
            distance = minOf(distance, currentDistance)
            if (distance <= radius) {
                return true
            }
            i += step
        }
        return false
    }

    private fun createArrowPath(start: PointF, end: PointF): Path {
        val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble()).toFloat()
        val arrowHeadLength = 30f
        val arrowHeadAngle = Math.toRadians(20.0).toFloat()

        return Path().apply {
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)

            val arrowHeadPoint1 = PointF(
                end.x - arrowHeadLength * cos(angle - arrowHeadAngle),
                end.y - arrowHeadLength * sin(angle - arrowHeadAngle)
            )
            val arrowHeadPoint2 = PointF(
                end.x - arrowHeadLength * cos(angle + arrowHeadAngle),
                end.y - arrowHeadLength * sin(angle + arrowHeadAngle)
            )

            lineTo(arrowHeadPoint1.x, arrowHeadPoint1.y)
            moveTo(end.x, end.y)
            lineTo(arrowHeadPoint2.x, arrowHeadPoint2.y)
        }
    }

    private fun drawArrow(canvas: Canvas, start: PointF, end: PointF) {
        canvas.drawPath(createArrowPath(start, end), currentPaint)
    }

    fun setDrawingEnabled(enabled: Boolean) {
        isDrawingEnabled = enabled
    }

    fun setDrawingBounds(bounds: RectF) {
        drawingBounds = bounds
    }

    fun undoLast() {
        // Si el usuario está en medio de un trazo, cancelamos el trazo actual
        if (currentPath != null || startPoint != null || endPoint != null) {
            currentPath = null
            startPoint = null
            endPoint = null
            invalidate()
            return
        }

        // Si no, borramos el último Path guardado
        if (paths.isNotEmpty()) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

}