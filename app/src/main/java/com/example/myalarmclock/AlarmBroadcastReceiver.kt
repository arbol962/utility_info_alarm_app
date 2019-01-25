package com.example.myalarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AlarmBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?){
        //context?.toast("アラームを受信しました")

//        val intent = Intent(context, MainActivity::class.java)
//            .putExtra("onReceive", true)
//            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context?.startActivity(intent)
        val intent = context?.intentFor<MainActivity>("onReceive" to true)
        intent?.newTask()
        context?.startActivity(intent)
    }
}
