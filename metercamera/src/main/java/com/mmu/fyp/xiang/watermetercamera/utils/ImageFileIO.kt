package com.mmu.fyp.xiang.watermetercamera.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ImageFileIO(private var context: Context) {

    var currentImage: File? = null

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(dirPath: String): File {
        // Create an image file name, example 20200101_200000
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //image store in camera folder first
        val storageDir: File? = context.getExternalFilesDir(dirPath)

        //create empty temp image file
        return File.createTempFile(

            "img_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentImage = this
        }
    }

    @Throws(IOException::class)
    fun removeEmptyCurrentImage(){
        //remove file if not null and image file is empty
        if(currentImage != null && currentImage!!.length() <= 0)
        {
            currentImage!!.delete()
            //set it back to null
            currentImage = null
        }
    }

    //send image file to gallery app visible
    fun sendImageToGallery()
    {
        if(currentImage != null)
        {
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                mediaScanIntent.data = Uri.fromFile(currentImage)
                context.sendBroadcast(mediaScanIntent)
            }
        }
    }

    //TODO
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun saveBitmap(bitmap: Bitmap): String
    {
        val imageFile: File? = try {
            createImageFile(DEFAULT_METERS_DIR_PATH)
        }
        catch(ex: IOException)
        {
            Toast.makeText(context.applicationContext, "Error while creating image file, please make sure " +
                    "you have give this app file storage permissions", Toast.LENGTH_LONG).show()
            null
            return ""
        }

        // Continue only if the File was successfully created
        imageFile?.also {

            return try {
                val stream = FileOutputStream(it)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()

                it.absolutePath
            } catch(ex: IOException) {
                Toast.makeText(context.applicationContext, "Error while saving image file, please make sure " +
                        "you have give this app file storage permissions", Toast.LENGTH_LONG).show()

                ""
            }
        }

        return ""
    }

    companion object {
        private val TAG = ImageFileIO::class.java.simpleName
        val DEFAULT_CAMERA_METERS_DIR_PATH = "images/Water Meters Camera"
        val DEFAULT_METERS_DIR_PATH = "images/Water Meters"
    }
}