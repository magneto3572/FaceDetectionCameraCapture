package com.example.cameraxfacedetection.face_detection

import android.graphics.*
import android.util.Log
import com.example.cameraxfacedetection.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas?) {
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        canvas?.drawRect(rect, boxPaint)

        val canvasW = canvas?.width
        val canvasH = canvas?.height
        val centerOfCanvas = Point(canvasW!! / 2, canvasH!! / 2)
        val rectW = 900
        val rectH = 900
        val left: Int = centerOfCanvas.x - rectW / 2
        val top: Int = centerOfCanvas.y - rectH + 120
        val right: Int = centerOfCanvas.x + rectW / 2
        val bottom: Int = centerOfCanvas.y + rectH / 14
        val rect2 = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        canvas.drawRect(rect2, boxPaint)

        if (rect2.contains(rect)){
            Log.d("LogTag", "Inside the rectangle")
        }else{
            Log.d("LogTag", "outside the rectangle")
        }
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}