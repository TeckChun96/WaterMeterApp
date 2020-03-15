package com.mmu.fyp.xiang.watermetercamera

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mmu.fyp.xiang.watermetercamera.`interface`.WaterMeterImageAnalyserListener
import com.mmu.fyp.xiang.watermetercamera.utils.WaterMeterImageAnalyser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ResultWaterMeterImageAnalyser : AppCompatActivity(), WaterMeterImageAnalyserListener {

    private lateinit var imagePaths: ArrayList<String>

    private lateinit var imageView: ImageView

    private val waterMeterImageAnalyser = WaterMeterImageAnalyser(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_analysis)

        imageView = findViewById(R.id.imageView)
        imagePaths = intent.getSerializableExtra("imagePaths") as ArrayList<String>

        waterMeterImageAnalyser.analyseImage(imagePaths[0])
    }

    override fun onWaterMeterImageAnalyseDone(
        isValidWaterMeterImage: Boolean,
        waterMeterBitmap: Bitmap?,
        serialNo: String,
        meterReading: String
    ) {
        imageView.setImageBitmap(waterMeterBitmap)
        Log.d(TAG, meterReading)
        Toast.makeText(applicationContext, "Scan success. Serial no.: $serialNo, MeterReading: $meterReading", Toast.LENGTH_LONG).show()
    }

    companion object{
        private val TAG = ResultWaterMeterImageAnalyser::class.java.simpleName
    }
}
