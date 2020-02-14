package com.example.watermeterapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_get_readings.*
import java.io.*

class ViewRecordList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_record_list)

        /*val intent=intent
        val item =intent.getStringExtra("item")

        val Etext=findViewById<EditText>(R.id.mreading_view_record_list)
        Etext.setText(item)*/

        /*var array = arrayOf()

        val adapter = ArrayAdapter(this,
            R.layout.unitlayoutdesign, array
        )

        val listView: ListView = findViewById(R.id.view_reading_list)
        listView.setAdapter(adapter)*/

        //val btnView = findViewById<Button>(R.id.button_viewReadings)


        /*btnView.setOnClickListener(View.OnClickListener {
            val Unit = unit_result.text.toString()
            if(Unit.toString()!=null && Unit.toString().trim()!=""){
                var fileInputStream: FileInputStream? = null
                fileInputStream = openFileInput(Unit)
                var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder: StringBuilder = StringBuilder()
                var text: String? = null
                while ({ text = bufferedReader.readLine(); text }() != null) {
                    stringBuilder.append(text)
                }
                //Displaying data on EditText
                mReading_result.setText(stringBuilder.toString()).toString()
            }else{
                Toast.makeText(applicationContext,"file name cannot be blank",Toast.LENGTH_LONG).show()
            }
        })*/
    }
}
