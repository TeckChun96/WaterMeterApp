package com.example.watermeterapp

import android.text.SpannableString
import android.widget.ArrayAdapter
import java.io.File
import java.io.InputStream
import android.widget.TextView





class WaterMeterReadingList{

    val block_value=null
    val floor_value=null
    val id_value=null
    val reading_value=null
    val date_value=null


    constructor()

    fun getWaterMeterReadingList(viewRecordMain: ViewRecordMain, block: String, floor: String, unit: String,date:String):ArrayAdapter<String> {
        var fileName = viewRecordMain.filesDir.absolutePath + "/UnitJson.json"

        val inputStream: InputStream = File(fileName).inputStream()

        // Read the text from buffferReader and store in String variable
        val inputString = inputStream.bufferedReader().use { it.readText() }

        var regex = Regex("\"Block\":\"(?<testblock>"+block+")\",\"Floor\":("+floor+"),\"ID\":("+unit+"),\"Reading\":\"(\\S+)\",\"date\":\"("+date+")\",\"path\":\"(\\S+)\",\"time\":\"(\\S+)\"", RegexOption.MULTILINE)
        var result = regex.findAll(inputString).map{ result ->"Block:"+result.groups[1]?.value +"               Floor:"+ result.groups[2]?.value+"               ID:"+ result.groups[3]?.value+"\nReading:"+ result.groups[4]?.value+"          \nDate:"+ result.groups[5]?.value+"     Time:"+result.groups[7]?.value}.toList()
        var adapter = ArrayAdapter<String>(viewRecordMain, R.layout.listview_meterreadinglist_white, result)


/*

        val test="User Name:first.sur"
        val pattern= "User Name:(?<name>\\w+\\.\\w+)".toRegex()
        val res=pattern.findAll(test)
        res.forEach { ress ->
            println("${ress.groups[1]?.value} matches")
        }

*/

/*

        var testing = Regex("\"Block\":\"(?<testblock>"+block+")\",\"Floor\":("+floor+"),\"ID\":("+unit+"),\"Reading\":\"(\\S+)\",\"date\":\"("+date+")\",\"path\":\"(\\S+)\"", RegexOption.MULTILINE)
        var results = testing.findAll(inputString)

        for (resss in results) {
            val a=resss.groups[1]?.value
            val b=resss.groups[2]?.value
            val c=resss.groups[3]?.value
            val d=resss.groups[4]?.value
            val e=resss.groups[5]?.value

            println("${resss.groups[1]?.value} matches")
            println("${resss.groups[2]?.value} matches")
            println("${resss.groups[3]?.value} matches")
            println("${resss.groups[4]?.value} matches")
            println("${resss.groups[5]?.value} matches")
        }
*/



        return adapter
    }
}