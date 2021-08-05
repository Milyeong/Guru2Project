package com.example.guru2project

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log

class LoadingActivity : AppCompatActivity() {

    val SPLASH_VIEW_TIME: Long = 1500 //2초간 스플래시 화면을 보여줌 (ms)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        startLoading()
    }

    override fun onRestart() {
        super.onRestart()

        startLoading()
    }

    private fun startLoading(){
        Handler(Looper.getMainLooper()).postDelayed({ //delay를 위한 handler
            check()
        }, SPLASH_VIEW_TIME)
    }

    private fun check(){
        //사용정보 접근 권한이 허용되지 않았을때
        if (!checkForPermission()) {
            var dlg = AlertDialog.Builder(this)
            dlg.setTitle("권한이 필요한 이유")
            dlg.setMessage("어플 사용을 위해서 사용정보 접근 허용이 필요합니다.")
            dlg.setPositiveButton( "확인") { dialog, which ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            dlg.setNegativeButton("취소"){ dialog, which ->
                finish()
            }
            dlg.show()
            Log.d(ContentValues.TAG, "확인: check if문")
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            Log.d(ContentValues.TAG, "확인: check if else문")
        }
    }
    // 사용정보 접근 권한 허용 여부 확인

    @SuppressLint("NewApi")
    private fun checkForPermission(): Boolean {
        Log.d(ContentValues.TAG, "확인: checkForPermission")
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }



//    @SuppressLint("NewApi")
//    private fun checkForPermission(): Boolean {
//        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//        val mode = appOps.startOp(
//            AppOpsManager.OPSTR_GET_USAGE_STATS,
//            applicationInfo.uid, applicationInfo.packageName, null, null)
//        return mode == AppOpsManager.MODE_ALLOWED
//    }

}