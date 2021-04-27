/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skillion.hawkeye;

import android.app.Fragment;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Trace;
import android.util.Size;

import com.R;
import com.skillion.detection.env.ImageUtils;

import java.nio.ByteBuffer;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity_Internal extends DetectorActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback {
  private static final Size DESIRED_PREVIEW_SIZE = new Size(1080, 960);

  public DetectorActivity_Internal(CameraActivity cameraActivity){
    setCamera(cameraActivity);
//    model = Model.yolo;
//    TF_OD_API_INPUT_SIZE = 416;
//    TF_OD_API_MODEL_FILE = "yolov4-tiny-hawk.tflite";
//    TF_OD_API_LABELS_FILE = "hawk.txt";

  }

//  @Override
//  protected Resources mGetResources() {
//    return getResources();
//  }

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        System.out.println("Ready_Set_Internal_CameraActivity");
//        setCamera(new Internal_CameraActivity(this));
//    }

  /** Callback for android.hardware.Camera API */
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();

        setPreviewHeight(previewSize.height);
        setPreviewWidth(previewSize.width);
        rgbBytes = new int[previewWidth*previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;

    imageConverter =
            () -> ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);

    postInferenceCallback =
            () -> {
              camera.addCallbackBuffer(bytes);
              isProcessingFrame = false;
            };
    processImage();
  }

  private void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Image.Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      int yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
              () -> ImageUtils.convertYUV420ToARGB8888(
                      yuvBytes[0],
                      yuvBytes[1],
                      yuvBytes[2],
                      previewWidth,
                      previewHeight,
                      yRowStride,
                      uvRowStride,
                      uvPixelStride,
                      rgbBytes);

      postInferenceCallback =
              () -> {
                image.close();
                isProcessingFrame = false;
              };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  public Fragment getFragmentCameraAPI2(String cameraId){
    CameraConnectionFragment camera2Fragment =
            CameraConnectionFragment.newInstance(
                    new CameraConnectionFragment.ConnectionCallback() {
                      @Override
                      public void onPreviewSizeChosenFragment(Size size, int rotation) {
//                        setPreviewHeight(size.getHeight());
//                        setPreviewWidth(size.getWidth());
                        onPreviewSizeChosen(size, rotation);
                      }
                    },
                    this,
                    getLayoutId(),
                    getDesiredPreviewFrameSize());

    camera2Fragment.setCamera(cameraId);
    return camera2Fragment;
  }

  public LegacyCameraConnectionFragment getFragmentCameraAPI1(){
    return new LegacyCameraConnectionFragment(
            this, getLayoutId(), getDesiredPreviewFrameSize());

  }

  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

}
