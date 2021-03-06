<br>
它能够快速准确地检测到摄像头内的多个物体，显示手机与物体的大致距离。每当有物体距离过近时，它会发出警告。<br>
用于智能骑行，骑行者将手机与车尾摄像头相连。当有车辆靠近，app能够提示骑行者。<br>
可以切换使用tflite模型和用yolov3-tiny训练的模型。<br>

****

## 名词

* sort算法：simple online and realtime tracking algorithm。关注短时目标追踪，使用匈牙利算法和卡尔曼过滤器。<br>
* 匈牙利算法：一种寻找二分图的最大匹配的算法，在多目标跟踪问题中可以简单理解为寻找前后两帧的若干目标的匹配最优解的一种算法。<br>
* 卡尔曼过滤器：一种运动模型，用来对目标的轨迹进行预测，并且使用确信度较高的跟踪结果进行预测结果的修正。有三种情况：<br>
	已匹配轨迹Matched_Track。预测结果与检测结果匹配，则更新Kalman滤波器，并预测目标下一帧结果；<br>
	未匹配检测Unmatched_Detections。检测到目标未与任何一个预测结果匹配，此时认为新目标进入场景，产生新的目标ID；<br>
	无匹配轨迹Unmatched_Tracks。预测结果未与任何一个检测结果匹配，此时认为目标丢失，从待跟踪目标中删除该目标ID<br>

* bounding box: 在camera preview内框住检测到的对象的矩形，可以为每个bounding box设置不同的颜色和标题。CNN模型会分析出检测到的物体的位置，SORT算法会过滤得到要展示的位置，MultiBoxTracker类的draw方法绘制bounding box。<br>


## 细节

* native-lib.cpp是Java调用c++方法的接口。数据传输只能用使用基本变量，不能直接传递object。所以需要把bounding box的数据分解成float array，在c++中叫做jFloatArray。数据传输过程如下：<br>
![image](https://github.com/bismuth1102/noilliks_hawkeye/blob/main/img/hawkeye.png)


## 学到的
如果成员变量中存在context，不要使用单例模式，会导致内存泄漏。<br>
Android链接C++库：https://blog.csdn.net/weixin_42787199/article/details/116419281<br>


## 参考

* tflite给出的object detection<br>
https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android

* 使用yolo模型替代tflite模型<br>
https://github.com/hunglc007/tensorflow-yolov4-tflite/tree/master/android

* 调用USB camera<br>
https://github.com/jiangdongguo/AndroidUSBCamera

* popup window<br>
https://github.com/razerdp/BasePopup
