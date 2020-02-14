package com.example.watermeterapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Xml
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_view_record_main.*
import java.io.File
import java.io.InputStream
import java.security.AccessController.getContext
import java.util.logging.XMLFormatter
import android.R.attr.path
import android.content.Context
import android.content.ContentResolver
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.Bitmap




class ViewRecordMain : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_record_main)

       /* val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path, "DemoPicture.jpg")*/


//        val relativePath = Environment.DIRECTORY_PICTURES + File.separator + "Your Directory" // save directory
//        val fileName = "Your_File_Name" // file name to save file with
//        val mimeType = "image/*" // Mime Types define here
//        val bitmap: Bitmap? = null // your bitmap file to save
//
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
////        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
////        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
//
//
//        val resolver = this.getContentResolver()


/*
        System.out.println("why am i still alive222222222222"+ file.toString())
*/


        initListener()
        /*OKBtn.setOnClickListener{
            System.out.println("\"Block\":\"A\",\"Floor\":1,\"ID\":1,\"Reading\":\"\\S+\",\"date\":\"date\"")
        }*/

//        var fileName = filesDir.absolutePath + "/UnitJson.json"
//
//        val inputStream: InputStream = File(fileName).inputStream()
//
//        // Read the text from buffferReader and store in String variable
//        val inputString = inputStream.bufferedReader().use { it.readText() }
//
//        var regex = Regex("\"Block\":\"("+block_Result+")\",\"Floor\":("+floor_Result+"),\"ID\":("+unitId_Result+"),\"reading\":\"(\\S+)\",\"timeStamp\":\"(\\S+)\"", RegexOption.MULTILINE)
//        var result =regex.findAll(inputString).map{ result -> result.value }.toList()
//        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result)
//        lv.setAdapter(adapter)
//
//
//        System.out.println(result)
//        System.out.println("\"Block\":\"("+block_Result+")\",\"Floor\":("+floor_Result+"),\"ID\":("+unitId_Result+"),\"reading\":\"(\\S+)\",\"timeStamp\":\"(\\S+)\"")
//

    }

    private fun initListener()
    {
        val block:EditText= findViewById(R.id.blockInput_ViewRecord)
        val floor:EditText= findViewById(R.id.floorInput_ViewRecord)
        val unit:EditText= findViewById(R.id.unitInput_ViewRecord)
        val date:EditText= findViewById(R.id.dateInput_ViewRecord)
        val listView:ListView= findViewById(R.id.readings_ListView)

        val viewRecordInputChangeListener = ViewRecordInputChangeListener(this, block, floor, unit,date, listView)

        block.addTextChangedListener(viewRecordInputChangeListener)
        floor.addTextChangedListener(viewRecordInputChangeListener)
        unit.addTextChangedListener(viewRecordInputChangeListener)
        date.addTextChangedListener(viewRecordInputChangeListener)
    }
//
//
//     class LashCustomTextWatcher(val adapter: ArrayAdapter<String>) :TextWatcher{
//        val funct=ViewRecordMain()
//
//         override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//            adapter.getFilter().filter(funct.lashCalculate())
//
//            System.out.println("block:"+funct.block)
//            System.out.println("unit:"+funct.unit)
//            System.out.println("floor:"+funct.floor)
//        }
//         override fun afterTextChanged(editable: Editable) {}
//    }
//
//
//    fun lashCalculate():String{
//
//        var Block=""
//        var Floor=0
//        var Unit=0
//
//        if(!block?.text.toString().equals("") && !floor?.text.toString().equals("")&&!unit?.text.toString().equals("")){
//            var BLOCK:String?="A"
//            var FLOOR:Int?=1
//            var UNIT:Int?=1
//
//            Block= BLOCK.toString()
//            Floor= FLOOR!!
//            Unit= UNIT!!
//        }
//        System.out.println("lushhhhhhhhyyyyyyyyyyyyyyBLOCK:"+ Block)
//        System.out.println("lushhhhhhhhyyyyyyyyyyyyyyFLOOR:"+ Floor)
//        System.out.println("lushhhhhhhhyyyyyyyyyyyyyyUNIT:"+ Unit)
//        return (Block+Floor+Unit)
//    }
//

}


