package com.example.classroominformer.data

import retrofit2.http.GET
import retrofit2.http.Query

interface InfoApi {

    @GET("info/room/timetable/free-slots")
    suspend fun getRoomFreeSlots(
        @Query("building_code") buildingCode: String,
        @Query("room_number") roomNumber: String,
        // optional: if null, backend will use default 09:00~20:00
        @Query("start_time") startTime: String? = null,   // "09:00"
        @Query("end_time") endTime: String? = null        // "20:00"
    ): List<RoomFreeSlotsResponse>
}
