package com.example.cameraxfacedetection.camerax

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaActionSound
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.cameraxfacedetection.ImagePreview
import com.example.cameraxfacedetection.face_detection.FaceContourDetectionProcessor
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay
) {

    private var preview: Preview? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null


    init {
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            Runnable {

                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .build()


                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, selectAnalyzer())
                    }


                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                setCameraConfig(cameraProvider, cameraSelector)

            }, ContextCompat.getMainExecutor(context)
        )
    }


    private fun createCameraCapture(screenAspectRatio: Rational): ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setTargetAspectRatio(screenAspectRatio.toInt())
        .build()

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(graphicOverlay)
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            val metrics = DisplayMetrics().also { finderView.display.getRealMetrics(it) }
            val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
            imageCapture = createCameraCapture(screenAspectRatio)
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )
            preview?.setSurfaceProvider(
                finderView.createSurfaceProvider()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    fun changeCameraSelector() {
        cameraProvider?.unbindAll()
        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
        graphicOverlay.toggleSelector()
        startCamera()
    }


    fun takePhoto() {
        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture?.takePicture(cameraExecutor, object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                //get bitmap from image
                try {
                    Log.d("Logbitmap", image.toString())
                    Log.d("LogTagbitmap", image.imageInfo.rotationDegrees.toString())
                    val matrix = Matrix()
                    matrix.postRotate(-90f)
                    matrix.preScale(1.0f, -1.0f);

                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
                    val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)

                    try {
                        //Write file
                        val filename = "bitmap.JPEG"
                        val stream: FileOutputStream =
                            context.openFileOutput(filename, Context.MODE_PRIVATE)
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)

                        //Cleanup
                        stream.close()
                        rotatedBitmap.recycle()



                        //Pop intent
                        val i = Intent(context, ImagePreview::class.java)
                        i.putExtra("image", filename)
                        context.startActivity(i)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }




                    //val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)


                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(TAG, "onCaptureSuccess: Error : " + e.message)
                }

                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
        })

    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    companion object {
        private const val TAG = "CameraXBasic"
    }

}