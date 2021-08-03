package com.example.guru2project

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class UserGifticonDetailActivity : AppCompatActivity() {

    private lateinit var giftImageView : ImageView
    private lateinit var tvGiftName : TextView
    private lateinit var tvDate : TextView
    private lateinit var tvPlace : TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_gifticon_detail)

        // intent 정보 받아오기 (UserGifticonListActivity에서)
        var intent = intent;
        var giftName = intent.getStringExtra("giftName").toString()
        var giftImage = intent.getStringExtra("giftImage").toString()
        var cost = intent.getIntExtra("cost", 0)
        var date = intent.getStringExtra("date").toString()

        giftImageView = findViewById(R.id.imageView_ugd)
        tvGiftName = findViewById(R.id.tv_ugd_name)
        tvDate = findViewById(R.id.tv_ugd_expiration_date)
        tvPlace = findViewById(R.id.tv_ugd_usePlace)

        // 이미지 변경
        var id = resources.getIdentifier(giftImage,"drawable",packageName)
        giftImageView.setImageDrawable(ContextCompat.getDrawable(this,id))

        tvGiftName.text = giftName + " " + cost + "원"
        tvPlace.text = giftName.split(" ")[0]

        tvDate.text = date


        // 기프티콘 만료일 설정 - 임의로 구매일로부터 3달 후로 설정. -> UserGifticonListActivity로 이동
//        var dateArray = date.split(" ")
//        var format = SimpleDateFormat("yyyy-MM-dd")
//        var calendar = Calendar.getInstance()
//        calendar.set(dateArray[0].replace("년","").toInt(), dateArray[1].replace("월","").toInt(), dateArray[2].replace("일","").toInt())
//        calendar.add(Calendar.MONTH,+3)
//        tvDate.text = format.format(calendar.time)





    }
}