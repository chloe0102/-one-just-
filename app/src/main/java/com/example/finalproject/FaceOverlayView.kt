package com.example.finalproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.finalproject.R
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

class FaceOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val landmarks = mutableListOf<Pair<Float, Float>>()
    private val boundingBoxes = mutableListOf<Rect>() // 保存人脸邊界框
    private val markerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.people2)

    private var imageWidth = 1
    private var imageHeight = 1
    private var viewWidth = 1
    private var viewHeight = 1
    private var scaleX = 1f
    private var scaleY = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    fun setFaceData(
        faces: List<Face>,
        imageWidth: Int,
        imageHeight: Int,
        overlayWidth: Int,
        overlayHeight: Int,
        isFrontCamera: Boolean,
        rotationDegrees: Int // 屏幕旋轉角度
    ) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.viewWidth = overlayWidth
        this.viewHeight = overlayHeight

        calculateScaleAndOffset()

        landmarks.clear()
        boundingBoxes.clear()

        for (face in faces) {
            boundingBoxes.add(face.boundingBox)
            face.getLandmark(FaceLandmark.NOSE_BASE)?.position?.let {
                landmarks.add(mapToOverlay(it.x, it.y, isFrontCamera))
            }
        }

        invalidate() // 重繪
    }

    private fun calculateScaleAndOffset() {
        val scaleXRaw = viewWidth.toFloat() / imageWidth
        val scaleYRaw = viewHeight.toFloat() / imageHeight
        val minScale = minOf(scaleXRaw, scaleYRaw)
        scaleX = minScale
        scaleY = minScale

        offsetX = (viewWidth - imageWidth * scaleX) / 2
        offsetY = (viewHeight - imageHeight * scaleY) / 2
    }

    private fun mapToOverlay(x: Float, y: Float, isFrontCamera: Boolean): Pair<Float, Float> {
        val mirroredX = if (isFrontCamera) imageWidth - x else x
        return Pair(mirroredX * scaleX + offsetX - 50, y * scaleY + offsetY + 100)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in landmarks.indices) {
            val (x, y) = landmarks[i]
            val boundingBox = boundingBoxes[i]

            val faceWidth = boundingBox.width() * scaleX
            val markerWidth = (faceWidth * 3.5f).toInt()
            val markerHeight = (markerBitmap.height * markerWidth / markerBitmap.width).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(markerBitmap, markerWidth, markerHeight, true)

            val left = x - markerWidth / 2
            val top = y - markerHeight / 2

            canvas.drawBitmap(scaledBitmap, left, top, null)
        }
    }
}