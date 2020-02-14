package com.example.watermeterapp


import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import android.appwidget.AppWidgetManager



class ViewRecordInputChangeListener: TextWatcher {

    val viewRecordMain: ViewRecordMain
    val blockEditTextView: EditText
    val floorEditTextView: EditText
    val unitEditTextView: EditText
    val dateEditText : EditText
    val listView: ListView
    val waterMeterReadingList: WaterMeterReadingList


    constructor(viewRecordMain: ViewRecordMain, blockEditTextView: EditText, floorEditTextView: EditText, unitEditTextView: EditText,dateEditText:EditText, listView: ListView) : super()
    {
        this.viewRecordMain =  viewRecordMain
        this.blockEditTextView = blockEditTextView
        this.floorEditTextView = floorEditTextView
        this.unitEditTextView = unitEditTextView
        this.dateEditText=dateEditText
        this.listView = listView

        this.waterMeterReadingList = WaterMeterReadingList()
    }


    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int){

    }
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
    {
        var block: String = this.blockEditTextView.text.toString()
        var floor: String = this.floorEditTextView.text.toString()
        var unit: String = this.unitEditTextView.text.toString()
        var date: String = this.dateEditText.text.toString()

        block = if(block.isNullOrEmpty()) "\\S+" else block
        floor = if(floor.isNullOrEmpty()) "\\S+" else floor
        unit = if(unit.isNullOrEmpty()) "\\S+" else unit
        date= if (date.isNullOrEmpty()) "\\S+" else date

        var readingList: ArrayAdapter<String> = this.waterMeterReadingList.getWaterMeterReadingList(this.viewRecordMain, block, floor, unit,date)

        /*var a=readingList.toString()
        var b= arrayOf(a)*/

        this.listView.adapter = readingList

        /*this.listView.setOnItemClickListener(AdapterView.OnItemClickListener(){ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            val item = listView.getItemAtPosition(i)
            System.out.println("this is the item "+item.toString())

            Toast.makeText(view1.context, "item pos at" + item, Toast.LENGTH_LONG).show()

            val intent2 = Intent(view1.context, ViewRecordList::class.java)
            intent2.putExtra("item",item.toString())
            view1.context.startActivity(intent2)
        })*/
    }
    override fun afterTextChanged(editable: Editable) {}
}