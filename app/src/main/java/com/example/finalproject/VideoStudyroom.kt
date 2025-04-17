package com.example.finalproject

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class VideoStudyroom : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var faceOverlayView: FaceOverlayView
    private lateinit var counterTextView: TextView
    private lateinit var cameraExecutor: ExecutorService
    private var rotationDegrees: Int = 0 // 當前螢幕旋轉角度
    private var seconds = 0
    private var isRunning = true
    private val handler: Handler = Handler(Looper.getMainLooper()) // 初始化 handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_studyroom)

        // 初始化視圖和變數
        previewView = findViewById(R.id.previewView)
        faceOverlayView = findViewById(R.id.faceOverlay)
        counterTextView = findViewById(R.id.counterTextView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()
        val userId = "EfrzmUXqiJMZQhvomN2h2ZgOrJj2"
        val focusManager = FocusManager(userId, db)
        val startTime = focusManager.startFocus()

        Log.d("VideoStudyroom", "初始化完成，準備檢查權限")

        // 檢查是否已登入
        val currentUser = "EfrzmUXqiJMZQhvomN2h2ZgOrJj2"
        if (currentUser == null) {
            // 未登入，提示用戶並退出當前頁面
            Toast.makeText(this, "請先登入！", Toast.LENGTH_SHORT).show()
            finish() // 結束頁面
            return
        }
        Log.d("BasicStudyroom", "當前用戶 ID: $userId")

        // 啟動計時器
        startTimer()

        // 檢查權限並啟動相機
        if (allPermissionsGranted()) {
            Log.d("VideoStudyroom", "權限已授予，啟動相機和計時器")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            startCamera() // 啟動相機
            startTimer() // 開始計時
        } else {
            Log.d("VideoStudyroom", "權限未授予，請求相機權限")
            requestCameraPermission() // 請求權限
        }

        // 返回自習室選擇頁面
        findViewById<Button>(R.id.finishButton).setOnClickListener {
            // 記錄結束時間並存儲到資料庫
            stopTimer() // 停止計時器
            logTimerResult(focusManager, startTime)
            finish() // 返回上一頁
        }
    }

    // 啟動計時器 function
    private fun startTimer() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                // 格式化時間
                val time = String.format("%02d:%02d:%02d", hours, minutes, secs)
                counterTextView.text = "您已專注 $time"

                // 如果計時器正在運行，遞增秒數
                if (isRunning) {
                    seconds++
                }

                // 每隔 1 秒重複執行
                handler.postDelayed(this, 1000)
            }
        })
    }

    // 紀錄計時成果並存儲到 Firestore
    private fun logTimerResult(focusManager: FocusManager, startTime: Timestamp) {
        val endTime = com.google.firebase.Timestamp.now()
        val duration = seconds
        Log.d("TimeRecord", "最終計時結果: ${String.format("%02d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60)}")

        // 儲存到 Firestore
        focusManager.storeFocusResult(startTime, endTime, duration)
    }

    // 停止計時 function
    private fun stopTimer() {
        isRunning = false // 停止計時
        handler.removeCallbacksAndMessages(null) // 清除所有計時相關的回調
    }

    private fun startCamera() {
        Log.d("VideoStudyroom", "啟動相機")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 如果生命週期無效，不進行相機綁定
            if (!this.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.e("VideoStudyroom", "活動的生命周期已銷毀，無法啟動相機")
                return@addListener
            }

            // 配置相機預覽
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // 配置影像分析
            val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    // 配置臉部檢測選項
                    val options = FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build()

                    val detector = FaceDetection.getClient(options)

                    detector.process(image)
                        .addOnSuccessListener { faces ->
                            rotationDegrees = resources.configuration.orientation
                            faceOverlayView.setFaceData(
                                faces = faces,
                                imageWidth = mediaImage.width,
                                imageHeight = mediaImage.height,
                                overlayWidth = faceOverlayView.width,
                                overlayHeight = faceOverlayView.height,
                                isFrontCamera = true,
                                rotationDegrees = rotationDegrees
                            )
                            Log.d("VideoStudyroom", "檢測到 ${faces.size} 張臉")
                        }
                        .addOnFailureListener { e ->
                            Log.e("VideoStudyroom", "臉部檢測失敗", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
            }

            // 綁定相機到生命週期
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            Log.d("VideoStudyroom", "相機綁定完成")
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        stopTimer()
        Log.d("VideoStudyroom", "資源已釋放")
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}