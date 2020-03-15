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
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.mmu.fyp.xiang.watermetercamera.`interface`.WaterMeterImageAnalyserListener
import com.mmu.fyp.xiang.watermetercamera.utils.ImageFileIO
import com.mmu.fyp.xiang.watermetercamera.utils.WaterMeterImageAnalyser


class GetReadings : AppCompatActivity(), WaterMeterImageAnalyserListener {
    private var stringBuilder: StringBuilder? = null

    private lateinit var imagePaths: ArrayList<String>

    private lateinit var imageView: ImageView

    private val waterMeterImageAnalyser = WaterMeterImageAnalyser(this)

    private lateinit var meterReadingEditText: EditText
    private lateinit var unitEditText: EditText
    private lateinit var blockEditText: EditText
    private lateinit var floorEditText: EditText

    private val imageFileIO = ImageFileIO(this)

    private var doneAnalyse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_readings)

        imageView = findViewById(R.id.image_view)
        meterReadingEditText = findViewById(R.id.mReading_result)
        blockEditText = findViewById(R.id.block_Result)
        unitEditText = findViewById(R.id.unit_result)
        floorEditText = findViewById(R.id.floor_Result)

        //init with 0
        resetEditTexts()

        imagePaths = intent.getSerializableExtra("imagePaths") as ArrayList<String>

        doneAnalyse = false
        //begin analyse first
        waterMeterImageAnalyser.analyseImage(imagePaths.first())


//        // Get the file Location and name where Json File are get stored


        button_confirm.setOnClickListener {
            if(doneAnalyse)
            {
                val imagePath = imageFileIO.saveBitmap(imageView.drawable.toBitmap())
                imageFileIO.sendImageToGallery()
                val fileName = filesDir.absolutePath + "/UnitJson.json"
                //call write Json method
                writeJSONtoFile(fileName, "path"/*,encodedImage*/)
                //Read the written Json File
                //readJSONfromFile(fileName)

                Toast.makeText(applicationContext, "Data Saved", Toast.LENGTH_LONG).show()

                if(imagePaths.isEmpty())
                {
                    finish()
                    //after saving will also navigate to other page
//                    val intent2 = Intent(this, HomePage::class.java)
//                    startActivity(intent2)
                }
                else
                {
                    waterMeterImageAnalyser.analyseImage(imagePaths.first())
                }
            }

        }

    }

    private fun resetEditTexts()
    {
        //init
        meterReadingEditText.setText("00000")
        blockEditText.setText("ST1")
        unitEditText.setText("01")
        floorEditText.setText("01")
    }

    override fun onWaterMeterImageAnalyseDone(
        isValidWaterMeterImage: Boolean,
        waterMeterBitmap: Bitmap?,
        serialNo: String,
        meterReading: String
    ) {
        if(!isValidWaterMeterImage)
        {
            Toast.makeText(applicationContext, "No meter detected from this image.", Toast.LENGTH_LONG).show()
        }
        Log.d("", "$serialNo $meterReading")
        //remove first path from array
        imagePaths.removeAt(0)

        imageView.setImageBitmap(waterMeterBitmap)

        meterReadingEditText.setText(meterReading)
        val serialNoTexts = serialNo.split('-')
        if(serialNoTexts.size == 3)
        {
            blockEditText.setText(serialNoTexts[0])
            floorEditText.setText(serialNoTexts[1])
            unitEditText.setText(serialNoTexts[2])
        }

        doneAnalyse = true
    }


    private fun writeJSONtoFile(s: String, imagePath: String/*,Image: String*/) {
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
        var unit = Unit(unit_Result, block_Result,floor_Result, reading_Result, currentDate, imagePath, currentTime/*,Image*/)

        //var buildingTest=Building(parameters)

        //Convert the Json object to JsonString
        var jsonString:String = gson.toJson(unit)

        //Initialize the File Writer and write into file
        val file = File(s)
        //file.writeText(jsonString)
        Log.d("TAG",
            "$unit_Result $block_Result $floor_Result $reading_Result $currentDate $imagePath $currentTime"
        )

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