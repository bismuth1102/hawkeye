package com.skillion.hawkeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.widget.CameraViewInterface;

import com.R;

import java.lang.reflect.Constructor;
import java.util.HashMap;


public class CameraActivity_External extends CameraActivity {

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private boolean isRequest = false;
    private boolean isPreview;
    private DetectorActivity_External detector_external;


    private CameraViewInterface.Callback mCallback = new CameraViewInterface.Callback(){
        @Override
        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
            Log.d("*** ", "created");
            // must have
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
                isPreview = true;
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
            Log.d("*** ", "changed");
        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
            Log.d("*** ", "destroyed");
            // must have
            if (isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
                isPreview = false;
            }
        }
    };


    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            Log.d("*** ", "attached device");
            // request open permission(must have)
            if (!isRequest) {
                Log.d("*** ", "isRequest = true");
                isRequest = true;
                if (mCameraHelper != null) {
                    UsbManager m = (UsbManager)getApplicationContext().getSystemService(USB_SERVICE);
                    HashMap<String, UsbDevice> usbDevices = m.getDeviceList();
                    Log.d("*** ", "USB Device List: " + usbDevices.values());
                    for (int i = 0; i < usbDevices.size(); i++) {
                        Log.d("*** ", "Requesting permissions...");
                        mCameraHelper.requestPermission(i);
                    }
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            Log.d("*** ", "detached device");
            // close camera(must have)
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                mCameraHelper.release();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            Log.d("*** ", "connected device");
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            Log.d("*** ", "disconnected device");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        super.onCreate(savedInstanceState);

//        detector_external = new DetectorActivity_External(this);
//        setDetector(detector_external);

        try {
            String detectorClass = getIntent().getStringExtra(AddCameraPopupActivity.DetectorClass);
            Class detectorActivity_External = Class.forName(detectorClass);
            Constructor constructor = detectorActivity_External.getDeclaredConstructor(CameraActivity.class);
            constructor.setAccessible(true);
            detector_external = (DetectorActivity_External) constructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDetector(detector_external);


        View mTextureView = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(mCallback);
        mCameraHelper = UVCCameraHelper.getInstance();
        if (mCameraHelper.getUSBMonitor() != null ) { mCameraHelper.release(); }
        mCameraHelper.setDefaultPreviewSize(640, 480);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        mCameraHelper.setOnPreviewFrameListener(detector_external.getPreviewFrameListener());
        if (isRequest) { mCameraHelper.requestPermission(0); }

        final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);


        if (hasPermission()) {
            Log.d("setFragment","Set Fragment");
            //setFragment();
        } else {
            requestPermission();
        }
    }


    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
        if(mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
        if(mUVCCameraView != null) {
            mUVCCameraView.onResume();
        }
    }



    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
        if(mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }

        if(mUVCCameraView != null) {
            mUVCCameraView.onPause();
        }
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }


    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        Log.d("*** ", "permission denied for device " + device);
                    }
                }
            }
        }
    };



}
