package com.example.guru2project

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var input_email: EditText
    private lateinit var input_pw: EditText
    private lateinit var btnJoin: Button
    private lateinit var btnlogin: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 사용자 로그인상태 확인
        auth = Firebase.auth

        // 사용자가 로그인되어 있다면(자동로그인) SettingTimeAcitivity로
        if (auth.currentUser != null) {
            val intent = Intent(this, SettingTimeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 뷰 연결
        input_email = findViewById(R.id.login_email)
        input_pw = findViewById(R.id.login_pw)
        btnJoin = findViewById(R.id.btnBackToSignUp)
        btnlogin = findViewById(R.id.btnLogin)

        btnJoin.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnlogin.setOnClickListener(View.OnClickListener {
            var email = input_email.text.toString()
            var pw = input_pw.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (TextUtils.isEmpty(pw)) {
                Toast.makeText(applicationContext, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            // 로그인 시도
            auth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "로그인:성공")
                    val user = auth.currentUser

                    val intent = Intent(this, SettingTimeActivity::class.java)
                    startActivity(intent)
                    finish()
                    // updateUi(user) 함수 사용 - 만들어야 함.
                } else {
                    Log.w(TAG, "로그인:실패", task.exception)
                    Toast.makeText(baseContext, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

            }

        })
    }
}