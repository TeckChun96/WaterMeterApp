#include "ImageAnalysis.h"



ImageAnalysis::ImageAnalysis(jint rotationDegrees)
{
    this->rotationDegrees = rotationDegrees;
    helper = new ImageAnalysisHelper();
    //initialize so can know which one no data
    for(auto & i : meterReadingImage)
    {
        i = Mat::zeros(Size(1, 1), CV_8U);
    }
}

//analyse the image and set the extract result
void ImageAnalysis::analyseImage(Mat &image)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Begin analyse image");
    //match condition found water meter image, find water meter serial no then return it is a valid image
    if(detectWaterMeter(image) && detectMeterSerialNo())
    {
        isValidWaterMeterImage = true;
        detectMeterReading();
    }
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "End analyse image");
}

long ImageAnalysis::getMeterImageMatAddr() {
    if(!isValidWaterMeterImage)
    {
        return 0L;
    }
    return reinterpret_cast<long>(&meterImage);
}

long ImageAnalysis::getSerialNoImageMatAddr() {
    if(!isValidWaterMeterImage)
    {
        return 0L;
    }
    return reinterpret_cast<long>(&serialNoImage);
}

long *ImageAnalysis::getMeterReadingImageMatAddrs()
{
    //initialize as 0 so java fun can know if is empty
    static long meterReadingImageMatAddrs[METER_READING_IMAGE_SIZE] = {0L, 0L, 0L, 0L, 0L};
    if(!isValidWaterMeterImage)
    {
        return meterReadingImageMatAddrs;
    }

    for(int i = 0; i < METER_READING_IMAGE_SIZE; ++i)
    {
        //initialize in constructor is width 1 and height 1, this is to check if no data
        if(meterReadingImage[i].cols > 1 && meterReadingImage[i].rows > 1)
        {
            meterReadingImageMatAddrs[i] = reinterpret_cast<long>(&meterReadingImage[i]);
        }
    }

    return meterReadingImageMatAddrs;
}

//step 1: find water meter from image
bool ImageAnalysis::detectWaterMeter(Mat &image) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Begin analyse meter");
    //mask is binary image which 1 (white area is blue object) else not
    Mat mask = findWaterMeterColorFilterMask(image);
    //to locate where meter at
    Rect meterBox = findWaterMeterBox(mask);

    //return false which is no meter found if meter box return blank rect
    if(meterBox.width == 1 && meterBox.height == 1 && meterBox.x == 0 && meterBox.y == 0)
    {
        return false;
    }
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Meter OK");
    //create meter image by crop image from meterbox
    meterImage = Mat(image, meterBox);

    return true;
}

//to convert to hsv image and filter out blue object
Mat ImageAnalysis::findWaterMeterColorFilterMask(Mat &image) {
    Mat tempImage, mask;

    //convert image to hsv
    cvtColor(image, tempImage, COLOR_RGB2HSV);

    //filter out blue object
    Scalar lower_blue(100, 50, 50);
    Scalar upper_blue(140, 255, 255);

    //set mask
    inRange(tempImage, lower_blue, upper_blue, mask);

    return mask;
}

//To find out where meter locate at in the binary image
Rect ImageAnalysis::findWaterMeterBox(const Mat &mask) {

    //get the features of mask in rectangle
    vector<Rect> boundRect = helper->getBoundRectFromMask(mask);

    for(const auto & rect : boundRect)
    {
        //filter out noises / un-wanted object
        if(!filterWaterMeterBoundRect(rect, mask))
        {
            continue;
        }

        //return first match bounding box, this contain where water meter locate at
        return rect;
    }

    //return blank rect if water meter not found
    return Rect(0, 0, 1, 1);
}

bool ImageAnalysis::filterWaterMeterBoundRect(const Rect &rect, const Mat& mask) {

    //filter out noises
    if(rect.width < helper->MIN_METER_WIDTH or rect.height < helper->MIN_METER_HEIGHT)
    {
        return false;
    }

    float meterWidthRatio = (float) rect.width / (float) mask.cols;
    float meterHeightRatio = (float) rect.height / (float) mask.rows;
    float imageAspectRatio = (float) mask.cols / (float) mask.rows;
    //if the bounding box width and height has at least larger than minimum requirement
    //of ratio of bounding meter width and height/ image width and height
    if(helper->checkMeterInImageWidthRatio(meterWidthRatio, imageAspectRatio) && helper->checkMeterInImageHeightRatio(meterHeightRatio, imageAspectRatio))
    {
        //get aspectRatio of the bounding box
        int width = rect.width;
        int height = rect.height;
        float aspectRatio =  (float) width / (float) height;

        //is match meter size aspectRatio
        return aspectRatio >= helper->METER_ASPECT_RATIO_MIN && aspectRatio <= helper->METER_ASPECT_RATIO_MAX;
    }
    return false;
}


