package com.example.watermeterapp

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import kotlinx.android.synthetic.main.activity_get_readings.*
import android.widget.EditText
import android.widget.Toast
import java.io.*
import com.google.gson.Gson
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.ETC1.encodeImage
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.path






class GetReadings : AppCompatActivity() {
    private var stringBuilder: StringBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_readings)

        var cursor: Cursor? = null

        val intent = intent
        val imagePath = intent.getStringExtra("ImgPath")
        val fileUri = Uri.parse(imagePath)
        image_view.setImageURI(fileUri)

        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = contentResolver.query(fileUri, proj, null, null, null)
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path= cursor?.getString(column_index!!)

       /* var test = getContentResolver().openInputStream(fileUri)*/
        //output=//media/external/images/media/2528

/*
        val encodedImage = encodeImage(selectedImage)
*/
        // Get the file Location and name where Json File are get stored
        val fileName = filesDir.absolutePath + "/UnitJson.json"

        button_confirm.setOnClickListener {

            //call write Json method
            writeJSONtoFile(fileName, path.toString()/*,encodedImage*/)
            //Read the written Json File
            //readJSONfromFile(fileName)

            System.out.println("imagepath:=>"+path)
            Toast.makeText(applicationContext, "Data Saved", Toast.LENGTH_LONG).show()

            //after saving will also navigate to other page
            val intent2 = Intent(this, HomePage::class.java)
            startActivity(intent2)
        }

    }


    private fun writeJSONtoFile(s: String,path: String/*,Image: String*/) {
        var bufferedWriter: BufferedWriter? = null

        //This is the save function in the result data page
        val unit_result = findViewById<EditText>(R.id.unit_result)
        val mReading = findViewById<EditText>(R.id.mReading_result)
        val block_num = findViewById<EditText>(R.id.block_Result)
        val floor_num = findViewById<EditText>(R.id.floor_Result)

        val unit_Result: Int = unit_result.text.toString().toInt()
        val block_Result: String = block_num.text.toString()
        val floor_Result: Int = floor_num.text.toString().toInt()
        val reading_Result: String = mReading.text.toString()

        //Auto- generate date
        val df = SimpleDateFormat("yyyy.MM.dd")
        val currentDate = df.format(Calendar.getInstance().time)
        val tf = SimpleDateFormat("h:mma")
        val currentTime = tf.format(Calendar.getInstance().time)


        //Create a Object of Gson
        var gson = Gson()

        //Create a Object of Unit
        //Instead of hard-coding the values into Unit() //Unit refers to the Unit class
        //We replaced them with variables instead  //Initially it was Unit("hard-code value","hard-code value",tags)
        //So that it will store what the user typed in the app
        var unit = Unit(unit_Result,block_Result,floor_Result, reading_Result,currentDate,path,currentTime/*,Image*/)

        //var buildingTest=Building(parameters)

        //Convert the Json object to JsonString
        var jsonString:String = gson.toJson(unit)

        //Initialize the File Writer and write into file
        val file = File(s)
        //file.writeText(jsonString)


        try {

            FileOutputStream(file,true).bufferedWriter().use { out -> out.write((jsonString+"\n")) }
            //fileOutputStream.write(data.toByteArray())

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()

        return Base64.encodeToString(b, Base64.DEFAULT)
    }


    /* private fun readJSONfromFile(f:String){


        //Creating a new Gson object to read data
        var gson = Gson()
        //Read the PostJSON.json file
        val bufferedReader: BufferedReader = File(f).bufferedReader()
        // Read the text from buffferReader and store in String variable
        //val inputString = bufferedReader.use { it.readText() }

         var line: String? = null
         while({ line = bufferedReader.readLine(); line }() != null) {

            var unit = gson.fromJson(line, Unit::class.java)
             Log.i("TAG", unit.Block)
         }

     }
*/

}