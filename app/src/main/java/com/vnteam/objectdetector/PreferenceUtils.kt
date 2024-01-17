package com.vnteam.objectdetector

import android.content.Context
import android.preference.PreferenceManager
import android.util.Size
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import com.google.common.base.Preconditions
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions

object PreferenceUtils {
    fun getCameraXTargetResolution(context: Context, lensfacing: Int): Size? {
        Preconditions.checkArgument(
            lensfacing == CameraSelector.LENS_FACING_BACK
                    || lensfacing == CameraSelector.LENS_FACING_FRONT
        )
        val prefKey =
            if (lensfacing == CameraSelector.LENS_FACING_BACK) context.getString(R.string.pref_key_camerax_rear_camera_target_resolution) else context.getString(
                R.string.pref_key_camerax_front_camera_target_resolution
            )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            Size.parseSize(sharedPreferences.getString(prefKey, null))
        } catch (e: Exception) {
            null
        }
    }

    fun getCustomObjectDetectorOptionsForLivePreview(
        context: Context, localModel: LocalModel
    ): CustomObjectDetectorOptions {
        return getCustomObjectDetectorOptions(
            context,
            localModel,
            R.string.pref_key_live_preview_object_detector_enable_multiple_objects,
            R.string.pref_key_live_preview_object_detector_enable_classification
        )
    }

    private fun getCustomObjectDetectorOptions(
        context: Context,
        localModel: LocalModel,
        @StringRes prefKeyForMultipleObjects: Int,
        @StringRes prefKeyForClassification: Int
    ): CustomObjectDetectorOptions {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val enableMultipleObjects =
            sharedPreferences.getBoolean(context.getString(prefKeyForMultipleObjects), false)
        val enableClassification =
            sharedPreferences.getBoolean(context.getString(prefKeyForClassification), true)
        val builder = CustomObjectDetectorOptions.Builder(localModel).setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
        if (enableMultipleObjects) {
            builder.enableMultipleObjects()
        }
        if (enableClassification) {
            builder.enableClassification().setMaxPerObjectLabelCount(1)
        }
        return builder.build()
    }
}
