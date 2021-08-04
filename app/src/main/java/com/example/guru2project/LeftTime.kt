package com.example.guru2project

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class LeftTime : AppCompatActivity() {

    private lateinit var appName1: TextView
    private lateinit var appUsageTime1: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvMinuts: TextView
    private lateinit var tvLeftTime: TextView

    private lateinit var usageMap: HashMap<String, Long>

    private var totalTime: Long = 0
    private var goalHours: Long = 0
    private var leftHours: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_left_time)

        tvHours = findViewById(R.id.tvHours)
        tvMinuts = findViewById(R.id.tvMinutes)
        tvLeftTime = findViewById(R.id.LeftTimeText)

        var tz= TimeZone.getDefault()
        var tzId= tz.toZoneId()
        var dtNow = LocalDate.now()
        var dtStart = dtNow.atStartOfDay(tzId).toInstant().toEpochMilli()

        usageMap = mapUsageTimes(dtStart, System.currentTimeMillis())

        usageMap.forEach {it->
            totalTime += it.value
        }

        // 사용시간을 기준으로 내림차순으로 배열
        val result = usageMap.toList().sortedByDescending { (_, value) -> value}

        var nameId:Int
        var timeId:Int

        for(i in result.indices) {
            if(i==3)
                break
            nameId=resources.getIdentifier("tvAppName${i+1}","id", packageName)
            timeId=resources.getIdentifier("tvUsageTimes${i+1}","id", packageName)

            var appName=findViewById<TextView>(nameId)
            var appUsageT=findViewById<TextView>(timeId)


            var appInfo = packageManager.getApplicationInfo(result[i].first, PackageManager.GET_META_DATA)
            var appLabel = packageManager.getApplicationLabel(appInfo)

            appName.text=appLabel
            appUsageT.text="${(result[i].second / (1000*60*60)) % 24}시간 ${((result[i].second)/(1000*60))%60}분"


        }
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        /*val editor = pref.edit()
        editor.putLong("GOAL_HOURS", 86400000)
        editor.apply()*/
        goalHours = pref.getLong("GOAL_HOURS", 0)
        leftHours = (goalHours-totalTime).toInt()

        //남은 시간
        if(leftHours < 0){
            //목표달성 실패
            tvLeftTime.text="다음엔 더 노력해 봅시다!!"

        } else{

            val min = (leftHours/ (1000*60))% 60
            val hour =(leftHours / (1000*60*60))%24
            tvHours.text="$hour"
            tvMinuts.text="$min"
        }




    }

    //하루동안 사용한 앱의 패키지 이름과 사용시간을 Map으로 가져오기
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun mapUsageTimes(startTime: Long, endTime: Long) : HashMap<String, Long>{
        var currentEvent: UsageEvents.Event
        val allEvents: MutableList<UsageEvents.Event> = ArrayList()
        val map: HashMap<String, Long> = HashMap()
        val mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        // 발생 이벤트를 쿼리
        val usageEvents = mUsageStatsManager.queryEvents(startTime, endTime)

        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            val packageName = currentEvent.packageName
            //추가 이벤트가 발견되면 이벤트 목록에 추가
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                allEvents.add(currentEvent)
                if (!map.containsKey(packageName)) {
                    map[packageName] = 0
                }
            }
        }

        //이벤트 발생시
        for (i in 0 until (allEvents.size - 1) step 1) {
            val event0 = allEvents[i]
            val event1 = allEvents[i + 1]

            //앱 실행할때마다 사용시간 측정
            if (event0.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                (event1.eventType == UsageEvents.Event.ACTIVITY_PAUSED || event1.eventType == UsageEvents.Event.ACTIVITY_STOPPED)
                && event0.packageName == event1.packageName) {
                val runtime = event1.timeStamp - event0.timeStamp
                val tInForeground = map[event0.packageName]!!.plus(runtime)
                map[event0.packageName] = tInForeground
            }
        }

        return map
    }


}



