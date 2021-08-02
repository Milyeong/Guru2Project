package com.example.guru2project

import android.content.ContentValues
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
import org.w3c.dom.Text

class UserGifticonListActivity : AppCompatActivity() {

    private lateinit var userGiftRecordRef : DatabaseReference
    private var recordArray = ArrayList<GifticonRecordItem>()
    private lateinit var adapter: UserGiftListAdapter

    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_gifticon_list)

        listView = findViewById(R.id.listView_ugl)

        // 사용자 정보 불러오기
        var auth = Firebase.auth
        var user = auth.currentUser

        if(user != null){
            var uid = user.uid
            userGiftRecordRef = Firebase.database.reference.child("gifticonRecord").child(uid)
            setListenerToRef()
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        adapter = UserGiftListAdapter(recordArray)
        listView.adapter = adapter

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


    inner class UserGiftListAdapter(val list: ArrayList<GifticonRecordItem>) : BaseAdapter() {
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
                view = inflater.inflate(R.layout.user_gifticon_list_item, null)
            }

            var item = list.get(position)
            var giftName = item.giftName + " " + item.cost + "원"
            var dateArray = item.date.split(" ")

            var tvName: TextView? = view?.findViewById<TextView>(R.id.tv_ugli_giftName)
            if(tvName != null) tvName.text = giftName

            var tvDate: TextView? = view?.findViewById<TextView>(R.id.tv_ugli_date)
            if(tvDate!= null) tvDate.text = dateArray[0] + dateArray[1] + dateArray[2] + " 구매"

            return view

        }
    }
}