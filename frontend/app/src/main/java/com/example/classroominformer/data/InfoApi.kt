package com.example.classroominformer.data

import retrofit2.http.GET
import retrofit2.http.Query

interface InfoApi {

    @GET("info/rooms/available")
    suspend fun getAvailableRooms(
        @Query("building_code") buildingCode: String,
        @Query("slots") slots: List<String>
    ): List<AvailableRoomDto>

    @GET("info/room/timetable/free-slots")
    suspend fun getFreeSlotsByRoom(
        @Query("building_code") buildingCode: String,
        @Query("room_number") roomNumber: String,
        @Query("start_time") startTime: String? = null,
        @Query("end_time") endTime: String? = null
    ): List<FreeSlotsResponseDto>

    // NEW: ROOM DETAIL ENDPOINT
    @GET("info/room/detail")
    suspend fun getRoomDetail(
        @Query("room_id") roomId: Long
    ): RoomDetailDto
}
