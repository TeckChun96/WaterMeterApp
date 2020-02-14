package com.example.watermeterapp

import android.widget.EditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class Unit (

    /*var building: List<Building>,*/
    var ID:Int,
    var Block:String,
    var Floor:Int,
    var Reading: String,
    var date: String,
    var path:String,
    var time:String


   /* constructor() : super(){}

    constructor(ID: String, Reading: String, TimeStamp: String, tags: List<String>) : super() {
        this.id = ID
        this.reading = Reading
        this.timeStamp = TimeStamp
        this.postTag = tags
        //this.building=Buildings
    }*/

)
