package com.example.waterplants.model

import android.net.Uri

data class Plant(var pin: Int?, var intervalDays: Int?, var amount: Int?, var hourOfDay: Int?,
                 var watered: Boolean?, var name: String?, var imageUri: Uri?) {
    override fun toString(): String {
        return if (name == null)
            ""
        else "$name"
    }
}