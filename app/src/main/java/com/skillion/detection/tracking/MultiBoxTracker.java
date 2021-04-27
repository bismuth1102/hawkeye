/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.skillion.detection.tracking;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.R;
import com.skillion.detection.env.ImageUtils;
import com.skillion.detection.env.Logger;
import com.skillion.detection.tflite.Recognition;
import com.skillion.detection.env.BorderedText;
import com.skillion.hawkeye.env.Sound;

/** A tracker that handles non-max suppression and matches existing objects to new detections. */
public class MultiBoxTracker {
  private static final float TEXT_SIZE_DIP = 18;
  private static final float MIN_SIZE = 16.0f;
  private static final int[] COLORS = {
    Color.BLUE,
    Color.RED,
    Color.GREEN,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.WHITE,
    Color.parseColor("#55FF55"),
    Color.parseColor("#FFA500"),
    Color.parseColor("#FF8888"),
    Color.parseColor("#AAAAFF"),
    Color.parseColor("#FFFFAA"),
    Color.parseColor("#55AAAA"),
    Color.parseColor("#AA33AA"),
    Color.parseColor("#0D0068")
  };
//  final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
  private final Logger logger = new Logger();
  private final Queue<Integer> availableColors = new LinkedList<Integer>();
  private List<Recognition> trackedObjects = new LinkedList<>();
  private final Paint boxPaint = new Paint();
  private float textSizePx = 0;
  private BorderedText borderedText = null;
  private Matrix frameToCanvasMatrix;
  private int frameWidth;
  private int frameHeight;
  private int sensorOrientation;

  public static float alertScope = 0.3f;
  public static float warningScope = 0.2f;
  public static float watchScope = 0.1f;

  private final Activity cameraActivity;
  private final Sound sound;



  public MultiBoxTracker(final Activity _cameraActivity) {

    cameraActivity = _cameraActivity;
    for (final int color : COLORS) {
      availableColors.add(color);
    }

    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(10.0f);
    boxPaint.setStrokeCap(Cap.ROUND);
    boxPaint.setStrokeJoin(Join.ROUND);
    boxPaint.setStrokeMiter(100);

    textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, cameraActivity.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);

    sound = Sound.getInstance(cameraActivity);
  }

  public synchronized void setFrameConfiguration(
      final int width, final int height, final int sensorOrientation) {
    frameWidth = width;
    frameHeight = height;
    this.sensorOrientation = sensorOrientation;
  }

  private Matrix getFrameToCanvasMatrix() {
    return frameToCanvasMatrix;
  }


  public synchronized void newTrackResults(List<Recognition> results) {
    trackedObjects.clear();
    trackedObjects = results;
  }

  public synchronized void draw(final Canvas canvas) {
    final boolean rotated = sensorOrientation % 180 == 90;
    final float multiplier =
            Math.min(
                    canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                    canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
    frameToCanvasMatrix =
            ImageUtils.getTransformationMatrix(
                    frameWidth,
                    frameHeight,
                    (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                    (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                    sensorOrientation,
                    false);

    HashMap<Integer, Integer> colorNums = new HashMap<Integer, Integer>();
    colorNums.put(Color.RED, 0);
    colorNums.put(Color.YELLOW, 0);
    colorNums.put(Color.GREEN, 0);

    for (final Recognition recognition : trackedObjects) {

      String detectedClass = recognition.getTitle();
      if (!(detectedClass.equals("person") || detectedClass.equals("car") ||
              detectedClass.equals("bicycle") || detectedClass.equals("motorbike") ||
              detectedClass.equals("bus") || detectedClass.equals("truck")
      )){
        continue;
      }


      final RectF trackedPos = new RectF(recognition.getLocation());

      getFrameToCanvasMatrix().mapRect(trackedPos);

      float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;

      //distance calculated by px_height / frameHeight
      double heightRatio = trackedPos.height()/canvas.getHeight();
      if (heightRatio>=alertScope){
          recognition.setColor(Color.RED);
          colorNums.put(Color.RED, colorNums.getOrDefault(Color.RED, 0)+1);
      }
      else if(heightRatio>=warningScope){
          recognition.setColor(Color.YELLOW);
          colorNums.put(Color.YELLOW, colorNums.getOrDefault(Color.YELLOW, 0)+1);
      }
      else if(heightRatio>=watchScope){
          recognition.setColor(Color.GREEN);
          colorNums.put(Color.GREEN, colorNums.getOrDefault(Color.GREEN, 0)+1);
      }
      boxPaint.setColor(recognition.getColor());


      canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint);

      final String labelString =
              !TextUtils.isEmpty(recognition.getTitle())
                      ? String.format("%s %d %s %.2f", recognition.getTitle(), recognition.getId(), "ratio:", heightRatio)
//                      ? String.format("%s %d %.2f %s", recognition.getTitle(), recognition.getId(), 100*recognition.getConfidence(), "%")
                      : String.format("%.2f %s", (100 * recognition.getConfidence()), "%");
      //            borderedText.drawText(canvas, trackedPos.left + cornerSize, trackedPos.top,
      // labelString);
      borderedText.drawText(
              canvas, trackedPos.left + cornerSize, trackedPos.top, labelString, boxPaint);

    }

    //set alertText and alertImg
    {
      ImageView alertImg = cameraActivity.findViewById(R.id.alert_img);
      ImageView redCircleImg = cameraActivity.findViewById(R.id.redCircle_img);
      ImageView yellowCircleImg = cameraActivity.findViewById(R.id.yellowCircle_img);
      ImageView greenCircleImg = cameraActivity.findViewById(R.id.greenCircle_img);
      TextView redCircleText = cameraActivity.findViewById(R.id.redCircle_text);
      TextView yellowCircleText = cameraActivity.findViewById(R.id.yellowCircle_text);
      TextView greenCircleText = cameraActivity.findViewById(R.id.greenCircle_text);
      TextView alertText = cameraActivity.findViewById(R.id.alert_text);

      int redNum = colorNums.get(Color.RED);
      int yellowNum = colorNums.get(Color.YELLOW);
      int greenNum = colorNums.get(Color.GREEN);

      redCircleText.setText(Integer.toString(redNum));
      yellowCircleText.setText(Integer.toString(yellowNum));
      greenCircleText.setText(Integer.toString(greenNum));


      redCircleImg.setBackgroundResource(R.drawable.circle_grey);
      yellowCircleImg.setBackgroundResource(R.drawable.circle_grey);
      greenCircleImg.setBackgroundResource(R.drawable.circle_grey);


      alertImg.getBackground().setAlpha(255);
      if (redNum>0){
        alertImg.setBackgroundResource(R.drawable.dialog_red);
        alertText.setText(R.string.alert);
        redCircleImg.setBackgroundResource(R.drawable.circle_red);
        sound.resumeSound();

      }
      else{
        sound.pauseSound();
        if (yellowNum>0){
          alertImg.setBackgroundResource(R.drawable.dialog_yellow);
          alertText.setText(R.string.warning);
          yellowCircleImg.setBackgroundResource(R.drawable.circle_yellow);
        }
        else{
          if (greenNum>0){
            alertImg.setBackgroundResource(R.drawable.dialog_green);
            alertText.setText(R.string.watch);
            greenCircleImg.setBackgroundResource(R.drawable.circle_green);
          }
          else{
            alertImg.getBackground().setAlpha(0);
            alertText.setText(R.string.standby);
          }
        }
      }


    }


  }





}
