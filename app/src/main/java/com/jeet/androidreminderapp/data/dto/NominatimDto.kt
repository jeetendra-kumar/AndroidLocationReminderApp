package com.jeet.androidreminderapp.data.dto

import com.google.gson.annotations.SerializedName

data class NominatimDto(
    @SerializedName("display_name")
    val displayName: String
)
