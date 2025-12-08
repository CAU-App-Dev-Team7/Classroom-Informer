from fastapi import APIRouter, HTTPException, status, Query, Depends
from typing import List, Optional
from core.config import supabase
from core.dependencies import get_current_user_id # 필요시 사용
from datetime import time, datetime
from model.models import (
    BuildingResponse,
    RoomResponse,
    TimetableEntryResponse
)

router = APIRouter(
    prefix="/info",
    tags=["Public Info"]
)

# ----------------------------------------
# GET /info/buildings
# ----------------------------------------
@router.get("/buildings", response_model=List[BuildingResponse])
async def get_buildings():
    """
    모든 건물 목록을 조회합니다.
    """
    try:
        response = supabase.table("buildings").select("*").order("code").execute()
        return response.data
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ----------------------------------------
# GET /info/rooms
# ----------------------------------------
@router.get("/rooms", response_model=List[RoomResponse])
async def get_rooms(
    building_code: Optional[str] = Query(None, description="특정 건물 코드로 필터링")
):
    """
    강의실 목록을 조회하며, building_code로 필터링할 수 있습니다.
    """
    # 💡 [수정] building_id를 명시적으로 쿼리 목록에 추가합니다.
    query = "*, building:buildings(id, code, name)"

    try:
        if building_code:
            # 1. 🏢 building_code를 사용하여 building_id를 조회합니다.
            building_res = supabase.table("buildings").select("id").eq("code", building_code).single().execute()
            
            # 조회 결과가 없다면 404 반환
            if not building_res.data:
                 raise HTTPException(status_code=404, detail=f"Building code {building_code} not found")
                 
            target_building_id = building_res.data['id']
            
            # 2. 🎯 rooms 테이블의 building_id를 기준으로 필터링합니다.
            #    이는 Supabase에게 명시적인 WHERE 절 필터링을 지시합니다.
            response = supabase.table("rooms").select(query).eq("building_id", target_building_id).execute()
        else:
            # 필터링이 없으면 전체 조회
            response = supabase.table("rooms").select(query).execute()
        
        return response.data

    except HTTPException as e:
        # 404 오류는 그대로 반환
        raise e
    except Exception as e:
        # 기타 DB 연결 오류 등은 500으로 처리
        # print(e) # 디버깅용 로그
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

# ----------------------------------------
# GET /info/room/details
# ----------------------------------------
@router.get("/room/details", response_model=RoomResponse)
async def get_room_by_identifier(
    # building_code와 room_number는 필수 쿼리 파라미터로 설정
    building_code: str = Query(..., description="조회할 건물의 코드 (예: 310)"),
    room_number: str = Query(..., description="조회할 강의실 번호 (예: 607)")
):
    """
    건물 코드와 강의실 번호를 사용하여 특정 강의실의 상세 정보를 조회합니다.
    """
    
    # 쿼리: 모든 필드(*)와 JOIN된 building 객체를 모두 요청
    query = "*, building:buildings(id, code, name)"

    try:
        # 1. 🏢 building_code를 사용하여 building_id를 조회합니다.
        building_res = supabase.table("buildings").select("id").eq("code", building_code).single().execute()
        
        if not building_res.data:
             raise HTTPException(status_code=404, detail=f"Building code '{building_code}' not found")
             
        target_building_id = building_res.data['id']
        
        # 2. 🎯 building_id와 room_number를 기준으로 정확히 하나의 강의실을 조회합니다.
        response = supabase.table("rooms")\
            .select(query)\
            .eq("building_id", target_building_id)\
            .eq("room_number", room_number)\
            .single()\
            .execute()
        
        if not response.data:
             raise HTTPException(status_code=404, detail=f"Room {room_number} in {building_code} not found")
             
        # single()을 사용했으므로 데이터는 딕셔너리 하나입니다.
        return response.data

    except HTTPException as e:
        # 404 오류는 그대로 반환
        raise e
    except Exception as e:
        # 기타 DB 연결 오류 또는 single()이 여러 결과를 반환했을 경우 500 처리
        # print(e) 
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")
    
# ----------------------------------------
# GET /info/room/timetable
# ----------------------------------------
@router.get("/room/timetable", response_model=List[TimetableEntryResponse])
async def get_timetable_by_room(
    building_code: str = Query(..., description="조회할 건물 코드"),
    room_number: str = Query(..., description="조회할 강의실 번호")
):
    """
    특정 강의실의 전체 시간표 조회
    """
    try:
        # 1. building_code -> building_id
        building_res = supabase.table("buildings").select("id").eq("code", building_code).single().execute()
        if not building_res.data:
            raise HTTPException(status_code=404, detail=f"Building code '{building_code}' not found")
        building_id = building_res.data['id']

        # 2. room_number -> room_id
        room_res = supabase.table("rooms")\
            .select("id")\
            .eq("building_id", building_id)\
            .eq("room_number", room_number)\
            .single()\
            .execute()
        if not room_res.data:
            raise HTTPException(status_code=404, detail=f"Room '{room_number}' in {building_code} not found")
        room_id = room_res.data['id']

        # 3. room_id -> timetable_entries 조회
        timetable_res = supabase.table("timetable_entries")\
            .select("*")\
            .eq("room_id", room_id)\
            .order("day")\
            .order("start_time")\
            .execute()

        return timetable_res.data

    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")