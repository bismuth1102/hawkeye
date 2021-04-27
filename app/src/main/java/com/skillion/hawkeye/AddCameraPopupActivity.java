package com.skillion.hawkeye;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.R;


/**
 * This is the popup menu that opens up showing the available cameras that
 * can be added/connected to the application
 */

public class AddCameraPopupActivity extends AppCompatActivity
    implements View.OnClickListener{

    public static final String DetectorClass = "com.skillion.hawkeye.AddCameraPopupActivity";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_popup);
        findViewById(R.id.internalCamera_btn).setOnClickListener(this);
        findViewById(R.id.externalCameraTflite_btn).setOnClickListener(this);
        findViewById(R.id.externalCameraYolo_btn).setOnClickListener(this);
        findViewById(R.id.flir_btn).setOnClickListener(this);
        findViewById(R.id.popupCancel_btn).setOnClickListener(this);
        findViewById(R.id.history_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()){
            case R.id.internalCamera_btn:
                i = new Intent(AddCameraPopupActivity.this, CameraActivity_Internal.class);
//                i = new Intent(AddCameraPopupActivity.this, DetectorActivity_yolo_In.class);
//                i.putExtra(DetectorClass, "com.skillion.hawkeye.DetectorActivity_Internal");
                startActivity(i);
                break;
            case R.id.externalCameraTflite_btn:
                i = new Intent(AddCameraPopupActivity.this, CameraActivity_External.class);
                i.putExtra(DetectorClass, "com.skillion.hawkeye.DetectorActivity_External_tflite");
                startActivity(i);
                break;
            case R.id.externalCameraYolo_btn:
                i = new Intent(AddCameraPopupActivity.this, CameraActivity_External.class);
                i.putExtra(DetectorClass, "com.skillion.hawkeye.DetectorActivity_External_yolo");
                startActivity(i);
                break;
            case R.id.flir_btn:
                break;
            case R.id.history_btn:
                i = new Intent(AddCameraPopupActivity.this, HistoryVideo.class);
                startActivity(i);
                break;
            case R.id.popupCancel_btn:
                finish();
                break;
        }
    }

}
