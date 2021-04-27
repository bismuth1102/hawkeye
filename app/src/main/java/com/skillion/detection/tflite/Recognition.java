package com.skillion.detection.tflite;

import android.graphics.Color;
import android.graphics.RectF;

public class Recognition implements Comparable<Recognition> {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private int id_from_OD;

    private int detectedClass;

    /** Display name for the recognition. */
    private String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private Float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    private int color = Color.WHITE;
    private int frame_num = -1;
    private int id = -1;    //number of detected thing
    private float distance = 0;
    private float speed = 0;


    //when tensorflow create
    public Recognition(int id_from_OD, String title, Float confidence, RectF location, int detectedClass) {
        this.id_from_OD = id_from_OD;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
        this.detectedClass = detectedClass;
    }

    //when create Recognition back from SORT
    public Recognition(int id_from_OD, int color, int id, float speed) {
        this.id_from_OD = id_from_OD;
        this.color = color;
        this.id = id;
        this.speed = speed;
    }


    public int getId_from_OD() {
        return id_from_OD;
    }

    public int getDetectedClass() {
        return detectedClass;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    public RectF getLocation() {
        return location;
    }
    public void setLocation(RectF location) {
        this.location = location;
    }

    public int getColor() {return color; }
    public void setColor(int color) {this.color = color; }

    public int getFrame_num() {return frame_num; }
    public void setFrame_num(int frame_num) {this.frame_num = frame_num; }

    public int getId() {return id; }
    public void setId(int id) {this.id = id; }

    public float getDistance() {return distance; }
    public void setDistance(float distance) {this.distance = distance; }

    public float getSpeed() {return speed; }
    public void setSpeed(float speed) {this.speed = speed; }


    @Override
    public String toString() {
        String resultString = "";
        if (id_from_OD != -1) {
            resultString += "[" + id_from_OD + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }


    @Override
    public int compareTo(Recognition r) {
        if (this.getId_from_OD() < r.getId_from_OD()) return -1;
        else if (this.getId_from_OD() == r.getId_from_OD()) return 0;
        else return 1;
    }
}