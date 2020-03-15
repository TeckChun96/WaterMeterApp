#include "ImageAnalysisHelper.h"

//to return the features of mask in rectangles
vector<Rect> ImageAnalysisHelper::getBoundRectFromMask(const Mat &mask) {
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;

    //find binary image contours
    findContours(mask, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, Point(0, 0));

    vector<vector<Point>> contours_poly(contours.size());
    vector<Rect> boundRect(contours.size());

    //convert to rectangle bounding box
    for( int i = 0; i< contours.size(); i++ )
    {
        approxPolyDP(contours[i], contours_poly[i], 3, true);
        boundRect[i] = boundingRect(contours_poly[i]);
    }

    return boundRect;
}

//to handle with multiple image aspect ratio
//to limit the water meter object must meet more than image height with how many percentage
bool ImageAnalysisHelper::checkMeterInImageWidthRatio(float meterWidthRatio, float imageAspectRatio) {
    float epsilon = 0.000001;
    if(abs(3.0 / 4.0 - imageAspectRatio) < epsilon)
    {
        return meterWidthRatio >= METER_IN_IMAGE_3_4_WIDTH_RATIO;
    }
    else if(abs(4.0 / 3.0 - imageAspectRatio) < epsilon)
    {
        return meterWidthRatio >= METER_IN_IMAGE_4_3_WIDTH_RATIO;
    }
    else if(abs(16.0 / 9.0 - imageAspectRatio) < epsilon)
    {
        return meterWidthRatio >= METER_IN_IMAGE_16_9_WIDTH_RATIO;
    }
    else
    {
        return meterWidthRatio >= METER_IN_IMAGE_OTHER_WIDTH_RATIO;
    }
}

//to handle with multiple image aspect ratio
//to limit the water meter object must meet more than image height how many percentage
bool ImageAnalysisHelper::checkMeterInImageHeightRatio(float meterHeightRatio, float imageAspectRatio) {
    float epsilon = 0.000001;
    if(abs(3.0 / 4.0 - imageAspectRatio) < epsilon)
    {
        return meterHeightRatio >= METER_IN_IMAGE_3_4_HEIGHT_RATIO;
    }
    else if(abs(4.0 / 3.0 - imageAspectRatio) < epsilon)
    {
        return meterHeightRatio >= METER_IN_IMAGE_4_3_HEIGHT_RATIO;
    }
    else if(abs(16.0 / 9.0 - imageAspectRatio) < epsilon)
    {
        return meterHeightRatio >= METER_IN_IMAGE_16_9_HEIGHT_RATIO;
    }
    else
    {
        return meterHeightRatio >= METER_IN_IMAGE_OTHER_HEIGHT_RATIO;
    }
}

//check the rect bound box is at correct place in meter image
bool ImageAnalysisHelper::checkSerialNoRectLocation(const Rect& rect, const Size &imageSize)
{
    float minHeight = (float)imageSize.height * SERIAL_NO_IN_METER_HEIGHT_LOCATION_MIN;
    return rect.y >= minHeight;
}


//check the rect bound box at least occupy some of the width and height of meter image
bool ImageAnalysisHelper::checkSerialNoRectInImageSize(const Rect& rect, const Size &imageSize)
{
    float minRectWidth = imageSize.width * SERIAL_NO_IN_METER_WIDTH_RATIO_MIN;
    float minRectHeight = imageSize.height * SERIAL_NO_IN_METER_HEIGHT_RATIO_MIN;

    return rect.width >= minRectWidth && rect.height >= minRectHeight;
}

//check the rect bound box is at correct place in meter image
bool ImageAnalysisHelper::checkMeterReadingRectLocation(const Rect& rect, const Size &imageSize)
{
    float minWidth = imageSize.width * METER_READING_IN_METER_WIDTH_LOCATION_MIN;
    float minHeight = imageSize.height * METER_READING_IN_METER_HEIGHT_LOCATION_MIN;
    float maxHeight = imageSize.height * METER_READING_IN_METER_HEIGHT_LOCATION_MAX;

    return rect.x >= minWidth && rect.y >= minHeight && rect.y <= maxHeight;
}

//check the rect bound box at least occupy some of the width and height of meter image
bool ImageAnalysisHelper::checkMeterReadingRectInImageSize(const Rect& rect, const Size &imageSize)
{
    float minRectWidth = imageSize.width * METER_READING_IN_METER_WIDTH_RATIO_MIN;
    float minRectHeight = imageSize.height * METER_READING_IN_METER_HEIGHT_RATIO_MIN;

    return rect.width >= minRectWidth && rect.height >= minRectHeight;
}

