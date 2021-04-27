package com.skillion.hawkeye;

import com.skillion.hawkeye.enums.Model;

public class DetectorActivity_External_tflite extends DetectorActivity_External {

    public DetectorActivity_External_tflite(CameraActivity cameraActivity){
        super(cameraActivity);
        model = Model.tflite;
        TF_OD_API_INPUT_SIZE = 300;
        TF_OD_API_MODEL_FILE = "detect.tflite";
        TF_OD_API_LABELS_FILE = "labelmap.txt";
    }

}
