package com.vnteam.objectdetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.vnteam.objectdetector.ui.theme.ObjectDetectorTheme


class MainActivity : ComponentActivity() {

    private var cameraId: String? = null
    private var surface: Surface? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, CameraXSourceDemoActivity::class.java))
        return
        cameraId = getBackCameraId()
        setContent {
            ObjectDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            SurfaceView(this).apply {
                                surface = this.holder.surface
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        openCamera()
                                    }

                                    override fun surfaceChanged(
                                        holder: SurfaceHolder,
                                        format: Int,
                                        width: Int,
                                        height: Int,
                                    ) = Unit

                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        closeCamera()
                                    }
                                })
                            }
                        }
                    )
                }
            }
        }
    }

    private fun getBackCameraId(): String? {
        var backCameraId: String? = null
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager

        return try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId
                    break
                }
            }
            backCameraId
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            null
        }
    }

    private fun openCamera() {
        if (cameraId == null) {
            Log.e("CameraTAG", "MainActivity openCamera cameraId == null")
            return
        }
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.openCamera(cameraId.toString(), object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        Log.e("CameraTAG", "MainActivity openCamera onOpened cameraId $cameraId")
                        cameraDevice = camera
                        createCaptureSession()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.e("CameraTAG", "MainActivity openCamera onDisconnected")
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e("CameraTAG", "MainActivity openCamera onError")
                        camera.close()
                    }
                }, null)
            } else {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        cameraCaptureSession?.close()
        cameraCaptureSession = null

        cameraDevice?.close()
        cameraDevice = null
    }

    private fun createCaptureSession() {
        Log.e("CameraTAG", "MainActivity createCaptureSession surface $surface")
        val surface1 = surface ?: return
        try {
            val sessionConfig = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                listOf(OutputConfiguration(surface1)),
                ContextCompat.getMainExecutor(this),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        Log.e(
                            "CameraTAG",
                            "MainActivity createCaptureSession onConfigured cameraId $cameraId"
                        )
                        val captureRequestBuilder =
                            cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                ?.apply {
                                    addTarget(surface1)
                                }
                        captureRequestBuilder?.let {
                            session.setRepeatingRequest(
                                it.build(),
                                null,
                                null
                            )
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(
                            "CameraTAG",
                            "MainActivity createCaptureSession onConfigureFailed cameraId $cameraId"
                        )
                    }
                })
            cameraDevice?.createCaptureSession(sessionConfig)
        } catch (e: CameraAccessException) {
            Log.e("CameraTAG", "MainActivity createCaptureSession error ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    private fun startDeviceObjectDetector() {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build()
        val objectDetector = ObjectDetection.getClient(options)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, @JvmSuppressWildcards Boolean>? ->
            if (isGranted?.values?.contains(false) == true) {
                Log.e(
                    "CameraTAG",
                    "MainActivity requestPermissionLauncher isGranted?.values?.contains(false) == true"
                )
            } else {
                openCamera()
            }
        }

    override fun onStop() {
        super.onStop()
        closeCamera()
    }
}