package com.example.cameraxfacedetection.face_detection

import android.graphics.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cameraxfacedetection.MainActivity
import com.example.cameraxfacedetection.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face
import kotlin.properties.Delegates


class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private var boxPaint: Paint

    init {
        val selectedColor = Color.WHITE
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()

    }

    override fun draw(canvas: Canvas?) {

        val viewportMargin = 120
        val viewportCornerRadius = 100
        val eraser = Paint()
        eraser.isAntiAlias = true
        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        val width = canvas?.width?.toFloat()!! - viewportMargin
        val height = width
        val rect2 = RectF(viewportMargin.toFloat(), viewportMargin.toFloat(), width, height)

        val frame = RectF(viewportMargin.toFloat() - 2, viewportMargin.toFloat() - 2, width + 4, height + 4)
        val path = Path()
        val stroke = Paint()
        stroke.isAntiAlias = true
        stroke.strokeWidth = 10f

        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )

        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH

        if (rect2.contains(rect)){
            stroke.color = Color.GREEN
            Log.d("LogTag", "inside the rectangle")
            MainActivity.varib.isinside = true
        }else{
            stroke.color = Color.RED
            Log.d("LogTag", "outside the rectangle")
            MainActivity.varib.isinside = false
        }

        stroke.style = Paint.Style.STROKE
        path.addRoundRect(frame, viewportCornerRadius.toFloat(), viewportCornerRadius.toFloat(), Path.Direction.CW)
        canvas.drawPath(path, stroke)
        canvas.drawRoundRect(rect2, viewportCornerRadius.toFloat(), viewportCornerRadius.toFloat(), eraser)
        canvas.drawRect(rect, boxPaint)

    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }



}