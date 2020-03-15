#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include "ImageAnalysis.h"

using namespace std;
using namespace cv;

//callback to WaterMeterImageAnalysis.kt Class
int callBackDoneWaterMeterImageAnalysis(JNIEnv *env, jobject instance, bool isValidWaterMeterImage,
        long imageMatAddr, long serialNoImageMatAddr, long *meterReadingImageMatAddrs)
{
    //get class
    jclass thisClass = env->GetObjectClass(instance);
    //get function ID
    jmethodID methodID = env->GetMethodID(thisClass, "callBackDoneWaterMeterImageAnalysis", "(ZJJ[J)V");
    if (NULL == methodID)
    {
        return 0;
    }

    jlongArray meterReadingImageMatAddrsArray = env->NewLongArray(METER_READING_IMAGE_SIZE);
    env->SetLongArrayRegion(meterReadingImageMatAddrsArray, 0, METER_READING_IMAGE_SIZE, reinterpret_cast<const jlong *>(meterReadingImageMatAddrs));

    //call function callBackDoneWaterMeterImageAnalysis in WaterMeterImageAnalysis class
    env->CallVoidMethod(instance, methodID, isValidWaterMeterImage, imageMatAddr, serialNoImageMatAddr, meterReadingImageMatAddrsArray);

    return 0;
}


extern "C" JNIEXPORT void JNICALL
Java_com_mmu_fyp_xiang_watermetercamera_utils_WaterMeterImageAnalyser_startAnalyse(JNIEnv *env, jobject instance,
                                                                                   jlong matAddress, jint rotationDegrees) {

    //cast jlong address into Mat reference
    Mat& image = *(Mat*)matAddress;

    auto *imageAnalysis = new ImageAnalysis(rotationDegrees);
    //begin analyse meter
    imageAnalysis->analyseImage(image);

    bool isValidWaterMeterImage = imageAnalysis->isValidWaterMeterImage;
    long meterImageMatAddr = imageAnalysis->getMeterImageMatAddr();
    long serialNoImageMatAddr = imageAnalysis->getSerialNoImageMatAddr();
    long *meterReadingImageMatAddrs = imageAnalysis->getMeterReadingImageMatAddrs();

    callBackDoneWaterMeterImageAnalysis(env, instance, isValidWaterMeterImage, meterImageMatAddr, serialNoImageMatAddr, meterReadingImageMatAddrs);
}