//step 2: find serial no from meter image
bool ImageAnalysis::detectMeterSerialNo() {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Begin analyse serial no");
    //mask is binary image which 1 (white area is black object) else not
    Mat mask = findSerialNoColorFilterMask();
    //to locate where serial no at
    Rect serialNoBox = findSerialNoBox(mask);
    //return false which is no serial no found if serial no box return blank rect
    if(serialNoBox.width == 1 && serialNoBox.height == 1 && serialNoBox.x == 0 && serialNoBox.y == 0)
    {
        return false;
    }

    serialNoImage = getAccurateSerialNoImage(serialNoBox);
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Serial no OK");

    return true;
}

//to convert to hsv image and filter out black object
Mat ImageAnalysis::findSerialNoColorFilterMask() {
    Mat tempImage, mask;

    //convert image to hsv
    cvtColor(meterImage, tempImage, COLOR_RGB2HSV);

    //filter out black object
    Scalar lower_black(0, 0, 0);
    Scalar upper_black(255, 255, 85);

    //set mask
    inRange(tempImage, lower_black, upper_black, mask);

    //remove noises
    helper->morph_erode(mask, mask, 10);

    return mask;
}

//To find out where serial no locate at in the binary image
Rect ImageAnalysis::findSerialNoBox(const Mat &mask) {
    //get the features of mask in rectangle
    vector<Rect> boundRect = helper->getBoundRectFromMask(mask);

    for(const auto & rect : boundRect)
    {
        //filter out noises / un-wanted object
        if(!filterSerialNoBoundRect(rect, mask))
        {
            continue;
        }
        //return first match bounding box, this contain serial no locate at
        //further center crop rect to remove outer noises
        return helper->centerCropByPercent(rect, 0.05, 0.1);
    }

    //return blank rect if serial no not found
    return Rect(0, 0, 1, 1);
}

bool ImageAnalysis::filterSerialNoBoundRect(const Rect &rect, const Mat& mask) {
    //filter out noises
    //check the rect bound box is at correct place in meter image
    //check the rect bound box at least occupy some of the width and height of meter image//check the rect bound box at least occupy some of the width and height of meter image
    if(!helper->checkSerialNoRectLocation(rect, mask.size()) || !helper->checkSerialNoRectInImageSize(rect, mask.size()))
    {
        return false;
    }

    //get aspectRatio of the bounding box
    int width = rect.width;
    int height = rect.height;
    float aspectRatio =  (float) width / (float) height;
    //is match serial no size aspectRatio
    return aspectRatio >= helper->SERIAL_NO_ASPECT_RATIO_MIN && aspectRatio <= helper->SERIAL_NO_ASPECT_RATIO_MAX;
}

//further crop serial no image by row and column, to remove upper and lower, left, right noises
Mat ImageAnalysis::getAccurateSerialNoImage(const Rect &serialNoBox)
{
    //create temp serial no image by crop again meter Image and box
    Mat tempSerialNoImg = Mat(meterImage, serialNoBox);

    //get horizontal projection
    int *horizontalProjectionArr = helper->horizontalProjectionMat(tempSerialNoImg, false, false);
    //get each block (white pixels group based on horizontal projection)
    vector<Rect> horizontalSegmentedMatBlocks = helper->segmentMatByProjection(tempSerialNoImg,
            reinterpret_cast<const vector<int> &>(horizontalProjectionArr), helper->HORIZONTAL_PROJECTION, 0, 5);

    //get maximum height block
    Rect maxBlock = helper->getMaxBlock(horizontalSegmentedMatBlocks, helper->HEIGHT_RECT);
    Mat resultMaxBlock(tempSerialNoImg, maxBlock);

    //get vertical projection
    int *verticalProjectionArr = helper->verticalProjectionMat(resultMaxBlock, false, false);
    //remove left and right noises
    return helper->removeVerticalEdgeByProjection(resultMaxBlock, reinterpret_cast<const vector<int> &>(verticalProjectionArr), 5);
}


//step 3: find meter reading from meter image
bool ImageAnalysis::detectMeterReading() {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Begin analyse meter reading");
    //mask is binary image which 1 (white area is non blue object) else not
    Mat mask = findWaterMeterColorFilterMask(meterImage);
    //invert to exclude blue object
    bitwise_not(mask, mask);
    //remove noises
//    helper->morph_erode(mask, mask, 3);
    helper->morph_operation(mask, mask, MORPH_OPEN, 15);
    //to locate where meter reading at
    Rect meterReadingBox = findMeterReadingBox(mask);
    //return false which is no serial no found if serial no box return blank rect
    if(meterReadingBox.width == 1 && meterReadingBox.height == 1 && meterReadingBox.x == 0 && meterReadingBox.y == 0)
    {
        return false;
    }

    Mat accurateMeterReadingImage = getAccurateMeterReadingImage(meterReadingBox, mask);
    segmentMeterReadingImage(accurateMeterReadingImage);

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Meter reading OK");

    return true;
}

