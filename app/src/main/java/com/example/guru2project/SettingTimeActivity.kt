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
        // ?????? ???????????? ?????? ????????????.
        var user = auth.currentUser
        if (user != null){
            var uid = user.uid
            mileageRef = Firebase.database.reference.child("users").child(uid).child("mileage")

            val mileageListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val data = dataSnapshot.getValue().toString()
                    mileage = data.toInt()
                    Log.d(ContentValues.TAG, "????????????"+ mileage)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            mileageRef.addValueEventListener(mileageListener)
        } else {
            Toast.makeText(this, "????????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
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

        // ?????? ?????? ???????????? ???
        if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains(currentDate)) {

            //????????? ?????? ???????????????
            if (pref.getLong("GOAL_HOURS", 0)>0){
                val intent = Intent(this, LeftTime::class.java)
                startActivity(intent)
                finish()
            }

        } else {//?????? ?????? ???????????????

            //????????? ?????????
            if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains("nodate")) {

                //?????? ???????

            } else {// ?????? ????????? ?????? ????????? ??????????????? ????????????

                var lastDate = pref.getString("LAST_LAUNCH_DATE", "nodate")
                var lastDateTime= sdf.parse(lastDate).time
                var dtStart = lastDateTime
                var dtEnd = lastDateTime+24*60*60*1000
                var lastTotal = totalTimes(dtStart, dtEnd)

                //????????? ??????????????? ????????? ????????? ?????????
                var lastGoal = pref.getLong("GOAL_HOURS", 0)
                if(lastTotal - lastGoal < 0) {
                    //???????????? ??????
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
                //???????????? ???????????? ???????????? ????????????????????? ??????
                sqlitedb = dbManager.writableDatabase
                //var date= pref.getString("LAST_LAUNCH_DATE", "nodate")
                sqlitedb.execSQL("UPDATE Time SET total = "+lastTotal+" WHERE date = '"+lastDate+"';")
                sqlitedb.close()

                // ???????????? ?????????
                editor.putLong("GOAL_HOURS", 0)
                editor.putString("LAST_LAUNCH_DATE", currentDate)
                editor.apply()
            }

        }


        //?????? ??????
        hourSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setHour = hourSpinner.getItemAtPosition(position).toString().toInt()
            }
        }
        //??? ??????
        minuteSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setMinute = minuteSpinner.getItemAtPosition(position).toString().toInt()
            }
        }

        //???????????? ??????
        btnTimeSet.setOnClickListener {
            if(setHour == 0 && setMinute == 0) {
                Toast.makeText(this, "?????? 30????????? ?????? ???????????????", Toast.LENGTH_LONG).show()
            } else{
                var dlg = AlertDialog.Builder(this)
                dlg.setMessage("${setHour}?????? ${setMinute}????????? ?????????????????????????")
                dlg.setPositiveButton( "??????") { dialog, which ->
                    //???????????? pref??? ??????
                    goalHours = ( (setHour.toLong() * 60 ) + setMinute.toLong() ) *60*1000
                    editor.putLong("GOAL_HOURS", goalHours)
                    editor.putString("LAST_LAUNCH_DATE", currentDate)
                    editor.apply()
                    //????????????????????? ??????
                    sqlitedb = dbManager.writableDatabase
                    sqlitedb.execSQL("INSERT INTO Time VALUES ('" + currentDate + "', '" + 0 + "', " + goalHours.toInt() + ", '" + 0 + "')")
                    sqlitedb.close()
                    //LeftTime??????
                    val intent = Intent(this, LeftTime::class.java)
                    startActivity(intent)
                    finish()
                }
                dlg.setNegativeButton("??????", null)
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
        // ?????? ???????????? ??????
        val usageEvents = mUsageStatsManager.queryEvents(startTime, endTime)

        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            val packageName = currentEvent.packageName
            //?????? ???????????? ???????????? ????????? ????????? ??????
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                allEvents.add(currentEvent)
                if (!map.containsKey(packageName)) {
                    map[packageName] = 0
                }
            }
        }

        //????????? ?????????
        for (i in 0 until (allEvents.size - 1) step 1) {
            val event0 = allEvents[i]
            val event1 = allEvents[i + 1]

            //??? ?????????????????? ???????????? ??????
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



    // ???????????? ?????? (Drawer) ?????????
    private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_st)
        toolbar.title = "?????? ??????"
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

    // ???????????? ???????????? ?????? ???????????? ????????? ??????
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
                    //????????? ?????? ???????????????
                if (pref.getLong("GOAL_HOURS", 0)>0){
                    val intent = Intent(this, LeftTime::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    drawLayout.closeDrawer(GravityCompat.START);
                    Toast.makeText(this, "?????? ?????? ????????? ????????? ???????????????.", Toast.LENGTH_LONG).show()
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
        // Drawer(???????????? ??????)??? ??????????????? ??????
        if (drawLayout.isDrawerOpen(GravityCompat.START)) {
            drawLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



}