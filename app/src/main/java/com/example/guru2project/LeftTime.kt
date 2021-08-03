package com.example.guru2project

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class LeftTime : AppCompatActivity() {

    lateinit var usageMap: HashMap<String, Long>
    lateinit var appName: TextView
    lateinit var appUsageTime: TextView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_left_time)


        var tz= TimeZone.getDefault()
        var tzId= tz.toZoneId()
        var dtNow = LocalDate.now()
        var dtStart = dtNow.atStartOfDay(tzId).toInstant().toEpochMilli()

        usageMap = mapUsageTimes(dtStart, System.currentTimeMillis())

        var totalTime: Long = 0
        usageMap.forEach {it->
            totalTime += it.value
        }

        // 사용시간을 기준으로 내림차순으로 배열
        val result = usageMap.toList().sortedByDescending { (_, value) -> value}
        val list:MutableList<Pair<String, Long>> = mutableListOf()
        //하루동안 제일 많이 사용한 어플 3개
        for (i in 0..2) {
            list.add(Pair(result[i].first, result[i].second))

                //print(result[i].first)
                //println(result[i].second)
        }



    }

    //하루동안 사용한 앱의 패키지 이름과 사용시간을 MutableList로 가져오기
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