//convert color image into binary image
Mat ImageAnalysisHelper::thresholdImage(const Mat &image, int min, int max, int type, bool isInvertBin) {

    Mat tempImg, binImg;
    //to gray first for easy analysis
    cvtColor(image, tempImg, COLOR_BGR2GRAY);

    blur(tempImg, binImg, Size(3, 3));
    threshold(binImg, binImg, min, max, type);

    //if the input image is white background and black object, invert to white object, black background
    if(isInvertBin)
    {
        bitwise_not(binImg, binImg);
    }

    return binImg;
}

//center crop the image
Rect ImageAnalysisHelper::centerCropByPercent(const Rect& rect, float widthPercent, float heightPercent)
{
    int croppedX = rect.x + (int)(rect.width * widthPercent);
    int croppedY = rect.y + (int)(rect.height * heightPercent);
    int croppedWidth = rect.width - (int)(rect.width * widthPercent * 2);
    int croppedHeight = rect.height - (int)(rect.height * heightPercent * 2);


    return {croppedX, croppedY, croppedWidth, croppedHeight};
}

//to count the number of binary white pixels per row
int *ImageAnalysisHelper::horizontalProjectionMat(const Mat& srcImg, bool isInvertBin, bool isBin = false)
{
    //convert to binary image
    Mat binImg;
    if(isBin){
        binImg = srcImg;
    } else {
        binImg = thresholdImage(srcImg, 0, 255, THRESH_OTSU, isInvertBin);
    }

    int perPixelValue = 0;
    int width = srcImg.cols;
    int height = srcImg.rows;
    //to save each white pixels count per row
    int* projectValArr = new int[height];
    //init with 0 with 4 * height bytes
    memset(projectValArr, 0, static_cast<size_t>(height * 4));

    //count each pixel by row
    for (int row = 0; row < height; row ++)
    {
        for (int col = 0; col < width; col++)
        {
            perPixelValue = binImg.at<uchar>(row, col);
            //if is white pixel
            if (perPixelValue > 0)
            {
                projectValArr[row]++;
            }
        }
    }

    return projectValArr;
}

//to count the number of binary white pixels per col
int *ImageAnalysisHelper::verticalProjectionMat(const Mat& srcImg, bool isInvertBin, bool isBin = false)
{
    //convert to binary image
    Mat binImg;
    if(isBin){
        binImg = srcImg;
    } else {
        binImg = thresholdImage(srcImg, 0, 255, THRESH_OTSU, isInvertBin);
    }

    int perPixelValue = 0;
    int width = srcImg.cols;
    int height = srcImg.rows;
    //to save each white pixels count per row
    int* projectValArr = new int[width];
    //init with 0 with 4 * width bytes
    memset(projectValArr, 0, static_cast<size_t>(width * 4));

    //count each pixel by col
    for (int col = 0; col < width; col++)
    {
        for (int row = 0; row < height; row++)
        {
            perPixelValue = binImg.at<uchar>(row, col);
            //if is white pixel
            if (perPixelValue > 0)
            {
                projectValArr[col]++;
            }
        }
    }

    return projectValArr;
}


/**
 *
 * @param srcImg
 * @param projectValArr pixels count per row/  column
 * @param projectionType row / column
 * @param thresholdPixel max pixels count to indentify & each segment
 * @param border crop by border pixel
 * @return segmented mat blocks
 */
