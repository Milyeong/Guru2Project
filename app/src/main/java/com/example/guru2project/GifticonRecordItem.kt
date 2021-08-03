package com.example.guru2project

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GifticonRecordItem(
    var giftName: String = "",
    var giftImage: String = "",
    var date: String = "",
    var cost: Int = 0,
    var leftMileage: Int = 0
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "giftName" to giftName,
            "giftImage" to giftImage,
            "date" to date,
            "cost" to cost,
            "leftMileage" to leftMileage
        )
    }
}