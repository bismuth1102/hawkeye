///////////////////////////////////////////////////////////////////////////////
// KalmanTracker.cpp: KalmanTracker Class Implementation Declaration

#include "KalmanTracker.h"
#include <iostream>
#include <algorithm>



// initialize Kalman filter
void KalmanTracker::init_kf(StateType stateMat)
{
    int stateNum = 7;
    int measureNum = 4;
    kf = KalmanFilter(stateNum, measureNum, 0);

    measurement = Mat::zeros(measureNum, 1, CV_32F);

    kf.transitionMatrix = (Mat_<float>(stateNum, stateNum) <<
                                                           1, 0, 0, 0, 1, 0, 0,
            0, 1, 0, 0, 0, 1, 0,
            0, 0, 1, 0, 0, 0, 1,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 1, 0, 0,
            0, 0, 0, 0, 0, 1, 0,
            0, 0, 0, 0, 0, 0, 1);

    setIdentity(kf.measurementMatrix);
    setIdentity(kf.processNoiseCov, Scalar::all(1e-2));
    setIdentity(kf.measurementNoiseCov, Scalar::all(1e-1));
    setIdentity(kf.errorCovPost, Scalar::all(1));

    // initialize state vector with bounding box in [cx,cy,s,r] style
    kf.statePost.at<float>(0, 0) = stateMat.x + stateMat.width / 2;
    kf.statePost.at<float>(1, 0) = stateMat.y + stateMat.height / 2;
    kf.statePost.at<float>(2, 0) = stateMat.area();
    kf.statePost.at<float>(3, 0) = stateMat.width / stateMat.height;
}


// Predict the estimated bounding fm_time_since_updatebox.
StateType KalmanTracker::predict()
{
    // predict
    Mat p = kf.predict();
    m_age += 1;

    if (m_time_since_update > 0)
        m_hit_streak = 0;
    m_time_since_update += 1;

    StateType predictBox = get_rect_xysr(p.at<float>(0, 0), p.at<float>(1, 0), p.at<float>(2, 0), p.at<float>(3, 0));

    m_history.push_back(predictBox);
    area_history.push_back(predictBox.area());
    return predictBox;
}


// Update the state vector with observed bounding box.
void KalmanTracker::update(StateType stateMat, int id_from_OD)
{
    this->id_from_OD = id_from_OD;

    lastBox = m_history.back();

    m_time_since_update = 0;
    //m_history.clear();
    m_hits += 1;
    m_hit_streak += 1;

    // measurement
    measurement.at<float>(0, 0) = stateMat.x + stateMat.width / 2;
    measurement.at<float>(1, 0) = stateMat.y + stateMat.height / 2;
    measurement.at<float>(2, 0) = stateMat.area();
    measurement.at<float>(3, 0) = stateMat.width / stateMat.height;

    // update
    kf.correct(measurement);
}


// Return the current state vector
StateType KalmanTracker::get_state()
{
    Mat s = kf.statePost;
    return get_rect_xysr(s.at<float>(0, 0), s.at<float>(1, 0), s.at<float>(2, 0), s.at<float>(3, 0));
}


// Convert bounding box from [cx,cy,s,r] to [x,y,w,h] style.
StateType KalmanTracker::get_rect_xysr(float cx, float cy, float s, float r)
{
    float w = sqrt(s * r);
    float h = s / w;
    float x = (cx - w / 2);
    float y = (cy - h / 2);

    if (x < 0 && cx > 0)
        x = 0;
    if (y < 0 && cy > 0)
        y = 0;

    return StateType(x, y, w, h);
}

int KalmanTracker::get_moving_state() {
//    update_score(); //暂且把评分阈值和m_history的个数下限等同
//    if(score>low_approach_threshold) return approaching;
//    else if(score<0-low_approach_threshold) return leaving;
//    else return dontknow;
    return trajectory();

}

void KalmanTracker::update_score() {

    if (area_history.size() <= low_approach_threshold) score=0;
    else {
        if (area_history.size() > high_approach_threshold) {
            float b = area_history[1];
            float a = area_history[0];
            if(b > a*1.05) score--;
            else if(a > b*1.05) score++;
            m_history.erase(m_history.begin());
            area_history.erase(area_history.begin());
        }
        float z = area_history[area_history.size()-1];
        float y = area_history[area_history.size()-2];
        if(z > y*1.05) score++;
        else if(y > z*1.05) score--;

    }
}

int KalmanTracker::trajectory(){
    float threshold = 10;
    if(m_history.size()<3){
        return dontknow;
    }
    StateType firstLast = m_history[area_history.size() - 1];
    StateType secondLast = m_history[area_history.size() - 2];
    StateType thirdLast = m_history[area_history.size()-3];
    int v1 = dontknow;
    int v2 = dontknow;
    vector<float> v1vec;
    vector<float> v2vec;

    float v1_dx_1 = -threshold - (firstLast.x - secondLast.x);
    float v1_dx_2 = -threshold - (firstLast.y - secondLast.y);
    float v1_dy_1 = (firstLast.x + firstLast.width) - (secondLast.x + secondLast.width) - threshold;
    float v1_dy_2 = (firstLast.y + firstLast.height) - (secondLast.y + secondLast.height) - threshold;
    v1vec.push_back(v1_dx_1);
    v1vec.push_back(v1_dx_2);
    v1vec.push_back(v1_dy_1);
    v1vec.push_back(v1_dy_2);
    v1 = three_out_of_four(v1vec);

    float v2_dx_1 = -threshold - (secondLast.x - thirdLast.x);
    float v2_dx_2 = -threshold - (secondLast.y - thirdLast.y);
    float v2_dy_1 = (secondLast.x + secondLast.width) - (thirdLast.x + thirdLast.width) - threshold;
    float v2_dy_2 = (secondLast.y + secondLast.height) - (thirdLast.y + thirdLast.height) - threshold;
    v2vec.push_back(v2_dx_1);
    v2vec.push_back(v2_dx_2);
    v2vec.push_back(v2_dy_1);
    v2vec.push_back(v2_dy_2);
    v2 = three_out_of_four(v2vec);

    if(v1==leaving && v2==leaving) return leaving;
    else if(v1==approaching && v2==approaching) return approaching;
    else return dontknow;

}

int KalmanTracker::three_out_of_four(const vector<float>& vec){
    int res = 0;
    for(float f: vec){
        if (f>=0) res++;
        else res--;
    }
    if(res>0) return approaching;
    else if(res<0) return leaving;
    else if(res==0) return dontknow;
}

float KalmanTracker::get_speed(){
    if(m_history.size()<2){
        return 0;
    }
    float p_2 = m_history[area_history.size() - 1].height;
    float p_1 = m_history[area_history.size() - 2].height;
    float speed = 0.3 * (1/p_2) * (1 / ( 1/p_2 - 1/p_1 ));
    return speed;
}


int KalmanTracker::id_count(0);