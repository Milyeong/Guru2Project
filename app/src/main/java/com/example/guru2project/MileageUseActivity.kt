package com.example.guru2project

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
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
import java.util.*
import kotlin.collections.ArrayList

class MileageUseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var userGiftRecordRef: DatabaseReference
    private var recordArray = ArrayList<GifticonRecordItem>()
    private lateinit var adapter: UserUseMileageListAdapter

    private lateinit var drawLayout: DrawerLayout
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mileage_use)

        this.init()
        listView = findViewById(R.id.listView_um)


        // 사용자 정보 불러오기
        auth = Firebase.auth
        var user = auth.currentUser

        if(user != null){
            var uid = user.uid
            userGiftRecordRef = Firebase.database.reference.child("gifticonRecord").child(uid)
            setListenerToRef()
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        adapter = UserUseMileageListAdapter(recordArray)
        listView.adapter = adapter
    }

    // 슬라이드 메뉴 (Drawer) 초기화
    private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_mu)
        toolbar.title = "마일리지 사용 기록"
        if(toolbar!= null) {
            setSupportActionBar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawLayout = findViewById<DrawerLayout>(R.id.drawer_layout_mu)
        var navigationView = findViewById<NavigationView>(R.id.nav_view_mu)

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

    // 사용자 기프티콘 목록 불러오기
    private fun setListenerToRef(){
        val listListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(ContentValues.TAG,"확인" + snapshot.childrenCount)
                recordArray.clear()

                for(itemSnapshot in snapshot.children){
                    var data =  itemSnapshot.getValue(GifticonRecordItem::class.java)
                    if(data != null){
                        recordArray.add(data)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(ContentValues.TAG, "오류가 발생했습니다: 목록")
            }
        }
        userGiftRecordRef.addValueEventListener(listListener)

    }


    inner class UserUseMileageListAdapter(val list: ArrayList<GifticonRecordItem>) : BaseAdapter() {
        private lateinit var inflater: LayoutInflater

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            inflater = LayoutInflater.from(parent?.context)

            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.use_mileage_item, null)
            }

            var item = list.get(position)

            // 기프티콘 이름 설정
            var giftName = item.giftName + " " + item.cost + "원"
            var tvName: TextView? = view?.findViewById<TextView>(R.id.tv_um_giftName)
            if(tvName != null) tvName.text = giftName

            // 기프티콘 구매일 설정
            var dateArray = item.date.split(" ")
            var format = SimpleDateFormat("yyyy-MM-dd")
            var calendar = Calendar.getInstance()
            calendar.set(dateArray[0].replace("년","").toInt(), dateArray[1].replace("월","").toInt(), dateArray[2].replace("일","").toInt())
            var date = format.format(calendar.time)

            var tvDate: TextView? = view?.findViewById<TextView>(R.id.tv_um_date)
            if(tvDate!= null) tvDate.text = date

            // 사용 마일리지 설정
            var useMile = item.cost
            var tvUseMile: TextView? = view?.findViewById(R.id.tv_um_useMile)
            if(tvUseMile != null) tvUseMile.text = useMile.toString()

            // 남은 마일리지 설정
            var leftMile = item.leftMileage
            var tvLeftMile: TextView? = view?.findViewById(R.id.tv_um_leftMile)
            if(tvLeftMile != null) tvLeftMile.text = leftMile.toString()



            return view

        }
    }
}