package com.example.myalarmclock

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager.LayoutParams.*
import android.widget.ArrayAdapter
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity()
        , SimpleAlertDialog.OnClickListener
        , DatePickerFragment.OnDateSelectedListener
        , TimePickerFragment.OnTimeSelectedListener {

    public var prefecture = ""

    override fun onSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)

        dateText.text = android.text.format.DateFormat.format("yyy/MM/dd", c)
    }

    override fun onSelected(hourOfDay: Int, minute: Int) {
        timeText.text = "%1$02d:%2$02d".format(hourOfDay, minute)
    }

    override fun onPositiveClick(){
        finish()
    }

    override fun onNegativeClick(){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, 5)
        setAlarmManager(calendar)
        finish()
    }

    private val spinnerItems = arrayOf("北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県"
        , "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県"
        , "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県"
        , "島根県", "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県"
        , "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val spinner = findViewById<Spinner>(R.id.spinner)

        val adapter = ArrayAdapter<String>(applicationContext, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner?.adapter = adapter
        // spinnerリスナーを登録
        spinner?.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
            //　アイテムが選択されたとき
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                prefecture = item
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if(intent?.getBooleanExtra("onReceive", false) == true) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_SHOW_WHEN_LOCKED)

                else ->
                    window.addFlags(FLAG_TURN_SCREEN_ON or FLAG_SHOW_WHEN_LOCKED or FLAG_DISMISS_KEYGUARD)
            }

            val dialog = SimpleAlertDialog()
            dialog.show(supportFragmentManager, "alert_dialog")
        }

        setContentView(R.layout.activity_main)


//        Spinner spinner = (Spinner) findViewById(R.layout.spinner)
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);

        setAlarm.setOnClickListener {
            val date = "${dateText.text} ${timeText.text}".toDate()
            when {
                date != null -> {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    setAlarmManager(calendar)
                    toast("アラームをセットしました")
                }
                else -> {
                    toast("日付の形式が正しくありません")
                }
            }
        }
        cancelAlarm.setOnClickListener {
            cancelAlarmManager()
        }

        dateText.setOnClickListener {
            val dialog = DatePickerFragment()
            dialog.show(supportFragmentManager, "date_dialog")
        }

        timeText.setOnClickListener {
            val dialog = TimePickerFragment()
            dialog.show(supportFragmentManager, "time_dialog")
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setAlarmManager(calendar: Calendar){
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        intent.putExtra("pref", prefecture)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, null)
                am.setAlarmClock(info, pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
            else -> {
                am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            }
        }
    }

    private fun cancelAlarmManager() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        am.cancel(pending)
    }

    fun String.toDate(pattern:String = "yyyy/MM/dd HH:mm") : Date? {
        val sdFormat = try {
            SimpleDateFormat(pattern)
        } catch (e: IllegalArgumentException) {
            null
        }
        val date = sdFormat?.let {
            try {
                it.parse(this)
            } catch (e: ParseException) {
                null
            }
        }
        return date
    }
}
