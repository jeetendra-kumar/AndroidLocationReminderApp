package com.jeet.androidreminderapp.data.remote

import com.jeet.androidreminderapp.data.dto.NominatimDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GeoCodingApi {

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Header("User-Agent") userAgent: String = "AndroidReminderApp/1.0"
    ): NominatimDto
}