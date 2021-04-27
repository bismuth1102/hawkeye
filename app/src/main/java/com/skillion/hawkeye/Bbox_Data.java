package com.skillion.hawkeye;

import android.graphics.Color;
import android.graphics.RectF;

import androidx.appcompat.app.AppCompatActivity;

import com.skillion.detection.tflite.Recognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class Bbox_Data {

    static {
        System.loadLibrary("native-lib");
    }

    private static String dir = null;
    private static File boxFile = null;
    private static int frame_num = 0;
    private static int inarray_num = 6;
    private static int outarray_num = 4;

    private ArrayList<Recognition> inList = new ArrayList<>();
    private float inArray[];
    private ArrayList<Recognition> finalList = new ArrayList<>();
    private float outArray[];


    public Bbox_Data(AppCompatActivity main){
        dir = main.getExternalFilesDir(null).getAbsolutePath();
        boxFile = new File(dir+"/test.txt");
        frame_num++;
    }

    public Bbox_Data(){ }

    public void pass_data(ArrayList<Recognition> results) {

        inList = results;

        if (!inList.isEmpty()){
            inArray = new float[inList.size()*inarray_num];

            for (int i = 0; i < inList.size(); i++) {
                Recognition result = inList.get(i);
                RectF location = result.getLocation();
                result.setFrame_num(frame_num);

                inArray[inarray_num*i] = result.getId_from_OD();
                inArray[inarray_num*i+1] = frame_num;
                inArray[inarray_num*i+2] = location.left;
                inArray[inarray_num*i+3] = location.top;
                inArray[inarray_num*i+4] = location.right;
                inArray[inarray_num*i+5] = location.bottom;
            }
            outArray = data2sort(inArray);
        }


    }

    public void save_data() {
        try {
            OutputStream os = new FileOutputStream(boxFile, true);
            byte[] bytesArray;

            if (!inList.isEmpty()){
                for (int i = 0; i< inList.size(); i++) {
                    Recognition result = inList.get(i);
                    RectF location = result.getLocation();

                    String item = location.left+","+location.top+","+Math.abs(location.width())+","+Math.abs(location.height());
                    item = frame_num +","+item+"\n";
                    bytesArray = item.getBytes();
                    os.write(bytesArray);
                }
            }
            else{
                String str = frame_num +",1,0,0,0,0\n";
                bytesArray = str.getBytes();
                os.write(bytesArray);
            }

            os.flush();
            os.close();
            inList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public ArrayList<Recognition> get_box(){

        if (outArray==null) return finalList;
        ArrayList<Recognition> outList = new ArrayList<>();
        int numOfOut = outArray.length/outarray_num;
        for (int i = 0; i < numOfOut; i++) {
            Recognition r = new Recognition((int)outArray[outarray_num*i], (int)outArray[outarray_num*i+1],
                    (int)outArray[outarray_num*i+2], (float)outArray[outarray_num*i+3]);
            outList.add(r);
        }
        Collections.sort(outList);

        for (Recognition outR: outList){
            for (int i=0; i<inList.size(); i++){
                Recognition inR = inList.get(i);
                if (outR.getId_from_OD() == inR.getId_from_OD()){
                    inR.setId(outR.getId());
                    inR.setSpeed(outR.getSpeed());

                    finalList.add(inR);
                    inList.remove(i);

                    break;
                }
            }
        }
        return finalList;
    }

    public native float[] data2sort(float[] array);
}