//To find out where water meter locate at in the binary image
Rect ImageAnalysis::findMeterReadingBox(const Mat &mask) {
    //get the features of mask in rectangle
    vector<Rect> boundRect = helper->getBoundRectFromMask(mask);

    for(const auto & rect : boundRect)
    {
        //filter out noises / un-wanted object
        if(!filterMeterReadingBox(rect, mask))
        {
            continue;
        }
        //return first match bounding box, this contain mter reading locate at
        return rect;
    }

    //return blank rect meter reading not found
    return Rect(0, 0, 1, 1);
}

bool ImageAnalysis::filterMeterReadingBox(const Rect &rect, const Mat &mask) {
    //filter out noises

    if(!helper->checkMeterReadingRectLocation(rect, mask.size()) || !helper->checkMeterReadingRectInImageSize(rect, mask.size()))
    {
        return false;
    }

    //get aspectRatio of the bounding box
    int width = rect.width;
    int height = rect.height;
    float aspectRatio =  (float) width / (float) height;
    //is match meter reading size aspectRatio
    return aspectRatio >= helper->METER_READING_ASPECT_RATIO_MIN && aspectRatio <= helper->METER_READING_ASPECT_RATIO_MAX;
}

//further crop meter reading image by row and column, to remove upper and lower, left, right noises
Mat ImageAnalysis::getAccurateMeterReadingImage(const Rect &meterReadingBox, const Mat &mask) {
    //to get cropped mask with meter reading
    Mat croppedMask(mask, meterReadingBox);
    Mat croppedMeterReadingImage(meterImage, meterReadingBox);
    //find horizontal projection arr
    int *horizontalProjectionArr = helper->horizontalProjectionMat(croppedMask, false, true);

    //seperate block by min each row white pixels > 50% to remove noises like rust area
    vector<Rect> horizontalSegmentedMatBlocks = helper->segmentMatByProjection(croppedMask,
            reinterpret_cast<const vector<int> &>(horizontalProjectionArr), helper->HORIZONTAL_PROJECTION, (int)(croppedMask.cols * 0.5), 5);
    //get block with maximum height
    Rect maxHeightBlock = helper->getMaxBlock(horizontalSegmentedMatBlocks, helper->HEIGHT_RECT);
    //set meter reading image with removed upper and lower noises
    Mat resultMaxHeightBlock(croppedMask, maxHeightBlock);
    croppedMeterReadingImage = croppedMeterReadingImage(maxHeightBlock);

    //get vertical projection
    int *verticalProjectionArr = helper->verticalProjectionMat(resultMaxHeightBlock, false, true);
    //seperate block by min each col white pixels > 50% to remove noises like rust area
    vector<Rect> verticalSegmentedMatBlocks = helper->segmentMatByProjection(resultMaxHeightBlock,
            reinterpret_cast<const vector<int> &>(verticalProjectionArr), helper->VERTICAL_PROJECTION,
            (int)(resultMaxHeightBlock.rows * 0.5), 5);
    Rect maxWidthBlock = helper->getMaxBlock(verticalSegmentedMatBlocks, helper->WIDTH_RECT);

    //remove 20% height from below
    maxWidthBlock.height = maxWidthBlock.height - (int)(maxWidthBlock.height * 0.25);
    //set meter reading image with removed left and right noises
    return croppedMeterReadingImage(maxWidthBlock);
}

//segment meter reading image
void ImageAnalysis::segmentMeterReadingImage(const Mat &accurateMeterReadingImage) {
    Mat sharp, binImg, binImg2;
    helper->sharpen_image(accurateMeterReadingImage, sharp, 15, 5);

    //convert to binary image
    cvtColor(sharp, binImg, COLOR_BGR2GRAY);
    threshold(binImg, binImg, 0, 255, THRESH_OTSU);
    //remove noises
    helper->morph_erode(binImg, binImg2, 3);

    //get vertical projection
    int *verticalProjectionArr = helper->verticalProjectionMat(binImg2, false, true);

    //seperate block by min each col white pixels > 2.5% to remove noises like rust area
    vector<Rect> verticalSegmentedMatBlocks = helper->segmentMatByProjection(binImg2,
            reinterpret_cast<const vector<int> &>(verticalProjectionArr), helper->VERTICAL_PROJECTION,
            0, 5);

    int i = 0;
    for(const Rect &block: verticalSegmentedMatBlocks)
    {
        if(block.width >= sharp.cols * 0.05 && i < METER_READING_IMAGE_SIZE)
        {
            Mat temp = accurateMeterReadingImage(block);
            if(temp.cols < 32)
            {
                helper->resize_image(temp, meterReadingImage[i], 32, false, INTER_CUBIC);
            }
            else
            {
                meterReadingImage[i] = temp;
            }

            detailEnhance(meterReadingImage[i], meterReadingImage[i]);
            cvtColor(meterReadingImage[i], meterReadingImage[i], COLOR_BGR2GRAY);
            ++i;
        }
    }
}

