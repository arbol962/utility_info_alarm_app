package com.example.myalarmclock

import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class SimpleAlertDialog : DialogFragment(),TextToSpeech.OnInitListener{
    private var wikiResult = ""
    private var weatherResult = ""
    private var tts : TextToSpeech? = null
    private var prefDic = hashMapOf("北海道" to "sapporo", "青森県" to "aomori", "岩手県" to "morioka", "宮城県" to "sendai", "秋田県" to "akita"
        , "山形県" to "yamagata", "福島県" to "fukushima", "茨城県" to "mito", "栃木県" to "utsunomiya", "群馬県" to "maebashi"
        , "埼玉県" to "saitama", "千葉県" to "chiba", "東京都" to "shinjuku", "神奈川県" to "yokohama",  "新潟県" to "niigata"
        , "富山県" to "toyama",  "石川県" to "ishikawa", "福井県" to "fukui",  "山梨県" to "yamanashi", "長野県" to "nagano"
        , "岐阜県" to "gifu-shi", "静岡県" to "shizuoka", "愛知県" to "nagoya", "三重県" to "tsu", "滋賀県" to "otsu"
        , "京都府" to "kyoto", "大阪府" to "osaka",  "兵庫県" to "kobe", "奈良県" to "nara",  "和歌山県" to "wakayama"
        , "鳥取県" to "tottori",  "島根県" to "matsue", "岡山県" to "okayama", "広島県" to "hiroshima", "山口県" to "yamaguchi"
        , "徳島県" to "tokushima",  "香川県" to "takamatsu", "愛媛県" to "matsuyama",  "高知県" to "kouchi", "福岡県" to "fukuoka"
        , "佐賀県" to "saga", "長崎県" to "nagasaki",  "熊本県" to "kumamoto", "大分県" to "ooita", "宮崎県" to "miyazaki"
        , "鹿児島県" to "kagoshima", "沖縄県" to "naha")
    interface OnClickListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    private lateinit var listener: OnClickListener
    private fun gatherInfo(){
        val prefecture = arguments?.getString("pref")
        //　日時取得
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
        while(wikiResult == ""){
            continue
        }

        val parentJsonObj = JSONObject(wikiResult)

        val queryObject = JSONObject(parentJsonObj.getString("query"))
        val pagesObject = JSONObject(queryObject.getString("pages"))
        val pageNumJsonObject = JSONObject(pagesObject.getString(pagesObject.keys().next().toString()))

        val revisionsJsonArray = pageNumJsonObject.getJSONArray("revisions")
        val zeroJsonObj = revisionsJsonArray.getJSONObject(0)
        var today_anniv: String = zeroJsonObj.getString("*")

        today_anniv = today_anniv.substring(today_anniv.indexOf("記念日")+16, today_anniv.indexOf("フィクションのできごと"))
        today_anniv = today_anniv.replace("[", "")
        today_anniv = today_anniv.replace("]", "")
        today_anniv = today_anniv.replace("<!--", "")
        today_anniv = "*  $today_anniv"
        // {{JPN}}などの記号を削除する
        Log.d("TAG", today_anniv)
        today_anniv = today_anniv.replace("""\{\{...\}\}""".toRegex(), "")

        val anniv_list = today_anniv.split(Regex("\n"))
        var anniversaries = ""
        for (i in anniv_list){
            if(i != "") {
                when (i.substring(0,2) == "* " || i.substring(0,2) == "*:"){
                    true -> anniversaries += i.substring(2) + "。"
                    false -> anniversaries += i.substring(1) + "。"
                }
            }
        }

        val weatherLocale = prefDic[prefecture]
        val weatherAPIkey = "6f74e27163559007ed70228d2f3b50b8"
        var weatherUrl = "http://api.openweathermap.org/data/2.5/forecast"

        weatherUrl += "?q=$weatherLocale,jp&units=metric&cnt=1&APPID=$weatherAPIkey"
        APITask().execute(weatherUrl)
        while(weatherResult == ""){
            continue
        }
        val weatherJsonObj = JSONObject(weatherResult)
        // 天気予報取得
        val weatherList = JSONArray(weatherJsonObj.getString("list"))
        val listZero = weatherList.getJSONObject(0)
        val weatherZero = listZero.getJSONArray("weather")
        val weatherWeather = weatherZero.getJSONObject(0)
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


        // 気温取得
        val weatherMain = listZero.getJSONObject("main")
        val temp = weatherMain.getString("temp")

        val today = "本日は"+month+"月"+day+"日"+week+"曜日です。"
        val weather =  prefecture + "の天気は" +forecast+ "。現在の気温は"+temp +"です。"

        val speechText = today + weather + anniversaries

        Log.d("TAG",speechText)
        val langLocale: Locale = Locale.JAPANESE
        speakText(langLocale,speechText)
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

    companion object {
        fun newInstance(prefecture: String): SimpleAlertDialog {
            val dialog = SimpleAlertDialog()
            val args = Bundle().apply {
                putString("pref", prefecture)
            }
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        tts = TextToSpeech(context,this)
        val context = context
        thread {
            gatherInfo()
        }
        //処理ここ

        if (context is SimpleAlertDialog.OnClickListener) {
            listener = context
        }

        if (context == null)
            return super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(context).apply {
            setMessage("時間になりました！")
            setPositiveButton("起きる！") { dialog, which ->
                listener.onPositiveClick()
            }
            setNegativeButton("あと5分") { dialog, which ->
                listener.onNegativeClick()
            }
        }
        return builder.create()
    }
    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(21)
    private fun speakText(langLocale: Locale, text:String){
        tts!!.setLanguage(langLocale)
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH, null, "speech1")
    }

    override fun onDestroy() {
        super.onDestroy()
        tts!!.shutdown()
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
                if (params[0]?.substring(0,20) == "https://ja.wikipedia"){
                    wikiResult = jsonText
                }
                else{
                    weatherResult = jsonText
                }

            }catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
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


class DatePickerFragment : DialogFragment(),
            DatePickerDialog.OnDateSetListener {
    interface OnDateSelectedListener {
        fun onSelected(year: Int, month: Int, date: Int)
    }
    private lateinit var listener: OnDateSelectedListener
    override fun onAttach(context: Context?){
        super.onAttach(context)
        if (context is OnDateSelectedListener) {
            listener = context
        }
    }
    override  fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val date = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(context, this, year, month, date)
        }
    override fun onDateSet(view: DatePicker, year: Int, month:Int, date: Int){
        listener.onSelected(year, month,date)
    }
}

class TimePickerFragment: DialogFragment(),
            TimePickerDialog.OnTimeSetListener {

    interface OnTimeSelectedListener {
        fun onSelected(hourOfDay: Int, minute: Int)
    }
    private lateinit var listener: OnTimeSelectedListener
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is TimePickerFragment.OnTimeSelectedListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.HOUR_OF_DAY)
        return TimePickerDialog(context, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute:Int) {
        listener.onSelected(hourOfDay, minute)
    }
}