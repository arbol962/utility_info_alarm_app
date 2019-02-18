package com.example.myalarmclock

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.app.DialogFragment
import android.util.Log
import com.beust.klaxon.Parser
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonParser
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.io.File

import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

class AlarmBroadcastReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        val prefecture = intent?.getStringExtra("pref")
        val intent = context?.intentFor<MainActivity>("onReceive" to true)
        intent?.putExtra("pref", prefecture)
        //intent?.putExtra("pref", prefecture)
        //Log.d("TAG","intentします2")
        intent?.newTask()
        context?.startActivity(intent)
    }
}
