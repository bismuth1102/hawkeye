package com.skillion.hawkeye;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Size;
import android.widget.Toast;

import com.R;
import com.skillion.detection.customview.OverlayView;
import com.skillion.detection.env.ImageUtils;
import com.skillion.detection.env.Logger;
import com.skillion.detection.tflite.Classifier;
import com.skillion.detection.tflite.ReadModel;
import com.skillion.detection.tflite.Recognition;
import com.skillion.detection.tflite.TFLiteObjectDetectionAPIModel;
import com.skillion.detection.tracking.MultiBoxTracker;
import com.skillion.hawkeye.enums.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DetectorActivity {

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    protected enum DetectorMode {
        TF_OD_API;
    }

    protected static final Logger LOGGER = new Logger();

    protected static Enum<Model> model = Model.tflite;
    protected static int TF_OD_API_INPUT_SIZE = 300;
    protected static final boolean TF_OD_API_IS_QUANTIZED = true;
    protected static String TF_OD_API_MODEL_FILE = "detect.tflite";
    protected static String TF_OD_API_LABELS_FILE = "labelmap.txt";
    protected static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.65f;
    protected static final boolean MAINTAIN_ASPECT = false;
    protected static final boolean SAVE_PREVIEW_BITMAP = false;
    protected static final float TEXT_SIZE_DIP = 10;

    protected long lastProcessingTimeMs;
    protected long fps;
    protected boolean computingDetection = false;
    protected long timestamp = 0;
    protected Bitmap rgbFrameBitmap = null;
    protected Bitmap croppedBitmap = null;
    protected Bitmap cropCopyBitmap = null;
    protected Matrix frameToCropTransform;
    protected Matrix cropToFrameTransform;
    protected MultiBoxTracker tracker;
    protected OverlayView trackingOverlay;
    protected Classifier detector;

    protected boolean isProcessingFrame = false;
    protected Runnable postInferenceCallback;
    protected Runnable imageConverter;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    protected byte[][] yuvBytes = new byte[3][];
    protected int[] rgbBytes = null;

    protected CameraActivity cameraActivity;

    protected TestObject o1 = new TestObject();
    protected TestObject o2 = new TestObject();


//    public DetectorActivity(){}
//    public DetectorActivity(CameraActivity mCameraActivity){
//        cameraActivity = mCameraActivity;
//    }
    public void setCamera(CameraActivity cameraActivity) {
        this.cameraActivity = cameraActivity;
    }

    protected void setPreviewWidth(int width){
        previewWidth = width;
    }

    protected void setPreviewHeight(int height){
        previewHeight = height;
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected void setUseNNAPI(final boolean isChecked) {
        cameraActivity.runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    protected void setNumThreads(final int numThreads) {
        cameraActivity.runInBackground(() -> detector.setNumThreads(numThreads));
    }

    protected void onPreviewSizeChosen(final Size size, final int rotation) {

        tracker = new MultiBoxTracker(cameraActivity);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            if (model==Model.yolo){
                detector =
                        ReadModel.create(
                                cameraActivity.getAssets(),
                                TF_OD_API_MODEL_FILE,
                                TF_OD_API_LABELS_FILE,
                                TF_OD_API_IS_QUANTIZED);
            }
            else if(model==Model.tflite){
                detector =
                        TFLiteObjectDetectionAPIModel.create(
                                cameraActivity.getAssets(),
                                TF_OD_API_MODEL_FILE,
                                TF_OD_API_LABELS_FILE,
                                TF_OD_API_INPUT_SIZE,
                                TF_OD_API_IS_QUANTIZED);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            cameraActivity.getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }

        setPreviewWidth(size.getWidth());
        setPreviewHeight(size.getHeight());

        Integer sensorOrientation = rotation - cameraActivity.getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = cameraActivity.findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(Canvas canvas) {
                        tracker.draw(canvas);
//                    if (cameraActivity.isDebug()) {
//                        tracker.drawDebug(canvas);
//                    }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    protected void processImage() {
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        // LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");
        imageConverter.run();   //fill rgbBytes
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        cameraActivity.runInBackground(
                () -> {
                    // LOGGER.i("Running detection on image " + currTimestamp);
                    final long startTime = SystemClock.uptimeMillis();
                    final List<Recognition> results = detector.recognizeImage(croppedBitmap);
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                    fps = 1000 / (lastProcessingTimeMs);

                    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                    final Canvas canvas1 = new Canvas(cropCopyBitmap);
                    final Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2.0f);

                    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    switch (MODE) {
                        case TF_OD_API:
                            minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                    }

                    final ArrayList<Recognition> mappedRecognitions = new ArrayList<>();
                    Bbox_Data data_handler = new Bbox_Data(cameraActivity);

                    for (Recognition result : results){
                        final RectF location = result.getLocation();
                        // Flip the rectangle position horizontally
//                        location.left = TF_OD_API_INPUT_SIZE - location.left;
//                        location.right = TF_OD_API_INPUT_SIZE - location.right;
                        if (result.getConfidence() >= minimumConfidence) {

                            canvas1.drawRect(location, paint);

                            cropToFrameTransform.mapRect(location);
                            result.setLocation(location);
                            mappedRecognitions.add(result);
                        }
                    }

                    data_handler.pass_data(mappedRecognitions);
                    ArrayList<Recognition> boxes = data_handler.get_box();

                    //recordThread
//                    HashMap<String, Integer> objectsSet = new HashMap<>();
//                    for (Recognition obj : boxes) {
//                        objectsSet.put(obj.getTitle(), objectsSet.getOrDefault(obj.getTitle(), 0)+1);
//                    }
//                    RecordRunnable.setMap(objectsSet);

                    tracker.newTrackResults(boxes);

//                    tracker.trackResults(mappedRecognitions, currTimestamp);
                    trackingOverlay.postInvalidate();

                    computingDetection = false;

//                    runOnUiThread(
//                            () -> {
//                                cameraActivity.showFrameInfo(previewWidth + "x" + previewHeight);
//                                cameraActivity.showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
//                                //                    showInference(lastProcessingTimeMs + "ms");
//                                cameraActivity.showFPS(fps);
//                            });
                });
    }

}