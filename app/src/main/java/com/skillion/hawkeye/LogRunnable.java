package com.skillion.hawkeye;

import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.R;
import com.skillion.detection.tflite.Recognition;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogRunnable extends AppCompatActivity implements Runnable {

    public static HashMap<String, Integer> objects;
    public Context context;

    public LogRunnable(Context _context) {
        this.context = _context;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (objects!=null && !objects.isEmpty()){
                DateFormat DFormat = DateFormat.getTimeInstance();
                String date = DFormat.format(new Date());
                StringBuilder str = new StringBuilder(date + " - ");
                for (Map.Entry<String, Integer> i: objects.entrySet()) {
                    str.append(" ").append(i.getValue()).append(" ").append(i.getKey()).append(",");
                }
                str.deleteCharAt(str.length()-1);
                TextView log0Text = ((Activity)context).findViewById(R.id.log0_text);
                TextView log1Text = ((Activity)context).findViewById(R.id.log1_text);
                TextView log2Text = ((Activity)context).findViewById(R.id.log2_text);
                TextView log3Text = ((Activity)context).findViewById(R.id.log3_text);

                log3Text.setText(log2Text.getText());
                log2Text.setText(log1Text.getText());
                log1Text.setText(log0Text.getText());
                log0Text.setText(str);
            }
        }


    }

    public static void setMap(HashMap<String, Integer> _objects){
        objects = _objects;
    }

}
