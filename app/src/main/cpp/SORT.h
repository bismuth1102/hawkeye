//
// Created by 56291 on 2021/1/10.
//

#ifndef HAWKEYE_SORT_H
#define HAWKEYE_SORT_H

#include <opencv2/core/types.hpp>
#include "BoundingBox.h"
#include "KalmanTracker.h"

using namespace std;
using namespace cv;

class SORT{
public:
    static vector<BoundingBox> handle(vector<BoundingBox> box);

private:

    static double GetIOU(Rect_<float> bb_test, Rect_<float> bb_gt);

};

static const int max_age = 1;
static const int min_hits = 3;
static const double iouThreshold = 0.3;

static int last_frame_num = 0;
static vector<KalmanTracker> trackers;

#endif //HAWKEYE_SORT_H
