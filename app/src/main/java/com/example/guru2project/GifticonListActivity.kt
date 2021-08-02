package com.example.guru2project

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class GifticonListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var gifticonRef: DatabaseReference
    private var array: ArrayList<GifticonItem> = ArrayList<GifticonItem>()
    private lateinit var adapter: GridAdapter
    private var mileage = 0


    private lateinit var gridView : GridView
    private lateinit var tvMileage : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gifticon_list)

        gridView = findViewById(R.id.gridView_gl)
        tvMileage = findViewById(R.id.tv_gL_mileage)

        var auth = Firebase.auth
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
        //auth.signOut()


        getView()

        adapter = GridAdapter(array)
        gridView.adapter = adapter

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

    // 리스트를 반환해야 겠지? 아님 전체 변수로 선언하는 것도 좋을 듯.
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
                        if(data != null) {
                            array.add(data)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                // 어댑터에 리스트 넣기.
                //adapter = GridAdapter(array)
                //gridView.adapter = adapter
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


