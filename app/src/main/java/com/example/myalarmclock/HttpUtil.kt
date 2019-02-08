package com.example.myalarmclock

import android.app.DownloadManager
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpUtil {
    //　引数：叩きたいAPIのURL
    fun httpGET(url : String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        return response.body()?.string()
    }
}