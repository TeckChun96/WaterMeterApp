package com.mmu.fyp.xiang.watermetercamera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.mmu.fyp.xiang.watermetercamera.fragments.CameraPermissionsFragment
import com.mmu.fyp.xiang.watermetercamera.utils.ImageFileIO
import java.io.File
import java.io.IOException

class CameraActivity : AppCompatActivity() {

    //to save taken images
    private var imagePaths: ArrayList<String> = ArrayList()
    //handle create/ remove image file
    private var imageFIleIO: ImageFileIO = ImageFileIO(this)
    //to check is take multiple image or not
    private var isMultipleImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //to set whether to take multiple images or not
        isMultipleImage = intent.getBooleanExtra("isMultipleImage", false)

        Log.d(TAG, "Camera activity create")
        if(!CameraPermissionsFragment.hasPermissions(this))
        {
            supportFragmentManager.beginTransaction().add(CameraPermissionsFragment(), null).commit()
        }
        else
        {
            startCamera()
        }
    }

    fun startCameraFromPermissionsFragment(cameraPermissionsFragment: CameraPermissionsFragment)
    {
        while(supportFragmentManager.backStackEntryCount > 0)
        {
            supportFragmentManager.popBackStack()
        }
        supportFragmentManager.beginTransaction().remove(cameraPermissionsFragment).commit()
        startCamera()
    }

    private fun startCamera()
    {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the image should go

                val imageFile: File? = try {
                    imageFIleIO.createImageFile(ImageFileIO.DEFAULT_CAMERA_METERS_DIR_PATH)
                }
                catch(ex: IOException)
                {
                    //if come out error direct go back previous activity
                    Toast.makeText(applicationContext, "Error while creating image file, please make sure " +
                            "you have give this app camera and file storage permissions", Toast.LENGTH_LONG).show()
                    onBackPressed()
                    null
                }

                // Continue only if the File was successfully created
                imageFile?.also {
                    val imageURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                    startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CAMERA_PIC_REQUEST){
            when(resultCode){
                Activity.RESULT_OK -> handleCameraOK()
                Activity.RESULT_CANCELED -> handleCameraCanceled()
                else -> {
                    Log.d(TAG, "Other Result Codes: $resultCode")
                    //remove empty image
                    imageFIleIO.removeEmptyCurrentImage()
                    onBackPressed()
                }
            }
        }
    }

    private fun handleCameraOK(){
        imagePaths.add(imageFIleIO.currentImage!!.absolutePath)
        imageFIleIO.sendImageToGallery()

        //if not multiple image direct proceed images analysis
        if(!isMultipleImage)
        {
            closeCameraWithResult()
        }
        else //else continue take more image
        {
            Toast.makeText(applicationContext, "Press back to proceed next process", Toast.LENGTH_LONG).show()
            startCamera()
        }
    }

    private fun handleCameraCanceled() {
        //remove empty image if cancel
        imageFIleIO.removeEmptyCurrentImage()
        //if already take image before and press cancel, proceed images analysis
        if (imagePaths.size > 0) {
            closeCameraWithResult()
        }
        else //else direct go back previous activity
        {
            onBackPressed()
        }
    }

    private fun closeCameraWithResult() {
        val data = Intent()
        data.putExtra("imagePaths", imagePaths)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onDestroy() {
        //remove empty image file if close app
        imageFIleIO.removeEmptyCurrentImage()
        super.onDestroy()
    }

    override fun onBackPressed() {

        while(supportFragmentManager.backStackEntryCount > 0)
        {
            supportFragmentManager.popBackStack()
        }
        super.onBackPressed()
    }

    companion object{
        private const val CAMERA_PIC_REQUEST = 1000
        private val TAG = CameraActivity::class.java.simpleName
    }
}
