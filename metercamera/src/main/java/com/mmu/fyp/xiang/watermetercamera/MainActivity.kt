package com.mmu.fyp.xiang.watermetercamera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButton()
    }

    private fun initButton(){
        val testBtn = findViewById<Button>(R.id.testBtn)
        testBtn.setOnClickListener {
            val intent = Intent(this, ResultWaterMeterImageAnalyser::class.java)
            val imagePaths = ArrayList<String>()
            imagePaths.add("/storage/emulated/0/Android/data/com.mmu.fyp.xiang.watermetercamera/files/images/Water Meters Camera/meter3.jpeg")
            intent.putExtra("imagePaths", imagePaths)
            startActivity(intent)
//            val intent = Intent(this, CameraActivity::class.java)
//            intent.putExtra("isMultipleImage", true)
//            startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_ACTIVITY_REQUEST){
            when(resultCode){
                Activity.RESULT_OK -> {
                    //this will be the image paths from camera
                    val imagePaths = data?.getSerializableExtra("imagePaths") as ArrayList<String>
                    Log.d(TAG, imagePaths[0])
                }
            }
        }
    }

    companion object{
        private const val CAMERA_ACTIVITY_REQUEST = 1000
        private val TAG = MainActivity::class.java.simpleName

    }
}
