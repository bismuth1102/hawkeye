package com.skillion.hawkeye;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.R;
import com.skillion.detection.tracking.MultiBoxTracker;
import com.skillion.hawkeye.enums.SoundOption;
import com.skillion.hawkeye.env.Sound;
import com.skillion.hawkeye.preference.Preference_Controller;
import com.squareup.seismic.ShakeDetector;
import com.skillion.detection.env.Logger;

import java.util.concurrent.Executor;


public abstract class CameraActivity extends AppCompatActivity
        implements ShakeDetector.Listener,
        SurfaceHolder.Callback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener{


    protected static final Logger LOGGER = new Logger();
    protected static final int PERMISSIONS_REQUEST = 1;
    protected static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    protected Handler mHandler = new Handler();
    protected HandlerThread handlerThread;

    protected TextView frameValueTextView, cropValueTextView;
    protected TextView fpsTextView;
    protected SwitchCompat apiSwitchCompat;
    protected TextView threadsTextView;
    protected LinearLayout mVideoInfoLayout;
    protected LinearLayout mVideoInfoLayoutHeader;
    protected FrameLayout mCameraPreview;
    protected CameraManager cameraManager;
    protected CameraDevice mCameraDevice;
    protected MediaRecorder mMediaRecorder;
    protected Executor mExecutor;
    protected SensorManager sensorManager;
    protected ShakeDetector shakeDetector;
    protected Dialog incidentPopup;

    private DetectorActivity detectorActivity;
    private Preference_Controller preferenceController;

    private Sound sound;

    protected void setDetector(DetectorActivity detector){
        detectorActivity = detector;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sound.stopSound();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        this.mExecutor = ContextCompat.getMainExecutor(this);
        this.mMediaRecorder = new MediaRecorder();

        
        try {
            Size[] sizes = cameraManager.getCameraCharacteristics("0").get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceHolder.class);
            for (Size size : sizes) {
                Log.d("Camera", "Size: " + size);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_HARD);

        incidentPopup = new Dialog(this);

        this.preferenceController = Preference_Controller.getInstance();
        ImageButton preferenceBtn = findViewById(R.id.preferences_btn);
        preferenceBtn.setOnClickListener(v -> {
            preferenceController.showPreference(v);
        });

        sound = Sound.getInstance(this);
        sound.startSoundbyOption();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            mHandler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        super.onPause();
//        shakeDetector.stop();
    }

//    @Override
//    public void onRequestPermissionsResult(
//            final int requestCode, final String[] permissions, final int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSIONS_REQUEST) {
//            if (allPermissionsGranted(grantResults)) {
//                Log.d("setFragment","Set Fragment");
////                setFragment();
//            } else {
//                requestPermission();
//            }
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
////            startService(new Intent(this, LocationService.class)); // start location service
//        }
//    }
//
//    protected static boolean allPermissionsGranted(final int[] grantResults) {
//        for (int result : grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }

    protected boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    protected void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                        this,
                        "Camera permission is required for this demo",
                        Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);

        }
    }

    public boolean isDebug() {
        return false;
    }

    public int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    public synchronized void runInBackground(final Runnable r) {
        if (mHandler != null) {
            mHandler.post(r);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        detectorActivity.setUseNNAPI(isChecked);
        if (isChecked) apiSwitchCompat.setText("NNAPI");
        else apiSwitchCompat.setText("TFLITE");
    }


    public void showFrameInfo(String frameInfo) {
//        frameValueTextView.setText(frameInfo);
    }

    public void showCropInfo(String cropInfo) {
//        cropValueTextView.setText(cropInfo);
    }

    public void showFPS(long fps) {
//        fpsTextView.setText(String.valueOf(fps));
    }

    @Override
    public void hearShake() {
        showPopup();
    }

    private void showPopup() {
        incidentPopup.setContentView(R.layout.popup_incident_detected);
        Button yesBtn = incidentPopup.findViewById(R.id.yesBtn);
        Button noBtn = incidentPopup.findViewById(R.id.noBtn);
        yesBtn.setOnClickListener(view -> {
            // code for uploading would be called here
            incidentPopup.dismiss();
        });

        noBtn.setOnClickListener(view -> incidentPopup.dismiss());
        incidentPopup.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("Camera","Surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("Camera","Surface changed: " + i + " " + i1 + " " + i2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("Camera","Surface destroyed");
        closeCamera();
    }

    private void closeCamera() {
        try {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
