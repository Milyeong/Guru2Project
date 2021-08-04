package com.example.guru2project

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

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
                goalHours = ( (setHour.toLong() * 60) + setMinute.toLong() ) *60*60
                val pref = getSharedPreferences("pref", MODE_PRIVATE)
                val editor = pref.edit()
                editor.putLong("GOAL_HOURS", goalHours)
                editor.apply()
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