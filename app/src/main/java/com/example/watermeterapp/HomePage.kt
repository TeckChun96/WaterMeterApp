package com.example.watermeterapp

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
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat
import java.io.*
import java.security.AccessController.getContext
import java.util.*


class HomePage : AppCompatActivity() {


    private val PERMISSION_CODE_IMAGE_CAPTURE=1000
    private val IMAGE_CAPTURE_CODE=1001
    private val GALLERY_PICK_CODE=1002
    private val PERMISSION_CODE_GALLERY_PICK=1003
    private val PICK_IMAGE_MULTIPLE=1004
    private val PERMISSION_CODE_PICK_IMAGE_MULTIPLE=1005


    var image_uri: Uri?=null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)


        //this two buttons is to test pick one and pic k multiple images from gallery
/*
        button_single.setOnClickListener{
            *//*val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(path, "1578649482562.jpg")
            val uri = Uri.fromFile(file)
            imageview.setImageURI(uri)*//*

            *//*val pickPhoto = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, PERMISSION_CODE)*//*

            //check runtime permission
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_DENIED){
                    //permission not enabled
                    val permission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permission,PERMISSION_CODE_GALLERY_PICK)
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{

            }

        }

        button_multiple.setOnClickListener{
            //check runtime permission
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_DENIED){
                    //permission not enabled
                    val permission=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permission,PERMISSION_CODE_PICK_IMAGE_MULTIPLE)
                }
                else{
                    //permission already granted
                    pickMultipleImageFromGallery()
                }
            }
            else{

            }

        }*/


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
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.CAMERA)
                    ==PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_DENIED){
                //permission not enabled
                    val permission=arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission,PERMISSION_CODE_IMAGE_CAPTURE)
                }
                else{
                    //permission alrdy granted
                    openCamera()
                }
                }
            else{
            }
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE_IMAGE_CAPTURE->{
                if(grantResults.size>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED ){
                    //permission from popup was granted
                    openCamera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()

                }
            }
            PERMISSION_CODE_GALLERY_PICK->{
             if(grantResults.size>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                //permission from popup was granted
                pickImageFromGallery()
            }

             else{
                 //permission from popup was denied
                 Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()

             }
            }
            PERMISSION_CODE_PICK_IMAGE_MULTIPLE->{
                if(grantResults.size>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED ){
                    //permission from popup was granted
                    pickMultipleImageFromGallery()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()

                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //called when image was captured from camera intent
        if(resultCode== Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE){
            //set image captured to image view
            val intent= Intent(this,GetReadings::class.java)
                intent.putExtra("ImgPath",image_uri.toString())
                startActivity(intent)
        }

        //These codes are for activity result for pick one or pick multiple images from gallery
        /*if(resultCode== Activity.RESULT_OK && requestCode == GALLERY_PICK_CODE){
            //set image captured to image view
            var imgview = findViewById<ImageView>(R.id.imageView1)
            imgview.setImageURI(data?.data)
        }

        if(resultCode== Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE){
            //set image captured to image view
            var clipData = data?.getClipData()
            var imgview1 = findViewById<ImageView>(R.id.imageView1)
            var imgview2 = findViewById<ImageView>(R.id.imageView2)

            if(clipData!=null) {
                imgview1.setImageURI(clipData.getItemAt(0).getUri())
                imgview2.setImageURI(clipData.getItemAt(1).getUri())

                for (i in 0 until clipData.getItemCount())
                {
                    val item = clipData.getItemAt(i)
                    val uri = item.getUri()
                }

            }
            else{
                var imageUri = data?.getData()
                imgview1.setImageURI(imageUri)
            }
        }*/

    }

    //pick one from gallery function
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_PICK_CODE)
    }

    //pick multiple from gallery function
    private fun pickMultipleImageFromGallery(){
        //Intent to pick image
        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_MULTIPLE)
    }

    private fun openCamera(){
        val values= ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the Camera")
        image_uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        //camera intent
        val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri)
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)


    }

    private fun openDialog(){

        val builder  = AlertDialog.Builder(this)

        builder.setTitle("Alert!")
        builder.setMessage("There is no data to be displayed, proceed to Get Readings?")
        builder.setPositiveButton("YES"){dialog, which ->
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.CAMERA)
                    ==PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_DENIED){
                    //permission not enabled
                    val permission=arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission,PERMISSION_CODE_IMAGE_CAPTURE)
                }
                else{
                    //permission alrdy granted
                    openCamera()
                }
            }
            else{
            }
        }

        builder.setNegativeButton("NO"){dialog,which ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun export_to_csv(){


        val jsonfile = filesDir.absolutePath + "/UnitJson.json"
        val file = File(jsonfile)

        val fileName = filesDir.absolutePath + "/Udometer.csv"
        val writer = Files.newBufferedWriter(Paths.get(fileName))
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

        else{
            csvPrinter.printRecord(null, null,null,null, null,null)
        }

       /* val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput("Udometer.csv", Context.MODE_PRIVATE)
            fileOutputStream.write(data.toString().toByteArray())

        } catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: NumberFormatException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }catch (e: Exception){
            e.printStackTrace()
        }*/
        csvPrinter.flush()
        csvPrinter.close()

        Toast.makeText(this,"CSV file successfully exported",Toast.LENGTH_SHORT).show()



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


        val context=applicationContext
        val fileLocation = File(filesDir,"Udometer.csv")
        val path= FileProvider.getUriForFile(context, "com.example.watermeterapp.fileprovider", fileLocation)
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


        /*val fileName = filesDir.absolutePath + "/Udometer.csv"
        var file = File(fileName)
//        var myUri = Uri.parse(file.absolutePath)

        var myUri = FileProvider.getUriForFile(this, "com.example.watermeterapp.myfileprovider", file)

        var myIntent = Intent(Intent.ACTION_SEND)
        myIntent.type="text/csv"
        var shareBody = "Your Body here"
        var shareSub = myUri

        if(file.exists()){

            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody)
            myIntent.putExtra(Intent.EXTRA_STREAM,shareSub)
            startActivity(Intent.createChooser(myIntent,"Share using"))
        }


        else{
            Toast.makeText(this,"There are no files to be shared",Toast.LENGTH_SHORT).show()
        }*/


    }

    }
