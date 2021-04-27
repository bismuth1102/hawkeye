package com.skillion.hawkeye.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.R;

import androidx.appcompat.app.AppCompatActivity;

public class VideoViewActivity extends AppCompatActivity {
    //buttons
    private Button save_video;
    private Button upload_video;
    private Button cancel_video;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
            save_video = findViewById(R.id.save_video);
            upload_video = findViewById(R.id.upload_video);
            cancel_video = findViewById(R.id.cancel_video);
            save_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(VideoViewActivity.this, SaveVideoActivity.class);
                    startActivity(i);
                }
            });
            upload_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(VideoViewActivity.this, UploadVideoActivity.class);
                    startActivity(i);
                }
            });
            cancel_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

    }
}