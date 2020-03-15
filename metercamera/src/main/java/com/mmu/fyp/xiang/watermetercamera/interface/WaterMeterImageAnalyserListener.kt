package com.mmu.fyp.xiang.watermetercamera.`interface`

import android.graphics.Bitmap

//To let other classes set callback when water meter image analysis done
interface WaterMeterImageAnalyserListener{
    /**
     * @param isValidWaterMeterImage if the image pass to algorithm can get water meter reading
     * @param waterMeterBitmap the cropped meter image if valid, else will be original image
     * @param serialNo serial no of meter
     * @param meterReading meter reading in float
     */
    fun onWaterMeterImageAnalyseDone(isValidWaterMeterImage: Boolean, waterMeterBitmap: Bitmap?, serialNo: String, meterReading: String)
}