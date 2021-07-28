package com.example.guru2project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtPw: EditText
    private lateinit var edtName: EditText
    private lateinit var btnBackToLogin: Button
    private lateinit var btnSignUp: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        //auth = FirebaseAuth.getInstance() 이거랑 뭐가 다른거지?? -> 자바에서 쓰는 형태

        // 뷰 연결
        edtEmail = findViewById(R.id.edt_signUp_email)
        edtPw = findViewById(R.id.edt_singUp_pw)
        edtName = findViewById(R.id.edt_signUp_name)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        btnSignUp = findViewById(R.id.btnSignUp)



        btnSignUp.setOnClickListener(View.OnClickListener {
            var email = edtEmail.text.toString()
            var pw = edtPw.text.toString()
            var name = edtName.text.toString()

            if(TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext,"이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if(TextUtils.isEmpty(pw)){
                Toast.makeText(applicationContext,"비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if(TextUtils.isEmpty(name)){
                Toast.makeText(applicationContext,"이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            //사용자 계정 생성
            auth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(this) { task ->

            }


        })

        btnBackToLogin.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}