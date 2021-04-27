//
// Created by 56291 on 2021/1/10.
//

#include <iomanip> // to format image names using setw() and setfill()
#include <set>
#include "android/log.h"
#include "SORT.h"
#include "Hungarian.h"
#include "KalmanTracker.h"

#include "opencv2/video/tracking.hpp"
#include "opencv2/highgui/highgui.hpp"

using namespace std;
using namespace cv;


double SORT::GetIOU(Rect_<float> bb_test, Rect_<float> bb_gt)
{
    float in = (bb_test & bb_gt).area();
    float un = bb_test.area() + bb_gt.area() - in;

    if (un < DBL_EPSILON)
        return 0;

    return (double)(in / un);
}

vector<BoundingBox> SORT::handle(vector<BoundingBox> boxes){

    for (auto it = boxes.begin(); it != boxes.end(); it++){
        (*it).box = Rect_<float>(Point_<float>((*it).left, (*it).top), Point_<float>((*it).right, (*it).bottom));
    }
//    __android_log_print(ANDROID_LOG_INFO, "sort", "%s %i", "boxes size", boxes.size());



    // variables used in the for-loop
    vector<BoundingBox> result;
    vector<Rect_<float>> predictedBoxes;
    vector<vector<double>> iouMatrix;
    vector<int> assignment;
    set<int> unmatchedDetections;
    set<int> unmatchedTrajectories;
    set<int> allItems;
    set<int> matchedItems;
    vector<cv::Point> matchedPairs;
    unsigned int trkNum = 0;
    unsigned int detNum = 0;


    ///////////////////////////////////////
    // 1. If the frame count is not continuous, fresh trackers
    if (last_frame_num+1 != boxes[0].frame_num && last_frame_num != boxes[0].frame_num){
        trackers.clear();
    }
    last_frame_num = boxes[0].frame_num;


    ///////////////////////////////////////
    // 2. Initialize
    if (trackers.size() == 0) // the first frame met
    {
        // initialize kalman trackers using first detections.
        for (unsigned int i = 0; i < boxes.size(); i++) {
            KalmanTracker trk = KalmanTracker(boxes[i].box, boxes[i].id_from_OD);
            trackers.push_back(trk);

            string text = to_string(last_frame_num) + " " + to_string(trk.m_id) + " new!";
//            __android_log_print(ANDROID_LOG_INFO, "trk", "%s", text.c_str());

            BoundingBox box = BoundingBox(trk.id_from_OD, dontknow, trk.m_id, 0);
            result.push_back(box);
        }
        return result;
    }


    ///////////////////////////////////////
    // 3.1. get predicted locations from existing trackers.
    predictedBoxes.clear();

    for (auto it = trackers.begin(); it != trackers.end();)
    {
        Rect_<float> pBox = (*it).predict();
        if (pBox.x >= 0 && pBox.y >= 0)
        {
            predictedBoxes.push_back(pBox);
            it++;
        }
        else
        {
            it = trackers.erase(it);
        }
    }

    ///////////////////////////////////////
    // 3.2. associate detections to tracked object (both represented as bounding boxes)
    trkNum = predictedBoxes.size();
    detNum = boxes.size();

    iouMatrix.clear();
    iouMatrix.resize(trkNum, vector<double>(detNum, 0));

    for (unsigned int i = 0; i < trkNum; i++) // compute iou matrix as a distance matrix
    {
        for (unsigned int j = 0; j < detNum; j++)
        {
            // use 1-iou because the hungarian algorithm computes a minimum-cost assignment.
            iouMatrix[i][j] = 1 - GetIOU(predictedBoxes[i], boxes[j].box);
        }
    }

    // solve the assignment problem using hungarian algorithm.
    // the resulting assignment is [track(prediction) : detection], with len=preNum
    HungarianAlgorithm HungAlgo;
    assignment.clear();
    HungAlgo.Solve(iouMatrix, assignment);

    // find matches, unmatched_detections and unmatched_predictions
    unmatchedTrajectories.clear();
    unmatchedDetections.clear();
    allItems.clear();
    matchedItems.clear();

    if (detNum > trkNum) //	there are unmatched detections
    {
        for (unsigned int n = 0; n < detNum; n++)
            allItems.insert(n);

        for (unsigned int i = 0; i < trkNum; ++i)
            matchedItems.insert(assignment[i]);

        set_difference(allItems.begin(), allItems.end(),
                       matchedItems.begin(), matchedItems.end(),
                       insert_iterator<set<int>>(unmatchedDetections, unmatchedDetections.begin()));
    }
    else if (detNum < trkNum) // there are unmatched trajectory/predictions
    {
        for (unsigned int i = 0; i < trkNum; ++i)
            if (assignment[i] == -1) // unassigned label will be set as -1 in the assignment algorithm
                unmatchedTrajectories.insert(i);
    }
    else ;

    // filter out matched with low IOU
    matchedPairs.clear();
    for (unsigned int i = 0; i < trkNum; ++i)
    {
        if (assignment[i] == -1) // pass over invalid values
            continue;
        if (1 - iouMatrix[i][assignment[i]] < iouThreshold)
        {
            unmatchedTrajectories.insert(i);
            unmatchedDetections.insert(assignment[i]);
        }
        else
            matchedPairs.push_back(cv::Point(i, assignment[i]));
    }

    ///////////////////////////////////////
    // 3.3. updating trackers
    // update matched trackers with assigned detections.
    // each prediction is corresponding to a tracker
    int detIdx, trkIdx;
    for (unsigned int i = 0; i < matchedPairs.size(); i++)
    {
        trkIdx = matchedPairs[i].x;
        detIdx = matchedPairs[i].y;
        trackers[trkIdx].update(boxes[detIdx].box, boxes[detIdx].id_from_OD);
    }

    // create and initialise new trackers for unmatched detections
    for (auto umd : unmatchedDetections)
    {
        KalmanTracker tracker = KalmanTracker(boxes[umd].box, boxes[umd].id_from_OD);
        trackers.push_back(tracker);
    }
//    __android_log_print(ANDROID_LOG_INFO, "sort", "%s %i", "trackers size", trackers.size());

    // get trackers' output
    for (auto it = trackers.begin(); it != trackers.end();)
    {

//        if (((*it).m_time_since_update < 1) &&
//            ((*it).m_hit_streak >= min_hits || last_frame_num <= min_hits))
        if ((*it).m_time_since_update < 1)
        {
//            string text = to_string(last_frame_num) + " " + to_string((*it).m_id) + " " + to_string((int)(*it).area) + " " + to_string((*it).score);
//            __android_log_print(ANDROID_LOG_INFO, "trk", "%s", text.c_str());

            BoundingBox box = BoundingBox((*it).id_from_OD, (*it).get_moving_state(), (*it).m_id, (*it).get_speed());
            result.push_back(box);

        }
        it++;

        // remove dead track
        if (it != trackers.end() && (*it).m_time_since_update > max_age) {
            it = trackers.erase(it);
        }

    }

    return result;

}