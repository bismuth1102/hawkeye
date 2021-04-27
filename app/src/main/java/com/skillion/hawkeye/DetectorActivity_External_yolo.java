package com.skillion.hawkeye;

import com.skillion.hawkeye.enums.Model;

public class DetectorActivity_External_yolo extends DetectorActivity_External {

    public DetectorActivity_External_yolo (CameraActivity cameraActivity){
        super(cameraActivity);
        model = Model.yolo;
        TF_OD_API_INPUT_SIZE = 416;
        TF_OD_API_MODEL_FILE = "yolov4-tiny-hawk.tflite";
        TF_OD_API_LABELS_FILE = "hawk.txt";
    }

}
