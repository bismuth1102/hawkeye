#include <jni.h>
#include <string>
#include "SORT.h"
#include "BoundingBox.h"
#include "android/log.h"

using namespace std;

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_skillion_hawkeye_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    string helloworld = print();
//    return env->NewStringUTF(helloworld.c_str());
//}


extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_skillion_hawkeye_Bbox_1Data_data2sort(
        JNIEnv* env,
        jobject /* this */,
        jfloatArray jInArray) {

    int inarray_num = 6;
    int outarray_num = 4;

    jboolean isCopy;
    int length = env->GetArrayLength(jInArray);
    int in_set_num = length / inarray_num;

    float* inArray = env->GetFloatArrayElements(jInArray, &isCopy);
    vector<BoundingBox> inVector;
    vector<BoundingBox> outVector;


    for (int i = 0; i < in_set_num; i++) {
        int id_from_OD = inArray[inarray_num*i];
//        __android_log_print(ANDROID_LOG_INFO, "0", "%d", id_from_OD);
        int frame_num = inArray[inarray_num*i+1];
        float left = inArray[inarray_num*i+2];
        float top = inArray[inarray_num*i+3];
        float right = inArray[inarray_num*i+4];
        float bottom = inArray[inarray_num*i+5];

        BoundingBox inbox = BoundingBox(id_from_OD, frame_num, left, top, right, bottom);
        inVector.push_back(inbox);
    }
    outVector = SORT::handle(inVector);


    int out_set_num = outVector.size();

    float outArray[out_set_num * outarray_num];
    for (int i = 0; i < out_set_num; i++) {
        outArray[i*outarray_num] = outVector[i].id_from_OD;
//        __android_log_print(ANDROID_LOG_INFO, "1", "%d", outVector[i].id_from_OD);
        outArray[i*outarray_num+1] = outVector[i].color;
        outArray[i*outarray_num+2] = outVector[i].id;
        outArray[i*outarray_num+3] = outVector[i].speed;
    }
    jfloatArray jOutArray = env->NewFloatArray(out_set_num * outarray_num);
    env->SetFloatArrayRegion(jOutArray, 0, out_set_num*outarray_num, outArray);

    env->ReleaseFloatArrayElements(jInArray, inArray, 0);

    return jOutArray;
}
