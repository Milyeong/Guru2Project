package com.example.guru2project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
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

class HoursOfUse : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth

    private lateinit var drawLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hours_of_use)
        // 원래 activity_hours_of_use 파일명은
        // content_hours_of_use.xml로 바꾸었습니다.
        // 캘린더와 텍스트뷰가 있는 파일은 content_hours_of_use 입니다.
        //

        this.init()

        auth  = Firebase.auth





    }

    // 슬라이드 메뉴 (Drawer) 초기화
    private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_hou)
        toolbar.title = "누적 시간"
        if(toolbar!= null) {
            setSupportActionBar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawLayout = findViewById<DrawerLayout>(R.id.drawer_layout_hou)
        var navigationView = findViewById<NavigationView>(R.id.nav_view_hou)

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
                val pref = getSharedPreferences("pref", MODE_PRIVATE)
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val currentDate = sdf.format(Date())
                val editor = pref.edit()

                // 오늘 이미 실행했을 때
                if (pref.getString("LAST_LAUNCH_DATE", "nodate")!!.contains(currentDate)) {

                    //시간을 이미 설정했을때
                    if (pref.getLong("GOAL_HOURS", 0) > 0) {
                        drawLayout.closeDrawer(GravityCompat.START);
                        Toast.makeText(this, "오늘의 시간 약속은 이미 정했습니다.", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    val intent = Intent(this, SettingTimeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            R.id.nav_main -> {
                super.onBackPressed();
            }
            R.id.nav_time_record -> {
                drawLayout.closeDrawer(GravityCompat.START);
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
                val intent = Intent(this,MileageUseActivity::class.java)
                startActivity(intent)
                finish()
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