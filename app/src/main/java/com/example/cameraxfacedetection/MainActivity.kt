package com.example.cameraxfacedetection

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.cameraxfacedetection.camerax.CameraManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var previewView : PreviewView?= null

    object varib {
        var newval : MutableLiveData<Boolean>? = MutableLiveData()
        var isinside: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
            Log.d("LogNetwork", "$newValue")
            newval?.value = newValue
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createCameraManager()
        checkForPermission()
        onClicks()

        previewView = findViewById(R.id.previewView_finder)

        varib.newval?.observe(this) {
            Log.d("LogTagStr", it.toString())
            if (it){
                btnSwitch.setBackgroundColor(Color.BLUE)
                btnSwitch.setTextColor(Color.WHITE)
                btnSwitch.isEnabled = true
            }else{
                btnSwitch.setBackgroundColor(Color.GRAY)
                btnSwitch.setTextColor(Color.BLACK)
                btnSwitch.isEnabled = false
            }
        }
    }

    private fun checkForPermission() {
        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun onClicks() {
        btnSwitch.setOnClickListener {
//            cameraManager.changeCameraSelector()
            cameraManager.takePhoto()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraManager.startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            this,
            previewView_finder,
            this,
            graphicOverlay_finder
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

}