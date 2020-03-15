package com.mmu.fyp.xiang.watermetercamera.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.mmu.fyp.xiang.watermetercamera.`interface`.WaterMeterImageAnalyserListener
import org.opencv.android.Utils
import org.opencv.core.Mat

/** Helper type alias used for analysis use case callbacks */
typealias onDeviceMobileVisionOCRListener = (isValidResult: Boolean, resultText: String) -> Unit

class WaterMeterImageAnalyser(listener: WaterMeterImageAnalyserListener?) {


    //add listeners use for callback
    private var listeners = ArrayList<WaterMeterImageAnalyserListener>().apply { listener?.let { add(it) } }

    //to store currentBitmap
    private var currentBitmap: Bitmap? = null

    //to reset
    fun clean() {
        currentBitmap = null
        listeners.clear()
    }

    fun setOnWaterMeterImageAnalyseDone(listener: WaterMeterImageAnalyserListener) = listeners.add(listener)

    fun analyseImage(imagePath: String)
    {
        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) return

        // get bitmap from path
        currentBitmap = BitmapFactory.decodeFile(imagePath)
        val mat = Mat()
        // convert it to opencv Mat (Matrix)
        Utils.bitmapToMat(currentBitmap, mat)
        // send matrix nativeObjectAddr which is address(reference) of the variable in cpp
        startAnalyse(mat.nativeObjAddr, 0)
    }

    /**
     * When cpp there done analysis, will call this function.
     * Reference: image-analysis.cpp callBackDoneWaterMeterImageAnalysis
     * After convert the parameters into suitable value, callback to the listener
     * @param isValidWaterMeterImage if the image pass to algorithm can get water meter reading
     * @param imageMatAddr the opencv mat object C++ memory address of cropped meter image
     * @param serialNoImageMatAddr the opencv mat object C++ memory address of cropped serial no
     * @param meterReadingImageMatAddrs the opencv mat object C++ memory address cropped and segmented meter readings
     */
    fun callBackDoneWaterMeterImageAnalysis(isValidWaterMeterImage: Boolean, imageMatAddr: Long, serialNoImageMatAddr: Long,
                                            meterReadingImageMatAddrs: LongArray) {

        //convert the params to suitable values so can pass to listener
        val callbackWaterMeterBitmap = generateWaterMeterBitmapFromMatAddr(isValidWaterMeterImage, imageMatAddr)
//        val callbackWaterMeterBitmap = generateWaterMeterBitmapFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[0])
        generateSerialNoFromMatAddr(isValidWaterMeterImage, serialNoImageMatAddr) { isValidSerialNoResult, callbackSerialNo ->

            Log.d(TAG, "Result serial no: $callbackSerialNo")
            var callbackMeterReading = ""
            generateMeterReadingFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[0]) {
                _, callback -> Log.d(TAG, "Result meter reading 1: $callback")
                callbackMeterReading += callback
            }
            generateMeterReadingFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[1]) {
                _, callback -> Log.d(TAG, "Result meter reading 2: $callback")
                callbackMeterReading += callback
            }
            generateMeterReadingFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[2]) {
                _, callback -> Log.d(TAG, "Result meter reading 3: $callback")
                callbackMeterReading += callback
            }
            generateMeterReadingFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[3]) {
                _, callback -> Log.d(TAG, "Result meter reading 4: $callback")
                callbackMeterReading += callback
            }
            generateMeterReadingFromMatAddr(isValidWaterMeterImage, meterReadingImageMatAddrs[4]) {
                _, callback -> Log.d(TAG, "Result meter reading 5: $callback")
                callbackMeterReading += callback

                listeners.forEach{
                    it.onWaterMeterImageAnalyseDone(isValidWaterMeterImage, callbackWaterMeterBitmap, callbackSerialNo, callbackMeterReading)
                }
            }



        }
    }

    private fun generateWaterMeterBitmapFromMatAddr(isValidWaterMeterImage: Boolean, imageMatAddr: Long): Bitmap?
    {
        //if not valid image direct return image before cropped/ analyse
        if(!isValidWaterMeterImage)
        {
            return currentBitmap
        }

        return getBitMapFromMatAddr(imageMatAddr)
    }

    private fun generateSerialNoFromMatAddr(isValidWaterMeterImage: Boolean, serialNoImageMatAddr: Long, callback: onDeviceMobileVisionOCRListener)
    {

        if(!isValidWaterMeterImage)
        {
            callback(false, "")
            return
        }


        val bitmap = getBitMapFromMatAddr(serialNoImageMatAddr)
        onDeviceMobileVisionOCR(bitmap, "ST", callback)
    }

    private fun generateMeterReadingFromMatAddr(isValidWaterMeterImage: Boolean, meterReadingImageMatAddr: Long, callback: onDeviceMobileVisionOCRListener)
    {
        if(!isValidWaterMeterImage || meterReadingImageMatAddr == 0L)
        {
            callback(false, "0")
            return
        }

        val bitmap = getBitMapFromMatAddr(meterReadingImageMatAddr)
        onDeviceMobileVisionOCR(bitmap, "0", callback)
    }

    private fun onDeviceMobileVisionOCR(bitmap: Bitmap?, defaultText: String, callback: onDeviceMobileVisionOCRListener) {

        if(bitmap == null)
        {
            callback(false, defaultText)
            return
        }
        //using firebase to
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                if(firebaseVisionText.text != "")
                {
                    Log.d(TAG, "success OCR")
                    callback(true, firebaseVisionText.text)
                }
                else
                {
                    callback(true, defaultText)
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, e.toString())
                callback(false, defaultText)
            }
    }


    private fun getBitMapFromMatAddr(matAddr: Long): Bitmap?
    {
        val resultMat = Mat(matAddr)
        if(resultMat.width() <= 0 || resultMat.height() <= 0)
        {
            return null
        }

        val bitmap = Bitmap.createBitmap(resultMat.width(), resultMat.height(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(resultMat, bitmap)

        return bitmap
    }


    //function in jni cpp
    private external fun startAnalyse(matAddress: Long, rotationDegrees: Int)

    companion object{
        private val TAG = WaterMeterImageAnalyser::class.java.simpleName

        init {
            System.loadLibrary("native-libs")
        }
    }
}