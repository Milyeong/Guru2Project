package com.example.guru2project

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class SettingTimeActivity : AppCompatActivity() {


    private var setHour : Int = 0
    private var setMinute : Int = 0

    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var btnTimeSet: Button

    var goalHours : Long =0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_time)


        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val editor = pref.edit()
        // 오늘 이미 실행했을 때
        if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains(currentDate)) {

            //시간을 설정했을때
            if (pref.getLong("GOAL_HOURS", 0)>0){
                val intent = Intent(this, LeftTime::class.java)
                startActivity(intent)
                finish()
            } else{ //시간을 설정 안했을때
                val intent = Intent(this, SettingTimeActivity::class.java)
                startActivity(intent)
                finish()
            }

        } else {//오늘 처음 실행했을때

            // 설정시간 초기화
            editor.putLong("GOAL_HOURS", 0)
            editor.putString("LAST_LAUNCH_DATE", currentDate)
            editor.apply()

            // 어제 사용기록 가져온 후 어제 목표(데이터베이스에서 가져오기)보다 작으면 적립(함수로 구현)
        }


        hourSpinner = findViewById(R.id.hour_spinner)
        minuteSpinner = findViewById(R.id.minute_spinner)
        btnTimeSet = findViewById(R.id.btnSetTime)


        //시간 선택
        hourSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setHour = hourSpinner.getItemAtPosition(position).toString().toInt()
            }
        }
        //분 선택
        minuteSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setMinute = minuteSpinner.getItemAtPosition(position).toString().toInt()
            }
        }

        //시간설정 확인
        btnTimeSet.setOnClickListener {
            var dlg = AlertDialog.Builder(this)
            dlg.setMessage("${setHour}시간 ${setMinute}분으로 설정하시겠습니까?")
            dlg.setPositiveButton( "확인") { dialog, which ->
                //목표시간 pref에 저장
                goalHours = ( (setHour.toLong() * 60 ) + setMinute.toLong() ) *60*1000
                val pref = getSharedPreferences("pref", MODE_PRIVATE)
                val editor = pref.edit()
                //editor.clear()
                editor.putLong("GOAL_HOURS", goalHours)
                editor.apply()
                editor.commit()
                //LeftTime으로
                val intent = Intent(this, LeftTime::class.java)
                startActivity(intent)
                finish()
            }
            dlg.setNegativeButton("취소", null)
            dlg.show()

        }

    }


        /*@SuppressLint("PrivateApi")
        fun TimePicker.setTimeInterval(
                timeInterval: Int = DEFAULT_INTERVAL
        ) {
            try {
                val classForId = Class.forName("com.android.internal.R\$id")
                val fieldId = classForId.getField("minute").getInt(null)

                (this.findViewById(fieldId) as NumberPicker).apply {
                    minValue = MINUTES_MIN
                    maxValue = MINUTES_MAX / timeInterval - 1
                    displayedValues = getDisplayedValue(timeInterval)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }







    private fun getDisplayedValue(timeInterval: Int = DEFAULT_INTERVAL): Array<String> {
        val minutesArray = ArrayList<String>()
        for (i in 0 until MINUTES_MAX step timeInterval) {
            minutesArray.add(i.toString())
        }

        return minutesArray.toArray(arrayOf(""))
    }

    private fun TimePicker.getDisplayedMinute(

            timeInterval: Int = DEFAULT_INTERVAL
    ): Int = minute * timeInterval*/
}