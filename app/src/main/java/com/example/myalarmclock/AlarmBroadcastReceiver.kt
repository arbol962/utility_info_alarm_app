package com.example.myalarmclock

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.speech.tts.TextToSpeech
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



public var wikiResult = ""
public var weatherResult = ""
@TargetApi(Build.VERSION_CODES.LOLLIPOP)

class AlarmBroadcastReceiver: BroadcastReceiver(),TextToSpeech.OnInitListener{
    lateinit var player: MediaPlayer
    var tts : TextToSpeech? = null
    var prefDic = hashMapOf("北海道" to "sapporo", "青森県" to "aomori", "岩手県" to "morioka", "宮城県" to "sendai", "秋田県" to "akita"
        , "山形県" to "yamagata", "福島県" to "fukushima", "茨城県" to "mito", "栃木県" to "utsunomiya", "群馬県" to "maebashi"
        , "埼玉県" to "saitama", "千葉県" to "chiba", "東京都" to "shinjuku", "神奈川県" to "yokohama",  "新潟県" to "niigata"
        , "富山県" to "toyama",  "石川県" to "ishikawa", "福井県" to "fukui",  "山梨県" to "yamanashi", "長野県" to "nagano"
        , "岐阜県" to "gifu-shi", "静岡県" to "shizuoka", "愛知県" to "aichi", "三重県" to "tsu", "滋賀県" to "ootsu"
        , "京都府" to "kyoto", "大阪府" to "oosaka",  "兵庫県" to "kobe", "奈良県" to "nara",  "和歌山県" to "wakayama"
        , "鳥取県" to "tottori",  "島根県" to "matsue", "岡山県" to "okayama", "広島県" to "hiroshima", "山口県" to "yamaguchi"
        , "徳島県" to "tokushima",  "香川県" to "takamatsu", "愛媛県" to "matsuyama",  "高知県" to "kouchi", "福岡県" to "fukuoka"
        , "佐賀県" to "saga", "長崎県" to "nagasaki",  "熊本県" to "kumamoto", "大分県" to "ooita", "宮崎県" to "miyazaki"
        , "鹿児島県" to "kagoshima", "沖縄県" to "naha")

    override fun onReceive(context: Context?, intent: Intent?) {
        //context?.toast("アラームを受信しました")

//        val intent = Intent(context, MainActivity::class.java)
//            .putExtra("onReceive", true)
//            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context?.startActivity(intent)
        val intent = context?.intentFor<MainActivity>("onReceive" to true)
        val prefecture = intent?.getStringExtra("pref")
        intent?.putExtra("pref", prefecture)

        //　日時取得
        val date: Date = Date()
        val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)


        // wikipedia APIをたたく
        var wikiUrl =
            "https://ja.wikipedia.org/w/api.php?format=json&utf8&action=query&prop=revisions&rvprop=content&titles="
        val month = (calendar.get(Calendar.MONTH).toString().toInt() +1).toString()
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val weekList = arrayOf("","日","月","火","水","木","金","土")
        val week = weekList[calendar.get(Calendar.DAY_OF_WEEK)]
        wikiUrl += month + "月" + day + "日"
        APITask().execute(wikiUrl)
        Thread.sleep(100)
        while(wikiResult == ""){
            continue
        }


        //val parentJsonObj = JSONObject(wikiResult)
        val parser: Parser = Parser()
        Log.d("TAG","予定通り1")
        var parentJsonObj = parser.parse(wikiResult) as JsonObject
        Log.d("TAG",((parentJsonObj.get("query") as JsonObject).get("pages")as JsonObject).toString())
        Log.d("TAG","予定通り2")

        //val parentJsonObj = JSONArray(wikiResult)
        //val parentJsonObj = JSONArray(wikiResult)

//        val queryJsonArray = parentJsonObj.getJSONArray("query")
//        //ここでエラー
//        val pagesJsonArray = queryJsonArray.getJSONArray(0)
//        Log.d("TAG","予定通り2")
//
//        //val pagesJsonArray = parentJsonArray.getJSONArray(0)
//        val pageNumJsonArray = pagesJsonArray.getJSONArray(0)
//        val revisionsJsonArray = pageNumJsonArray.getJSONArray(3)
//        val zeroJsonObj = revisionsJsonArray.getJSONObject(0)
//        var today_anniv: String = zeroJsonObj.getString("*")
        var today_anniv = "hogehoge"

