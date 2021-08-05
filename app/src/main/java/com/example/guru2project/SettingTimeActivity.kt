package com.example.guru2project

import android.app.AlertDialog
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.pow

class SettingTimeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb: SQLiteDatabase

    private lateinit var auth: FirebaseAuth
    private lateinit var mileageRef: DatabaseReference
    private var setHour : Int = 0
    private var setMinute : Int = 0
    private var mileage = 0

    private lateinit var drawLayout: DrawerLayout
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var btnTimeSet: Button

    var goalHours : Long =0


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_time)

        auth = Firebase.auth
        // 유저 마일리지 정보 불러오기.
        var user = auth.currentUser
        if (user != null){
            var uid = user.uid
            mileageRef = Firebase.database.reference.child("users").child(uid).child("mileage")

            val mileageListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val data = dataSnapshot.getValue().toString()
                    mileage = data.toInt()
                    Log.d(ContentValues.TAG, "마일리지"+ mileage)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            mileageRef.addValueEventListener(mileageListener)
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        this.init()

        hourSpinner = findViewById(R.id.hour_spinner)
        minuteSpinner = findViewById(R.id.minute_spinner)
        btnTimeSet = findViewById(R.id.btnSetTime)

        dbManager = DBManager(this, "Time", null, 1)

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

            //설치후 맨처음
            if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains("nodate")) {

                //처음 안내?

            } else {// 앱을 실행한 최근 날짜의 총사용시간 가져오기

                var lastDate = pref.getString("LAST_LAUNCH_DATE", "nodate")
                var lastDateTime= sdf.parse(lastDate).time
                var dtStart = lastDateTime
                var dtEnd = lastDateTime+24*60*60*1000
                var lastTotal = totalTimes(dtStart, dtEnd)

                //최근의 사용시간이 최근의 골보다 작을때
                var lastGoal = pref.getLong("GOAL_HOURS", 0)
                if(lastTotal - lastGoal < 0) {
                    //마일리지 적립
                    var user = auth.currentUser
                    if (user != null) {
                        var result = 0
                        var n = 11 - (lastTotal / (1000* 60) / 30).toInt()
                        if(n % 2 == 0 ){
                            result = (mileage + 2*(2.0).pow(n/2)).toInt()
                        }else{
                            result = (mileage + 3*(2.0).pow((n-1)/2)).toInt()
                        }
                        mileageRef.setValue(result)
                    }

                    sqlitedb = dbManager.writableDatabase
                    sqlitedb.execSQL("UPDATE Time SET true = 1 WHERE date = '$lastDate';")
                    sqlitedb.close()
                }
                //어플실행 최근일의 실행기록 데이터베이스에 넣기
                sqlitedb = dbManager.writableDatabase
                //var date= pref.getString("LAST_LAUNCH_DATE", "nodate")
                sqlitedb.execSQL("UPDATE Time SET total = "+lastTotal+" WHERE date = '"+lastDate+"';")
                sqlitedb.close()

                // 설정시간 초기화
                editor.putLong("GOAL_HOURS", 0)
                editor.putString("LAST_LAUNCH_DATE", currentDate)
                editor.apply()
            }

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
                    editor.putString("LAST_LAUNCH_DATE", currentDate)
                    editor.apply()
                    //데이터베이스에 저장
                    sqlitedb = dbManager.writableDatabase
                    sqlitedb.execSQL("INSERT INTO Time VALUES ('" + currentDate + "', '" + 0 + "', " + goalHours.toInt() + ", '" + 0 + "')")
                    sqlitedb.close()
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun totalTimes(startTime: Long, endTime: Long) : Long{
        var currentEvent: UsageEvents.Event
        val allEvents: MutableList<UsageEvents.Event> = ArrayList()
        val map: HashMap<String, Long> = HashMap()
        val mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        // 발생 이벤트를 쿼리
        val usageEvents = mUsageStatsManager.queryEvents(startTime, endTime)

        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            val packageName = currentEvent.packageName
            //추가 이벤트가 발견되면 이벤트 목록에 추가
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                allEvents.add(currentEvent)
                if (!map.containsKey(packageName)) {
                    map[packageName] = 0
                }
            }
        }

        //이벤트 발생시
        for (i in 0 until (allEvents.size - 1) step 1) {
            val event0 = allEvents[i]
            val event1 = allEvents[i + 1]

            //앱 실행할때마다 사용시간 측정
            if (event0.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                    (event1.eventType == UsageEvents.Event.ACTIVITY_PAUSED || event1.eventType == UsageEvents.Event.ACTIVITY_STOPPED)
                    && event0.packageName == event1.packageName) {
                val runtime = event1.timeStamp - event0.timeStamp
                val tInForeground = map[event0.packageName]!!.plus(runtime)
                map[event0.packageName] = tInForeground
            }
        }

        var totalTime: Long = 0
        map.forEach {it->
            totalTime += it.value
        }
        return totalTime
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