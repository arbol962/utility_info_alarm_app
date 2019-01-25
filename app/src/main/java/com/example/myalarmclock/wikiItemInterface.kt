package com.example.myalarmclock

import android.content.ClipData
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Retrofit
import com.example.myalarmclock.wikiItemInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

interface wikiItemInterface {
    @GET("v2/items.json")
    fun items(): Call<List<ClipData.Item>>
}

fun createService(): wikiItemInterface {
    val baseApiUrl = "https://ja.wikipedia.org/w/api.php?format=json&utf8&action=query&prop=revisions&rvprop=content&titles="

    val httpLogging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogging)

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseApiUrl)
        .client(httpClientBuilder.build())
        .build()

    return retrofit.create(wikiItemInterface::class.java)
}