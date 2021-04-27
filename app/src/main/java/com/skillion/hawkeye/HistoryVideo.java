package com.skillion.hawkeye;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.R;

import java.io.File;

public class HistoryVideo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_video);

        VideoView mVideoView = findViewById(R.id.videoView);

//        String path = getExternalFilesDir(null).getAbsolutePath();
//        String videoPath = path+"/"+"test2.mp4";

        File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String videoPath = download.getAbsolutePath()+"/"+"test.mp4";
        Uri videoUrl=Uri.parse(videoPath);
        mVideoView.setVideoURI(videoUrl);
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);

            }
        });
    }
}
