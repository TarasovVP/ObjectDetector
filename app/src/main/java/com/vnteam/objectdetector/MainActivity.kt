package com.vnteam.objectdetector

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.camera.CameraSourceConfig
import com.google.mlkit.vision.camera.CameraXSource
import com.google.mlkit.vision.camera.DetectionTaskCallback
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions

class MainActivity : ComponentActivity() {

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing: Int = CameraSourceConfig.CAMERA_FACING_BACK
    private var cameraXSource: CameraXSource? = null
    private var customObjectDetectorOptions: CustomObjectDetectorOptions? = null
    private var targetResolution: Size? = null

    private val isPortraitMode: Boolean
        get() = applicationContext.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    private val localModel: LocalModel
        get() = LocalModel.Builder().setAssetFilePath("custom_models/bike_car_model.tflite").build()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, @JvmSuppressWildcards Boolean>? ->
            if (isGranted?.values?.contains(false) == true) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            } else {
                startCameraXSource()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.preview_view)
        graphicOverlay = findViewById(R.id.graphic_overlay)
    }

    override fun onResume() {
        super.onResume()
        if (cameraXSource != null &&
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel).equals(customObjectDetectorOptions) &&
            PreferenceUtils.getCameraXTargetResolution(applicationContext, lensFacing) != null &&
            PreferenceUtils.getCameraXTargetResolution(applicationContext, lensFacing) == targetResolution) {
            startCameraXSource()
        } else {
            createThenStartCameraXSource()
        }
    }

    private fun startCameraXSource() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraXSource?.start()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun createThenStartCameraXSource() {
        cameraXSource?.close()
        customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(applicationContext, localModel)
        val objectDetector = customObjectDetectorOptions?.let { ObjectDetection.getClient(it) }
        val detectionTaskCallback: DetectionTaskCallback<List<DetectedObject>> =
            DetectionTaskCallback<List<DetectedObject>> { detectionTask ->
                detectionTask
                    .addOnSuccessListener { results -> onDetectionTaskSuccess(results) }
                    .addOnFailureListener { e -> onDetectionTaskFailure(e) }
            }

        targetResolution =
            PreferenceUtils.getCameraXTargetResolution(applicationContext, lensFacing)
        objectDetector?.let { CameraSourceConfig.Builder(applicationContext, it, detectionTaskCallback).setFacing(lensFacing) }?.apply {
            targetResolution?.let { setRequestedPreviewSize(it.width, it.height) }
            cameraXSource = previewView?.let { CameraXSource(build(), it) }
        }
        needUpdateGraphicOverlayImageSourceInfo = true
        startCameraXSource()
    }

    private fun onDetectionTaskSuccess(results: List<DetectedObject>) {
        graphicOverlay?.apply {
            clear()
            if (needUpdateGraphicOverlayImageSourceInfo) {
                cameraXSource?.previewSize.takeIf { it != null }?.let { size ->
                    val isImageFlipped =
                        cameraXSource?.cameraFacing == CameraSourceConfig.CAMERA_FACING_FRONT
                    if (isPortraitMode) {
                        setImageSourceInfo(size.height, size.width, isImageFlipped)
                    } else {
                        setImageSourceInfo(size.width, size.height, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
            }
            results.forEach { detectedObject ->
                add(ObjectGraphic(this, detectedObject))
            }
            add(InferenceInfoGraphic(this))
            postInvalidate()
        }
    }

    private fun onDetectionTaskFailure(e: Exception) {
        graphicOverlay?.clear()
        graphicOverlay?.postInvalidate()
        val error = "Failed to process. Error: " + e.localizedMessage
        Toast.makeText(graphicOverlay?.context, error, Toast.LENGTH_SHORT).show()
    }
}