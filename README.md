# Object Detector

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-CameraX-3DDC84?logo=android&logoColor=white)](https://developer.android.com/media/camera/camerax)
[![ML Kit](https://img.shields.io/badge/ML_Kit-Object_Detection-4285F4?logo=google&logoColor=white)](https://developers.google.com/ml-kit/vision/object-detection/android)
[![TensorFlow Lite](https://img.shields.io/badge/TensorFlow_Lite-Task_Vision-FF6F00?logo=tensorflow&logoColor=white)](https://www.tensorflow.org/lite/inference_with_metadata/task_library/object_detector)

An Android playground for real-time object detection with CameraX. The project explores two implementations: custom-model detection with ML Kit and configurable TensorFlow Lite detection with MobileNet and EfficientDet.

## Implementations

The experiments are preserved in separate branches:

| Branch | Detection stack | Models | Purpose |
| --- | --- | --- | --- |
| [`master`](https://github.com/TarasovVP/ObjectDetector/tree/master) / [`mlkit`](https://github.com/TarasovVP/ObjectDetector/tree/mlkit) | ML Kit Object Detection | Custom `bike_car_model.tflite` | Focused bike and car detection with tracking and labels |
| [`tensorflow`](https://github.com/TarasovVP/ObjectDetector/tree/tensorflow) | TensorFlow Lite Task Vision | MobileNet V1 and EfficientDet Lite 0/1/2 | Model, delegate, threshold, and performance experiments |

`master` and `mlkit` currently point to the same ML Kit implementation. The `tensorflow` branch contains the later and more configurable TensorFlow Lite experiment.

## ML Kit implementation

The default branch:

- captures a live feed from the back camera with CameraX;
- runs an on-device custom object detector;
- tracks multiple objects between frames;
- draws bounding boxes, labels, confidence values, and tracking IDs;
- adjusts overlay coordinates for image size, orientation, and camera mirroring.

The active model is stored at:

```text
app/src/main/assets/custom_models/bike_car_model.tflite
```

## TensorFlow Lite implementation

The [`tensorflow`](https://github.com/TarasovVP/ObjectDetector/tree/tensorflow) branch adds:

- MobileNet V1 and EfficientDet Lite 0, 1, and 2;
- CPU, GPU, and NNAPI delegate selection;
- configurable score threshold, maximum results, and inference thread count;
- real-time inference timing;
- CameraX frame analysis using `STRATEGY_KEEP_ONLY_LATEST`;
- a dedicated overlay for bounding boxes and confidence scores.

The model files are downloaded automatically by Gradle when they are not already available in the assets directory.

## Project structure

```text
app/src/main/
├── assets/              # Custom or downloaded TensorFlow Lite models
├── java/.../
│   ├── MainActivity.kt  # Application entry point
│   ├── GraphicOverlay.kt
│   └── ObjectGraphic.kt # Detection visualization
└── res/                 # Camera preview and application resources
```

The exact structure differs in the `tensorflow` branch, where camera analysis and detector configuration are split into `CameraFragment`, `ObjectDetectorHelper`, and `OverlayView`.

## Run the project

Requirements:

- Android Studio with JDK 17 support;
- Android SDK 34;
- an Android device running Android 9 or newer;
- a physical camera for the live detection scenario.

Clone the branch you want to explore and build the debug application:

```bash
git clone https://github.com/TarasovVP/ObjectDetector.git
cd ObjectDetector

# Optional: use the TensorFlow Lite implementation
git switch tensorflow

./gradlew :app:assembleDebug
```

Install the resulting APK or run the app from Android Studio, then grant camera permission when requested.

## Experiment status

This repository is a technical experiment rather than a production application. It intentionally keeps the ML Kit and TensorFlow Lite approaches separate so their camera pipelines, model APIs, overlays, and runtime configuration can be compared directly.
