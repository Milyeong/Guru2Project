package com.example.guru2project

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GifticonItem (
    var name: String = "",
    var fileExtension: String = "",
    var image: String = "",
    var cost: Int = 0
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "fileExtension" to fileExtension,
            "image" to image,
            "cost" to cost
        )
    }

}