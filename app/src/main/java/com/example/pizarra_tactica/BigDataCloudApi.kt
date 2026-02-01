package com.example.pizarra_tactica

import retrofit2.http.GET
import retrofit2.http.Query

interface BigDataCloudApi {
    @GET("data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") localityLanguage: String = "en"
    ): BigDataCloudReverseGeocodeResponse
}
