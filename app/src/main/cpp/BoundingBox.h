//
// Created by 56291 on 2021/1/10.
//

#ifndef HAWKEYE_BOUNDINGBOX_H
#define HAWKEYE_BOUNDINGBOX_H
#include <opencv2/core/types.hpp>

class BoundingBox {

public:
    int frame_num = -1;
    int id_from_OD = -1;
    float left = 0;
    float top = 0;
    float right = 0;
    float bottom = 0;
    int color = -1;
    int score = 0;
    int id = -1;
    float speed;
    cv::Rect_<float> box;

    BoundingBox(int id_from_OD, int frame_num, float left, float top, float right, float bottom) {
        this->id_from_OD = id_from_OD;
        this->frame_num = frame_num;
        this->left = left;
        this->top = top;
        this->right = right;
        this->bottom = bottom;
    }

    BoundingBox(int id_from_OD, int color, int id, float speed) {
        this->id_from_OD = id_from_OD;
        this->color = color;
        this->id = id;
        this->speed = speed;
    }

};


#endif //HAWKEYE_BOUNDINGBOX_H
