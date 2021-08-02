package com.example.guru2project

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.annotation.RequiresApi

class SettingTimeActivity : AppCompatActivity() {

    private var setHour : Int = 0
    private var setMinute : Int = 0

    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var btnTimeSet: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_time)

        hourSpinner = findViewById(R.id.hour_spinner)
        minuteSpinner = findViewById(R.id.minute_spinner)
        btnTimeSet = findViewById(R.id.btnSetTime)

        //hourSpinner.setSelection(0) 원하는 포지션 이동(나중에 추가)

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
            //dlg.setTitle("권한이 필요한 이유")
            dlg.setMessage("${setHour}시간 ${setMinute}분으로 설정하시겠습니까?")
            dlg.setPositiveButton( "확인") { dialog, which ->
                startActivity(Intent(this, LeftTime::class.java))
                finish()
            }
            dlg.setNegativeButton("취소", null)
            dlg.show()

        }






    }
}