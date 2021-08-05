package com.example.guru2project

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
//import jdk.jfr.internal.handlers.EventHandler.timestamp
import java.text.SimpleDateFormat
import java.util.*


class PersonalInformationActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth

    private lateinit var drawLayout: DrawerLayout
    private lateinit var tvName: TextView
    private lateinit var tvPhoneNum : TextView
    private lateinit var tvEmail : TextView
    private lateinit var tvSignUpDate: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        tvName = findViewById(R.id.tv_pi_name)
        tvPhoneNum = findViewById(R.id.tv_pi_phoneNum)
        tvEmail = findViewById(R.id.tv_pi_email)
        tvSignUpDate = findViewById(R.id.tv_pi_date)

        this.init()

        auth = Firebase.auth

        var user = auth.currentUser
        if (user != null) {
            var uid = user.uid
            var name = ""
            var ref = Firebase.database.reference.child("users").child(uid).child("name").get().addOnSuccessListener {
                name = it.value.toString()
                tvName.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                tvName.text = name
            }

            var userMetadata = user.metadata
            var phoneNum = user.phoneNumber
            var email = user.email
            var date = userMetadata?.creationTimestamp

            val sfd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            if(date != null) {
                sfd.format(Date(date))
                tvSignUpDate.text = sfd.format(Date(date))
            }

            tvPhoneNum.text = phoneNum
            tvPhoneNum.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START

            tvEmail.text = email
            tvEmail.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START

            //tvSignUpDate.text = date.toString()
            tvSignUpDate.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START

        }else{
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 슬라이드 메뉴 (Drawer) 초기화
    private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_pi)
        toolbar.title = "사용자 개인 정보"
        if(toolbar!= null) {
            setSupportActionBar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawLayout = findViewById<DrawerLayout>(R.id.drawer_layout_pi)
        var navigationView = findViewById<NavigationView>(R.id.nav_view_pi)

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
                drawLayout.closeDrawer(GravityCompat.START);
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