#ifndef WATER_METER_CAMERA_IMAGEANALYSIS_H
#define WATER_METER_CAMERA_IMAGEANALYSIS_H

#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include "ImageAnalysisHelper.h"

//meter reading digit numbers
#define METER_READING_IMAGE_SIZE 5

using namespace cv;
using namespace std;

class ImageAnalysis
{
    private:
        int rotationDegrees;
        //helper class
        ImageAnalysisHelper *helper;
        //step 1: find water meter from image
        bool detectWaterMeter(Mat &image);
        //to convert to hsv image and filter out blue object
        Mat findWaterMeterColorFilterMask(Mat &image);
        //To find out where meter locate at in the binary image
        Rect findWaterMeterBox(const Mat &mask);
        bool filterWaterMeterBoundRect(const Rect &rect, const Mat& mask);


        //step 2: find serial no from meter image
        bool detectMeterSerialNo();
        //to convert to hsv image and filter out black object
        Mat findSerialNoColorFilterMask();
        //To find out where serial no locate at in the binary image
        Rect findSerialNoBox(const Mat &mask);
        bool filterSerialNoBoundRect(const Rect &rect, const Mat& mask);
        //further crop serial no image by row and column, to remove upper and lower, left, right noises
        Mat getAccurateSerialNoImage(const Rect &serialNoBox);


        //step 3: find meter reading from meter image
        bool detectMeterReading();
        //reuse findWaterMeterColorFilterMask
        //To find out where water meter locate at in the binary image
        Rect findMeterReadingBox(const Mat &mask);
        bool filterMeterReadingBox(const Rect &rect, const Mat& mask);
        //further crop meter reading image by row and column, to remove upper and lower, left, right noises
        Mat getAccurateMeterReadingImage(const Rect &meterReadingBox, const Mat &mask);
        //segment meter reading image
        void segmentMeterReadingImage(const Mat &accurateMeterReadingImage);

    public:
        //to save analysis result
        bool isValidWaterMeterImage = false;

        Mat meterImage, serialNoImage, meterReadingImage[METER_READING_IMAGE_SIZE];
        ImageAnalysis(jint rotationDegrees);
        //main function call at first
        void analyseImage(Mat &image);
        //return mat C++ memory address
        long getMeterImageMatAddr();
        long getSerialNoImageMatAddr();
        long * getMeterReadingImageMatAddrs();
};


#endif //WATER_METER_CAMERA_IMAGEANALYSIS_H
