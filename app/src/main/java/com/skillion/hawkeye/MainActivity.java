package com.skillion.hawkeye;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.R;

/**
 * The first page of the application
 */

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    private Button btnClickHere;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String version_num = "?.?.?";

        //draws version name from manifest file
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version_num = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView version = findViewById(R.id.version_text);
        version.setText("Version "+ version_num+"     ");



        btnClickHere = (Button)findViewById(R.id.start_btn);
        btnClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddCameraPopupActivity.class);
//                Intent i = new Intent(MainActivity.this, TestLayout.class);
                startActivity(i);
            }
        });


        int permissionCheckFile = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA);
        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED || permissionCheckFile != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
