package com.mmu.fyp.xiang.watermetercamera

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }
}