        today_anniv = today_anniv.substring(today_anniv.indexOf("記念日")+16, today_anniv.indexOf("フィクションのできごと"))
        today_anniv = today_anniv.replace("[", "")
        today_anniv = today_anniv.replace("]", "")
        today_anniv = today_anniv.replace("<!--", "")
        today_anniv = "*  $today_anniv"
        var anniv_list = today_anniv.split(Regex("\n"))

        var anniversaries = ""
        var count = 0
        for (i in anniv_list){
            if(anniv_list[count].substring(0,2) == "* ")
                anniversaries += anniv_list[count] + "。"
            count++
        }

        // お天気API
        val weatherLocale = prefDic[prefecture]
        val weatherAPIkey = "6f74e27163559007ed70228d2f3b50b8"
        var weatherUrl = "http://api.openweathermap.org/data/2.5/forecast"

        weatherUrl += "?q=$weatherLocale,jp&units=metric&cnt=1&APPID=$weatherAPIkey"
        //val weatherResult = weatherUrl.httpGet().responseJson().toString()
        APITask().execute(weatherUrl)

        val weatherJsonObj = JSONObject(weatherResult)
        val weatherList = weatherJsonObj.getJSONArray("list")
        // 天気予報取得
        val weatherWeather = weatherList.getJSONObject(2)
        var weatherDescription = weatherWeather.getString("description")
        val forecast = when(weatherDescription) {
            "clear sky" -> "快晴"
            "few clouds" -> "晴れ"
            "scattered clouds" -> "くもり"
            "broken clouds" -> "くもり"
            "shower rain" -> "小雨"
            "rain" -> "雨"
            "thunderstorm" -> "雷雨"
            "snow" -> "雪"
            "mist" -> "霧"
            else -> "不明"
        }


        // 最高,最低気温取得
        val weatherMain = weatherList.getJSONObject(1)
        val temp = weatherMain.getString("temp")

        val today = "本日は"+month+"月"+day+"日"+week+"曜日です。"
        val weather =  prefecture + "の天気は" +forecast+ "。現在の気温は"+temp +"です。"
        val speechText = today + weather + today_anniv
        speakText(Locale.JAPANESE,speechText)

        intent?.newTask()
        context?.startActivity(intent)
    }

//    private fun apiWikiFetch{
//        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
//        val wikiUrl = "https://ja.wikipedia.org/w/api.php?format=json&utf8&action=query&prop=revisions&rvprop=content&titles="
//        val retrofit = Retrofit.Builder()
//            .baseUrl(wikiUrl)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//            .build()
//        val wikiItem = retrofit.create(WikiItemInterface::class.java)
//        wikiItem.items("")
//            .subscribeOn(Schedulers.io())
//            .subscribe({
//                for (test in it) {
//                    Log.i(TAG, String.format("%s", test))
//                }
//            }, {
//                Log.w(TAG, "")
//            })
//    }


    private fun speakText(langLocale: Locale, text:String){
        tts!!.setLanguage(langLocale)
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH, null, "speech1")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("TTS", "TextToSpeechが初期化されました。")

            // 音声再生のイベントリスナを登録
            val listener : SpeechListener? = SpeechListener()
            tts!!.setOnUtteranceProgressListener(listener)
        } else {
            Log.e("TTS", "TextToSpeechの初期化に失敗しました。")
        }
    }

    inner class APITask : AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String? {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            var buffer: StringBuffer

            try {
                val url = URL(params[0])
                connection = url.openConnection() as HttpURLConnection

                connection.connect()

                val stream = connection.inputStream
                reader = BufferedReader(InputStreamReader(stream))
                buffer = StringBuffer()
                var line: String?

                while (true) {
                    line = reader.readLine()
                    if (line == null) {
                        break
                    }
                    buffer.append(line)

                }
                var jsonText = buffer.toString()
                Log.d("TAG",params[0]?.substring(0,20))
                if (params[0]?.substring(0,20) == "https://ja.wikipedia"){
                    wikiResult = jsonText
                }
                else{
                    weatherResult = jsonText
                    Log.d("TAG","なんかあった")
                }

            }catch (e: MalformedURLException) {
                Log.d("TAG","通過しました1")
                e.printStackTrace()
            } catch (e: IOException) {
                Log.d("TAG","通過しました2")
                Log.d("DEBUG",e.message)
                e.printStackTrace()
            } catch (e: JSONException) {
                Log.d("TAG","通過しました3")
                e.printStackTrace()
            }
            // 接続切断
            finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return null
        }
    }
}
class WikiResultGetter(result: String){
    val wikiResult:String = result
}
class WeatherResultGetter(result: String){
    val weatherResult:String = result
}