vector<Rect> ImageAnalysisHelper::segmentMatByProjection(const Mat &srcImg, const vector<int> &projectValArr,
        int projectionType, int thresholdPixel = 0, int border = 1)
{
    //result to save segmentedresult
    vector<Rect> segmentedMatBlocks;
    //to record segment start index
    int startIndex = 0;
    //to record segment end index
    int endIndex = 0;
    //to know is loop in segnment block
    bool inBlock = false;
    //is loop by width / height
    int length = (VERTICAL_PROJECTION == projectionType) ? srcImg.cols : srcImg.rows;

    for (int i = 0; i < length; i++)
    {
        //if reach row/ col with more than thresholdPixel white pixel (block)
        if (!inBlock && projectValArr[i] > thresholdPixel)
        {
            inBlock = true;
            startIndex = i;
        }
        //if end block (reach row / col with no/ less than number of white pixel)
        //or until end of index
        else if (inBlock && ((projectValArr[i] <= thresholdPixel) || (i == length - 1)))
        {
            endIndex = i;
            inBlock = false;
            //crop block with put 1 pixel black row / col
            Rect imageBlock;
            int cropStartIndex = (startIndex - border < 0)?0:(startIndex - border);
            int cropEndIndex = (endIndex + border > length)?length:(endIndex + border);
            if((VERTICAL_PROJECTION == projectionType))
            {
                //(x, y, width (end col - start col), height)
                imageBlock = Rect(cropStartIndex, 0, cropEndIndex - cropStartIndex, srcImg.rows);
            }
            else
            {
                //(x, y, width width, height (end row - start row))
                imageBlock = Rect(0, cropStartIndex, srcImg.cols, cropEndIndex - cropStartIndex);
            }
            segmentedMatBlocks.push_back(imageBlock);
        }
    }

    return segmentedMatBlocks;
}

/**
 *
 * @param srcImg
 * @param verticalProjectValArr pixels count per column
 * @param border crop by border pixel
 * @return mat with no vertical border
 */
Mat ImageAnalysisHelper::removeVerticalEdgeByProjection(const Mat &srcImg, const vector<int> &verticalProjectValArr, int border = 1)
{
    int startIndex = 0;
    int endIndex = srcImg.cols;

    //get the left col start got white pixel
    for(int i = 0; i < srcImg.cols; ++ i)
    {
        if(verticalProjectValArr[i] > 0)
        {
            startIndex = i;
            break;
        }
    }

    //get the last right col with white pixel
    for(int i = srcImg.cols - 1; i >= 0; -- i)
    {
        if(verticalProjectValArr[i] > 0)
        {
            endIndex = i;
            break;
        }
    }


    //crop vertical image
    int cropStartIndex = (startIndex - border < 0)?0:(startIndex - border);
    int cropEndIndex = (endIndex + border > srcImg.cols)?srcImg.cols:(endIndex + border);
    return srcImg(Range(0, srcImg.rows), Range(cropStartIndex, cropEndIndex));
}

//get max height/width block from blocks
Rect ImageAnalysisHelper::getMaxBlock(const vector<Rect> &blocks, int type) {

    int max = 0;
    Rect maxBlock;

    for(const Rect& block: blocks)
    {
        //to get largest block with maximum height
        if(HEIGHT_RECT == type && block.height > max)
        {
            max = block.height;
            maxBlock = block;
        }
        else if(WIDTH_RECT == type &&  block.width > max)
        {
            max = block.width;
            maxBlock = block;
        }
    }

    return maxBlock;
}

void ImageAnalysisHelper::morph_erode(const Mat &src, Mat &dest, int errosion_size) {

    Mat element = getStructuringElement(MORPH_RECT,
            Size(2 * errosion_size + 1, 2 * errosion_size + 1),
            Point(errosion_size, errosion_size));

    erode(src, dest, element);
}

void ImageAnalysisHelper::morph_dilate(const Mat &src, Mat &dest, int dilate_size) {

    Mat element = getStructuringElement(MORPH_RECT,
                                        Size(2 * dilate_size + 1, 2 * dilate_size + 1),
                                        Point(dilate_size, dilate_size));

    dilate(src, dest, element);
}

void ImageAnalysisHelper::morph_operation(const Mat &src, Mat &dest, int operation, int morph_size) {
    Mat element = getStructuringElement(MORPH_RECT,
                                        Size(2 * morph_size + 1, 2 * morph_size + 1),
                                        Point(morph_size, morph_size));

    morphologyEx(src, dest, operation, element);
}

void ImageAnalysisHelper::sharpen_image(const Mat &src, Mat &dest, double sigma, double amount) {
    Mat blurry;
    //get blur image
    GaussianBlur(src, blurry, Size(), sigma);
    //deduct blur image to get sharp image
    addWeighted(src, 1 + amount, blurry, -amount, 0, dest);
}

void ImageAnalysisHelper::resize_image(const Mat &src, Mat &dest, int size, bool isHeight, int interpolation)
{
    Size enlargeSize;
    int ratio = 0;

    if(isHeight)
    {
        ratio = size / src.rows;
        enlargeSize = Size(src.cols * ratio, size);
    }
    else
    {
        ratio = size / src.cols;
        enlargeSize = Size(size, src.rows * ratio);
    }

    resize(src, dest, enlargeSize, 0, 0, interpolation);
}


