package com.example.guru2project

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class GifticonListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var gifticonRef: DatabaseReference
    private var array: ArrayList<GifticonItem> = ArrayList<GifticonItem>()
    private lateinit var adapter: GridAdapter
    private var mileage = 0

    private lateinit var drawLayout: DrawerLayout
    private lateinit var gridView : GridView
    private lateinit var tvMileage : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gifticon_list)
        //supportActionBar.setTitle()
        this.init()
        gridView = findViewById(R.id.gridView_gl)
        tvMileage = findViewById(R.id.tv_gL_mileage)

        auth = Firebase.auth
        database = Firebase.database.reference
        gifticonRef = Firebase.database.reference.child("gifticon")

        // 유저 마일리지 정보 불러오기.
        var user = auth.currentUser
        if (user != null){
            var uid = user.uid
            database.child("users").child(uid).child("mileage").get().addOnSuccessListener {
                mileage = it.value.toString().toInt()
                tvMileage.text = mileage.toString()
                Log.d(TAG, "마일리지"+ mileage)
            }
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        // 판매 중인 기프티콘 데이터 가져오기.
        getView()

        adapter = GridAdapter(array)
        gridView.adapter = adapter

        // gridView 클릭이벤트리스너
        gridView.setOnItemClickListener{ parent: AdapterView<*>, view: View, position: Int, id: Long ->
            val item: GifticonItem = parent.getItemAtPosition(position) as GifticonItem
            val intent = Intent(this, GifticonDetailActivity::class.java)
            intent.putExtra("itemName", item.name)
            intent.putExtra("itemCost", item.cost)
            intent.putExtra("itemImage",item.image)
            startActivity(intent)

            //Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
        }
        // 1. 파이어베이스에서 이미지 불러오기
        // 먼저 gifticon 내 폴더 리스트 만들고, (함수 1)
        // 그 폴더 리스트 내에 있는 이미지 리스트 만들고 ( 함수 2)
        // 그 이미지 리스트 참고하여 레이아웃 리스트 만들기 (함수 3)
        // 2. 불러온 이미지(url) 리스트에 저장하기
        // 3. 리스트를 통해 새 레이아웃 아이템 만들고 기존 레이아웃에 더하기

    }

    // 슬라이드 메뉴 (Drawer) 초기화
   private fun init(){
        var toolbar = findViewById<Toolbar>(R.id.toolbar_gl)
        toolbar.title = " 기프티콘 목록"
        if(toolbar!= null) {
            setSupportActionBar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawLayout = findViewById<DrawerLayout>(R.id.drawer_layout_gl)
        var navigationView = findViewById<NavigationView>(R.id.nav_view_gl)

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
            R.id.nav_time_record -> {

            }
            R.id.nav_gifticon -> {
                drawLayout.closeDrawer(GravityCompat.START);
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
                val intent = Intent(this,MainActivity::class.java)
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



    // 판매 중인 기프티콘 데이터 받기.
    private fun getView(){
        val listListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG,"확인" + snapshot.childrenCount)
                array.clear()
                for(storeSnapshot in snapshot.children){
                    Log.d(TAG,"item 확인" + storeSnapshot.key)
                    for(giftSnapshot in storeSnapshot.children){
                        var data = giftSnapshot.getValue(GifticonItem::class.java)
                        Log.d(TAG,data?.image.toString())
                        // 데이터가 있으면 array에 추가 및 어댑터 갱신
                        if(data != null) {
                            array.add(data)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "오류가 발생했습니다: 목록")
            }
        }
        gifticonRef.addValueEventListener(listListener)

    }


    inner class GridAdapter(val list: ArrayList<GifticonItem>) : BaseAdapter() {
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
                view = inflater.inflate(R.layout.gifticon_list_item,null)
            }

            var item = list.get(position)
            var imageText = item.image
            var id = resources.getIdentifier(imageText,"drawable",packageName)

            var text : TextView? = view?.findViewById(R.id.tv_gLI_name)
            if (text != null) text.text = item.name + "\n" + item.cost + "원"

            var imageView : ImageView? = view?.findViewById(R.id.imageView_gLI)
            if(imageView != null){
                imageView.setImageDrawable(ContextCompat.getDrawable(baseContext,id))
            }

            return view

        }
    }
}



