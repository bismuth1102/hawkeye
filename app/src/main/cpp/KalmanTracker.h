///////////////////////////////////////////////////////////////////////////////
// KalmanTracker.h: KalmanTracker Class Declaration

#ifndef KALMAN_H
#define KALMAN_H 2
#define leaving 0
#define approaching 1
#define dontknow 2


#include "opencv2/video/tracking.hpp"
#include "opencv2/highgui/highgui.hpp"

using namespace std;
using namespace cv;

#define StateType Rect_<float>


// This class represents the internel state of individual tracked objects observed as bounding box.
class KalmanTracker
{
public:
//	KalmanTracker()
//	{
//		init_kf(StateType());
//		m_time_since_update = 0;
//		m_hits = 0;
//		m_hit_streak = 0;
//		m_age = 0;
//	}
	KalmanTracker(StateType initRect, int id_from_OD)
	{
		this->id_from_OD = id_from_OD;
		init_kf(initRect);
		m_time_since_update = 0;
		m_hits = 0;
		m_hit_streak = 0;
		m_age = 0;
		id_count++;
		m_id = id_count;
	}

	~KalmanTracker()
	{
		m_history.clear();
		area_history.clear();
	}

	StateType predict();
	void update(StateType stateMat, int id_from_OD);

	StateType get_state();
	StateType get_rect_xysr(float cx, float cy, float s, float r);

	int get_moving_state();
	void update_score();
	int trajectory();
	float get_speed();

	int m_time_since_update;
	int m_hits;
	int m_hit_streak;
	int m_age;
	int id_from_OD;
	int m_id;
	StateType lastBox;

	int score = 0;
	float area = 0;

private:
	void init_kf(StateType stateMat);

	cv::KalmanFilter kf;
	cv::Mat measurement;
	std::vector<StateType> m_history;
	vector<float> area_history;

    static int id_count;

	int low_approach_threshold = 1;
	int high_approach_threshold = 5;

	int three_out_of_four(const vector<float>& vec);
};



#endif