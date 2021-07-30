package com.example.guru2project

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var edtPhoneNum: EditText
    private lateinit var edtCode: EditText
    private lateinit var btnRequest: Button
    private lateinit var btnPhoneAuth: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        edtPhoneNum = findViewById(R.id.edt_pA_phoneNum)
        edtCode = findViewById(R.id.edt_pA_code)
        btnRequest = findViewById(R.id.btn_pA_code)
        btnPhoneAuth = findViewById(R.id.btn_pA_phoneAuth)

        auth = Firebase.auth
        auth.setLanguageCode("ko")
        // 콜백 함수 구현
        callback()

        // 사용자 전화로 인증 코드 전송
        btnRequest.setOnClickListener{
            var input = edtPhoneNum.text.toString()
            if(TextUtils.isEmpty(input)){
                Toast.makeText(baseContext, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

            var phoneNumber = "+82" + input.substring(1)
            Log.d(TAG, "??:${phoneNumber}")
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(90L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        // 코드로 인증하기
        btnPhoneAuth.setOnClickListener{
            var code = edtCode.text.toString()
            if(TextUtils.isEmpty(code)){
                Toast.makeText(baseContext, "인증코드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            try {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
                linkWithPhoneAuthCredential(credential)
            }catch(e:Exception){
                Toast.makeText(baseContext, "인증번호요청이 필요합니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "??:${e.message}")
            }
        }
    }

    private fun callback(){
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                linkWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }
    }

    private fun linkWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

}