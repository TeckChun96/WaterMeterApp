package com.example.watermeterapp

import android.R.attr.content
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_get_readings.*
import kotlinx.android.synthetic.main.activity_home_page.*
import java.util.jar.Manifest
import android.R.attr.data
import android.app.AlertDialog
import android.content.*
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.mmu.fyp.xiang.watermetercamera.CameraActivity
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import java.io.*


class HomePage : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        button_viewReadings.setOnClickListener{
            val fileName = filesDir.absolutePath + "/UnitJson.json"
            val file = File(fileName)
            if (file.exists())
            {
                val intent= Intent(this,ViewRecordMain::class.java)
                startActivity(intent)
            }
            else{
                openDialog()
            }
        }

        //button clicked
        button_getReadings.setOnClickListener{

            var select = arrayOf("Take single reading", "Take multiple reading", "Get reading from gallery")

            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Select Options")
                setItems(select) { dialog, which ->
                    if(which == 0)
                    {
                        openCamera(false)
                    }
                    else if(which == 1)
                    {
                        openCamera(true)
                    }
                    else
                    {
                        pickMultipleImageFromGallery()
                    }
                }
                show()
            }

//

        }


        button_erase_data.setOnClickListener {
            eraseData()
        }

        button_export_csv.setOnClickListener {
            export_to_csv()
        }

        button_share.setOnClickListener {
            shareFunction()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CAMERA_ACTIVITY_REQUEST){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val intent = Intent(this, GetReadings::class.java)
                    //this will be the image paths from camera
                    val imagePaths = data?.getSerializableExtra("imagePaths") as ArrayList<String>
                    intent.putExtra("imagePaths", imagePaths)
                    startActivity(intent)
                }
            }
        }
        else if(requestCode == PICK_IMAGE_MULTIPLE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val imagePaths = ArrayList<String>()
                    if(data?.clipData != null)
                    {
                        for(x in 0 until  data.clipData!!.itemCount)
                        {
                            val uri = data.clipData!!.getItemAt(x).uri
                            val imagePath = getImageFilePath(uri)
                            imagePaths.add(imagePath)
                        }
                    }
                    else if(data?.data != null)
                    {
                        val uri = data.data
                        val imagePath = uri?.let { getImageFilePath(it) }
                        if (imagePath != null) {
                            imagePaths.add(imagePath)
                        }
                    }

                    if(imagePaths.isNotEmpty())
                    {
                        val intent = Intent(this, GetReadings::class.java)
                        intent.putExtra("imagePaths", imagePaths)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    fun getImageFilePath(uri: Uri): String {
        val file = File(uri.path)
        val filePath = file.path.split(':')
        val image_id = filePath[filePath.size - 1]

        val cursor = contentResolver.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID+"=?", arrayOf(image_id), null)

        if(cursor != null)
        {
            cursor.moveToFirst()
            val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))

            return imagePath
        }

        return ""
    }

    //pick one from gallery function
//    private fun pickImageFromGallery() {
//        //Intent to pick image
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, GALLERY_PICK_CODE)
//    }

    //pick multiple from gallery function
    private fun pickMultipleImageFromGallery(){
        //Intent to pick image
        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_MULTIPLE)
    }


    private fun openCamera(isMultipleImage: Boolean){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("isMultipleImage", isMultipleImage)
        startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST)
    }

    private fun openDialog(){

        val builder  = AlertDialog.Builder(this)

        builder.setTitle("Alert!")
        builder.setMessage("There is no data to be displayed, proceed to Get Readings?")
        builder.setPositiveButton("YES"){
                dialog, which -> openCamera(false)
        }

        builder.setNegativeButton("NO"){
                dialog,which ->
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun export_to_csv(){


        val jsonfile = filesDir.absolutePath + "/UnitJson.json"
        val file = File(jsonfile)

        val fileName = filesDir.absolutePath + "/Udometer.csv"
//        val writer = Files.newBufferedWriter(Paths.get(fileName))

        val writer = FileWriter(fileName, false)
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("Block", "Floor", "ID", "Reading","Date","Time"))

        if (file.exists()){

            val inputStream: InputStream = File(jsonfile).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            var regex = Regex("\"Block\":\"(\\S+)\",\"Floor\":(\\S+),\"ID\":(\\S+),\"Reading\":\"(\\S+)\",\"date\":\"(\\S+)\",\"path\":\"(\\S+)\",\"time\":\"(\\S+)\"", RegexOption.MULTILINE)
            var Block = regex.findAll(inputString).map{ result ->result.groups[1]?.value}.toList()
            var Floor = regex.findAll(inputString).map{ result ->result.groups[2]?.value}.toList()
            var ID = regex.findAll(inputString).map{ result ->result.groups[3]?.value}.toList()
            var Reading = regex.findAll(inputString).map{ result ->result.groups[4]?.value}.toList()
            var Date = regex.findAll(inputString).map{ result ->result.groups[5]?.value}.toList()
            var Time = regex.findAll(inputString).map{ result ->result.groups[7]?.value}.toList()



            val len = Block.count()
            for (i in 0..len-1) {
                csvPrinter.printRecord(Block[i], Floor[i],ID[i],Reading[i],Date[i],Time[i])
            }
        }
        else
        {
            csvPrinter.printRecord(null, null, null, null, null,  null)
        }

        csvPrinter.flush()
        csvPrinter.close()

        Toast.makeText(this,"CSV file successfully exported",Toast.LENGTH_SHORT).show()

        val intent = Intent()

        val context=applicationContext
        val fileLocation = File(filesDir,"Udometer.csv")

        val path= FileProvider.getUriForFile(context, "$packageName.fileprovider", fileLocation)
        val mime = contentResolver.getType(path)

        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(path,mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)

    }


    private fun eraseData(){

        val jsonfile = filesDir.absolutePath + "/UnitJson.json"
        val file = File(jsonfile)
        val builder  = AlertDialog.Builder(this)


        if (file.exists()){
            builder.setTitle("Warning!")
            builder.setMessage("Are you sure you want to erase all data?")
            builder.setPositiveButton("YES"){dialog, which ->file.delete()
                Toast.makeText(this,"Delete Successful",Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("NO"){dialog,which ->}
        }
        else{
            builder.setTitle("Alert!")
            builder.setMessage("There are no files to be deleted")
            builder.setNeutralButton("OK"){dialog,which ->}

        }
        val dialog: AlertDialog = builder.create()
        dialog.show()


    }
    private fun shareFunction(){

        val fileLocation = File(filesDir,"Udometer.csv")
        val path= FileProvider.getUriForFile(applicationContext, "$packageName.fileprovider", fileLocation)
        var myIntent = Intent(Intent.ACTION_SEND)
        myIntent.type="text/csv"


        if(fileLocation.exists()){
            myIntent.putExtra(Intent.EXTRA_SUBJECT,"Data")
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            myIntent.putExtra(Intent.EXTRA_STREAM,path)
            startActivity(Intent.createChooser(myIntent,"Share using"))
        }

        else{
            Toast.makeText(this,"There are no files to be shared",Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        private val CAMERA_ACTIVITY_REQUEST = 123
        private val PICK_IMAGE_MULTIPLE = 999
    }
}
