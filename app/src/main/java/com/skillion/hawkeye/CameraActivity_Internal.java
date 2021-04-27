package com.skillion.hawkeye;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.widget.FrameLayout;

import com.R;

import java.lang.reflect.Constructor;


public class CameraActivity_Internal extends CameraActivity {
    private boolean useCamera2API;

    private DetectorActivity_Internal detector_internal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        super.onCreate(savedInstanceState);

        //activity_internal don't need this two views
        FrameLayout cameraFrameLayout = this.findViewById(R.id.cameraPreview);
        cameraFrameLayout.removeView(findViewById(R.id.camera_view));
        cameraFrameLayout.removeView(findViewById(R.id.tracking_overlay));

        detector_internal = new DetectorActivity_Internal(this);
        setDetector(detector_internal);

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }


    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }


    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distororientationted or otherwise broken previews.
                useCamera2API =
                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                                || isHardwareLevelSupported(
                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                LOGGER.i("Camera API lv2?: %s", useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            LOGGER.e(e, "Not allowed to access camera");
        }

        return null;
    }

    public void setFragment() {
        String cameraId = chooseCamera();
        Fragment fragment;
//        useCamera2API = true;
        if (useCamera2API) {
            fragment = detector_internal.getFragmentCameraAPI2(cameraId);
        } else {
            fragment = detector_internal.getFragmentCameraAPI1();
        }
        getFragmentManager().beginTransaction().replace(R.id.cameraPreview, fragment).commit();
    }

}
