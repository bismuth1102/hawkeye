package com.skillion.hawkeye.video;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.R;
import com.skillion.hawkeye.CustomAdapter;

/***
 * Used for generating list view - contains a time stamp, image and any particular
 * information that needs to be added
 * For now, no extra info(particular info) has been created - no argument for that
 */
public class UploadVideoActivity extends Activity {
    ListView simpleList;
    String timeStampList[] = {"TS1", "TS2", "TS3", "TS4"};
    int imageVideo[] = {R.drawable.upload,R.drawable.upload,R.drawable.upload,R.drawable.upload};
private Button btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        simpleList = (ListView) findViewById(R.id.simpleListView);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), timeStampList, imageVideo);
        simpleList.setAdapter(customAdapter);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}