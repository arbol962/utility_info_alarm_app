package com.example.myalarmclock

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import org.jetbrains.anko.toast
import java.util.*

class SimpleAlertDialog : DialogFragment(), TextToSpeech.OnInitListener{

    private lateinit var player: MediaPlayer

    interface OnClickListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    private lateinit var listener: OnClickListener

    override fun onAttach(context: Context?){
        super.onAttach(context)
        if (context is SimpleAlertDialog.OnClickListener) {
            listener = context
        }
    }
    private var tts : TextToSpeech? = null
    override fun onResume() {
        //　ダイアログ起動時の処理
        super.onResume()

        tts = TextToSpeech(context, this)
        var langLocale : Locale = Locale.JAPANESE
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR).toString()
        val month = c.get(Calendar.MONTH).toString()
        val date = c.get(Calendar.DAY_OF_MONTH).toString()
        val hour = c.get(Calendar.HOUR).toString()
        val minute = c.get(Calendar.MINUTE).toString()

        val weeks = arrayOf("月","火","水","木","金","土","日")
        val week = weeks[c.get(Calendar.DAY_OF_WEEK)]


        val wiki_url = "https://ja.wikipedia.org/w/api.php?format=json&utf8&action=query&prop=revisions&rvprop=content&titles=${month}月${date}日"
        val pref = intent.getIntExtra("")


        if(input.isNotBlank()){
                speakText(langLocale, input)
            }



        player = MediaPlayer.create(context, R.raw.getdown)
        player.isLooping = false
        player.start()
    }

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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
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