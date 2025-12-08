package com.example.classroominformer.ui.home

import com.example.classroominformer.data.FavoriteToggleRequest
import com.example.classroominformer.data.FavoritesApi
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.data.FavoriteResponseDto
import com.example.classroominformer.data.RoomDetailDto

/**
 * FastAPI + Supabase favorites 테이블과 통신하는 구현체.
 *
 * - userName 파라미터는 UI 표시에만 사용
 * - 백엔드는 JWT(access_token)로 user_id를 구함
 */
class RemoteFavouriteRoomRepository(
    private val api: FavoritesApi = RetrofitClient.favoritesApi
) : FavouriteRoomRepository {

    override suspend fun getFavourites(userName: String): List<FavouriteRoom> {
        val backendList: List<FavoriteResponseDto> = api.getMyFavorites()

        return backendList.map { dto ->
            val room: RoomDetailDto? = dto.room
            FavouriteRoom(
                id = room?.id ?: dto.room_id,
                roomName = if (room != null) {
                    "${room.building_code}-${room.room_number}"
                } else {
                    "Room ${dto.room_id}"
                },
                building = room?.let { "Building ${it.building_code}" } ?: "Unknown building",
                floor = "Unknown floor",       // TODO: add real floor later
                nextFreeSlot = "Unknown time", // TODO: link to timetable/free-slots API later
                isFreeNow = false              // TODO: compute from current time
            )
        }
    }

    override suspend fun searchRooms(query: String): List<FavouriteRoom> {
        // 현재 즐겨찾기 API에는 검색이 없으므로, 클라이언트에서 필터
        val all = getFavourites(userName = "")
        if (query.isBlank()) return all
        val q = query.trim()
        return all.filter {
            it.roomName.contains(q, ignoreCase = true) ||
                    it.building.contains(q, ignoreCase = true) ||
                    it.floor.contains(q, ignoreCase = true)
        }
    }

    override suspend fun addFavourite(userName: String, roomId: Long) {
        api.toggleFavorite(FavoriteToggleRequest(room_id = roomId))
    }

    override suspend fun removeFavourite(userName: String, roomId: Long) {
        api.toggleFavorite(FavoriteToggleRequest(room_id = roomId))
    }
}
