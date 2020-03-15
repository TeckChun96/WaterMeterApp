#ifndef WATER_METER_CAMERA_IMAGEANALYSISHELPER_H
#define WATER_METER_CAMERA_IMAGEANALYSISHELPER_H

#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <fstream>

using namespace cv;
using namespace std;

#define LOG_TAG "WaterMeterCppLib"

class ImageAnalysisHelper {
    public:
        //to return the features of mask in rectangles
        vector<Rect> getBoundRectFromMask(const Mat &mask);

        //set minimum meter width and height in pixel
        const float MIN_METER_WIDTH = 300, MIN_METER_HEIGHT = 360;
        //detected meter width or height per width height in image
        //3:4 image ratio
        const float METER_IN_IMAGE_3_4_WIDTH_RATIO = 0.1, METER_IN_IMAGE_3_4_HEIGHT_RATIO = 0.1375;
        //4:3 image ratio
        const float METER_IN_IMAGE_4_3_WIDTH_RATIO = 0.0625, METER_IN_IMAGE_4_3_HEIGHT_RATIO = 0.1625;
        //16: 9 image ratio
        const float METER_IN_IMAGE_16_9_WIDTH_RATIO = 0.025, METER_IN_IMAGE_16_9_HEIGHT_RATIO = 0.25;
        //other image ratio / 1:1
        const float METER_IN_IMAGE_OTHER_WIDTH_RATIO = 0.1, METER_IN_IMAGE_OTHER_HEIGHT_RATIO = 0.1;
        //meter width / height
        const float METER_ASPECT_RATIO_MIN = 0.55, METER_ASPECT_RATIO_MAX = 0.85;
        //to handle with multiple image aspect ratio
        //to limit the water meter object must meet more than image height with how many percentage
        bool checkMeterInImageWidthRatio(float meterWidthRatio, float imageAspectRatio);
        //to handle with multiple image aspect ratio
        //to limit the water meter object must meet more than image height how many percentage
        bool checkMeterInImageHeightRatio(float meterHeightRatio, float imageAspectRatio);


        //serial no box at least locate after 60% of image height
        const float SERIAL_NO_IN_METER_HEIGHT_LOCATION_MIN = 0.6;
        //check the rect bound box is at correct place in meter image
        bool checkSerialNoRectLocation(const Rect &rect, const Size &imageSize);
        //minimum serial no box width and height in meter image
        const float SERIAL_NO_IN_METER_WIDTH_RATIO_MIN = 0.25, SERIAL_NO_IN_METER_HEIGHT_RATIO_MIN = 0.05;
        //check the rect bound box at least occupy some of the width and height of meter image
        bool checkSerialNoRectInImageSize(const Rect& rect, const Size &imageSize);
        //serial no width / height
        const float SERIAL_NO_ASPECT_RATIO_MIN = 2.0, SERIAL_NO_ASPECT_RATIO_MAX = 5.0;


        const float METER_READING_IN_METER_WIDTH_LOCATION_MIN = 0.1;
        //meter reading box at least locate before 60% of image height
        const float METER_READING_IN_METER_HEIGHT_LOCATION_MIN = 0.175, METER_READING_IN_METER_HEIGHT_LOCATION_MAX = 0.55;
        //check the rect bound box is at correct place in meter image
        bool checkMeterReadingRectLocation(const Rect &rect, const Size &imageSize);
        //minimum meter reading box width and height in meter image
        const float METER_READING_IN_METER_WIDTH_RATIO_MIN = 0.4, METER_READING_IN_METER_HEIGHT_RATIO_MIN = 0.05;
        //check the rect bound box at least occupy some of the width and height of meter image
        bool checkMeterReadingRectInImageSize(const Rect &rect, const Size &imageSize);
        //meter reading width / height
        const float METER_READING_ASPECT_RATIO_MIN = 1.5, METER_READING_ASPECT_RATIO_MAX = 6.0;

        //convert color image into binary image
        Mat thresholdImage(const Mat &image, int min, int max, int type, bool isInvertBin);
        //center crop the image
        Rect centerCropByPercent(const Rect& rect, float widthPercent, float heightPercent);

        //to count the number of binary white pixels per row
        int *horizontalProjectionMat(const Mat& srcImg, bool isInvertBin, bool isBin);
        //to count the number of binary white pixels per col
        int *verticalProjectionMat(const Mat& srcImg, bool isInvertBin, bool isBin);

        const int HORIZONTAL_PROJECTION = 0, VERTICAL_PROJECTION = 1;
        /**
         *
         * @param srcImg
         * @param projectValArr pixels count per row/  column
         * @param projectionType row / column
         * @param thresholdPixel max pixels count to indentify & each segment
         * @param border crop by border pixel
         * @return segmented mat blocks
         */
        vector<Rect> segmentMatByProjection(const Mat &srcImg, const vector<int> &projectValArr, int projectionType, int thresholdPixel, int border);
        /**
         *
         * @param srcImg
         * @param verticalProjectValArr pixels count per column
         * @param border crop by border pixel
         * @return mat with no vertical border
         */
        Mat removeVerticalEdgeByProjection(const Mat &srcImg, const vector<int> &verticalProjectValArr, int border);

        const int WIDTH_RECT = 0, HEIGHT_RECT = 1;
        //get max height/width block from blocks
        Rect getMaxBlock(const vector<Rect> &blocks, int type);

        void morph_erode(const Mat &src, Mat &dest, int errosion_size);
        void morph_dilate(const Mat &src, Mat &dest, int dilate_size);
        void morph_operation(const Mat &src, Mat &dest, int operation, int morph_size);
        void sharpen_image(const Mat &src, Mat &dest, double sigma, double amount);
        void resize_image(const Mat &src, Mat &dest, int size, bool isHeight, int interpolation);
};


#endif //WATER_METER_CAMERA_IMAGEANALYSISHELPER_H
