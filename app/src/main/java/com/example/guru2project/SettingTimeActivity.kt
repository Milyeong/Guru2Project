package com.example.guru2project

import android.app.AlertDialog
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class SettingTimeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private var setHour : Int = 0
    private var setMinute : Int = 0

    private lateinit var drawLayout: DrawerLayout
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var btnTimeSet: Button

    var goalHours : Long =0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_time)

        auth = Firebase.auth
        this.init()

        hourSpinner = findViewById(R.id.hour_spinner)
        minuteSpinner = findViewById(R.id.minute_spinner)
        btnTimeSet = findViewById(R.id.btnSetTime)

        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val editor = pref.edit()

        // 오늘 이미 실행했을 때
        if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains(currentDate)) {

            //시간을 이미 설정했을때
            if (pref.getLong("GOAL_HOURS", 0)>0){
                val intent = Intent(this, LeftTime::class.java)
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
            if(setHour == 0 && setMinute == 0) {
                Toast.makeText(this, "최소 30분부터 설정 가능합니다", Toast.LENGTH_LONG).show()
            } else{
                var dlg = AlertDialog.Builder(this)
                dlg.setMessage("${setHour}시간 ${setMinute}분으로 설정하시겠습니까?")
                dlg.setPositiveButton( "확인") { dialog, which ->
                    //목표시간 pref에 저장
                    goalHours = ( (setHour.toLong() * 60 ) + setMinute.toLong() ) *60*1000
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

    }


    // 슬라이드 메뉴 (Drawer) 초기화
    private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_st)
        toolbar.title = "시간 설정"
       if(toolbar!= null) {
            setSupportActionBar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawLayout = findViewById<DrawerLayout>(R.id.drawer_layout_st)
        var navigationView = findViewById<NavigationView>(R.id.nav_view_st)

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawLayout,
            toolbar,
            R.string.open,
            R.string.close
        );

        drawLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }

    // 슬라이드 메뉴에서 메뉴 선택시의 이벤트 처리
    override fun onNavigationItemSelected(item: MenuItem):Boolean{
        when(item.itemId){
            R.id.nav_personnal_information ->{
                val intent = Intent(this,PersonalInformationActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_setting_time -> {
                drawLayout.closeDrawer(GravityCompat.START);
            }
            R.id.nav_left_time -> {
                val pref = getSharedPreferences("pref", MODE_PRIVATE)
                    //시간을 이미 설정했을때
                if (pref.getLong("GOAL_HOURS", 0)>0){
                    val intent = Intent(this, LeftTime::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    drawLayout.closeDrawer(GravityCompat.START);
                    Toast.makeText(this, "아직 시간 약속을 정하지 않았습니다.", Toast.LENGTH_LONG).show()
                }
            }
            R.id.nav_time_record -> {
                val intent = Intent(this,HoursOfUse::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_gifticon -> {
                val intent = Intent(this,GifticonListActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_user_gifticon -> {
                val intent = Intent(this,UserGifticonListActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_mileage_record -> {
                drawLayout.closeDrawer(GravityCompat.START);
            }
            R.id.nav_logout -> {
                auth.signOut()
                val intent = Intent(this,LoadingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        return true
    }

    override fun onBackPressed() {
        // Drawer(슬라이드 메뉴)가 열려있으면 닫기
        if (drawLayout.isDrawerOpen(GravityCompat.START)) {
            drawLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



}