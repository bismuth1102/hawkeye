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
import android.util.Size;

import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.skillion.detection.env.ImageUtils;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public abstract class DetectorActivity_External extends DetectorActivity {
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    public DetectorActivity_External(CameraActivity mCameraActivity){
        setCamera(mCameraActivity);
    }

    private AbstractUVCCameraHandler.OnPreViewResultListener mPreviewFrameListener = data -> {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[640 * 480];
            Size size = new Size(640, 480);
            onPreviewSizeChosen(size, 0);
        }

        isProcessingFrame = true;
        yuvBytes[0] = data;
        imageConverter =
                () -> ImageUtils.convertYUV420SPToARGB8888(data, previewWidth, previewHeight, rgbBytes);

        postInferenceCallback =
                () -> {
                    // camera.addCallbackBuffer(data);
                    isProcessingFrame = false;
                };

        processImage();
    };

    public AbstractUVCCameraHandler.OnPreViewResultListener getPreviewFrameListener(){
        return mPreviewFrameListener;
    }

}
