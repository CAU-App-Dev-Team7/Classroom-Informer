from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
from ..core.config import supabase
from ..core.dependencies import get_current_user_id
from ..model.models import FavoriteToggleRequest, FavoriteResponse, RoomDetail

router = APIRouter(
    prefix="/favorites",
    tags=["Favorites"]
)

@router.post("/toggle", status_code=status.HTTP_200_OK)
async def toggle_favorite(
    req: FavoriteToggleRequest,
    user_id: str = Depends(get_current_user_id)
):
    """
    하트 버튼 클릭 시 호출:
    - 이미 즐겨찾기 되어 있으면 -> 삭제 (Unfavorite)
    - 없으면 -> 추가 (Favorite)
    """
    try:
        # 1. 기존 존재 여부 확인
        check_res = supabase.table("favorites")\
            .select("*")\
            .eq("user_id", user_id)\
            .eq("room_id", req.room_id)\
            .execute()

        if check_res.data:
            # 존재하면 삭제
            supabase.table("favorites")\
                .delete()\
                .eq("user_id", user_id)\
                .eq("room_id", req.room_id)\
                .execute()
            return {"status": "removed", "message": "Favorites removed"}
        else:
            # 없으면 추가
            supabase.table("favorites")\
                .insert({"user_id": user_id, "room_id": req.room_id})\
                .execute()
            return {"status": "added", "message": "Favorites added"}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("", response_model=List[FavoriteResponse])
async def get_my_favorites(user_id: str = Depends(get_current_user_id)):
    """
    내 즐겨찾기 목록 조회 (방 상세 정보 포함)
    """
    try:
        # Supabase JOIN 문법: rooms 테이블과 그 안의 buildings 테이블까지 참조
        query = """
            user_id, room_id, created_at,
            rooms (
                id, room_number,
                buildings ( code )
            )
        """
        response = supabase.table("favorites")\
            .select(query)\
            .eq("user_id", user_id)\
            .execute()

        results = []
        for item in response.data:
            # 응답 데이터 구조 평탄화 (Flattening)
            room_data = item.get("rooms")
            building_data = room_data.get("buildings") if room_data else None
            
            room_detail = None
            if room_data and building_data:
                room_detail = RoomDetail(
                    id=room_data["id"],
                    room_number=room_data["room_number"],
                    building_code=building_data["code"]
                )

            results.append(FavoriteResponse(
                user_id=item["user_id"],
                room_id=item["room_id"],
                created_at=item["created_at"],
                room=room_detail
            ))
            
        return results

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))