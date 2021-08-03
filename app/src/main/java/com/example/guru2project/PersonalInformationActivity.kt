package com.example.guru2project

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
//import jdk.jfr.internal.handlers.EventHandler.timestamp
import java.text.SimpleDateFormat
import java.util.*


class PersonalInformationActivity : AppCompatActivity() {

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


        var auth = Firebase.auth

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
}