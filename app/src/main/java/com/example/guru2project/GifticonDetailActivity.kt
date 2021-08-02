package com.example.guru2project

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GifticonDetailActivity : AppCompatActivity() {

    private lateinit var tvGiftName: TextView
    private lateinit var tvMileage: TextView
    private lateinit var tvGiftCost: TextView
    private lateinit var imageView : ImageView
    private lateinit var btnBuy : Button

    private lateinit var database: DatabaseReference
    private lateinit var mileageRef: DatabaseReference
    private var mileage = 0
    private var giftCost = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gifticon_detail)

        // intent 정보 받아오기 (GifticonListActivity에서)
        var intent = intent;
        var giftName = intent.getStringExtra("itemName").toString()
        var giftImage = intent.getStringExtra("itemImage").toString()
        giftCost = intent.getIntExtra("itemCost", 0)

        var id = resources.getIdentifier(giftImage,"drawable",packageName)

        tvGiftName = findViewById(R.id.tv_gd_giftName)
        tvMileage = findViewById(R.id.tv_gd_mileage)
        tvGiftCost = findViewById(R.id.tv_gd_giftCost)
        imageView = findViewById(R.id.imageView_gd)
        btnBuy = findViewById(R.id.btn_gd_buy)

        var auth = Firebase.auth
        database = Firebase.database.reference

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
                    tvMileage.text = data
                    Log.d(ContentValues.TAG, "마일리지"+ mileage)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            mileageRef.addValueEventListener(mileageListener)
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }


        imageView.setImageDrawable(ContextCompat.getDrawable(this,id))
        tvGiftCost.text = giftCost.toString()
        tvGiftName.text = giftName



        btnBuy.setOnClickListener{
            if(user == null){
                Toast.makeText(this,"현재 구매할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var dlg = AlertDialog.Builder(this)
            dlg.setMessage("$giftName ${giftCost}원을 구매하시겠습니까?")
            dlg.setPositiveButton( "구매") { dialog, which ->
                if(mileage >= giftCost) {
                    var balance = mileage - giftCost
                    tvMileage.text = balance.toString()
                    mileageRef.setValue(balance)

                    // 구매 시간 계산
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
                    val formatted = current.format(formatter)

                    val uid = user.uid
                    database.child("gifticonRecord").child(uid).child("date").setValue(formatted)
                    database.child("gifticonRecord").child(uid).child("giftName").setValue(giftName)
                    database.child("gifticonRecord").child(uid).child("giftImage").setValue(giftImage)
                    database.child("gifticonRecord").child(uid).child("cost").setValue(giftCost)
                    Toast.makeText(this, "구매되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            dlg.setNegativeButton("취소", null)
            dlg.show()


        }

    }
}


