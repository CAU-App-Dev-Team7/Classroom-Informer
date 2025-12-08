from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional, Dict
from core.config import supabase
from datetime import time, datetime
from model.models import (
    BuildingResponse,
    RoomResponse,
    TimetableEntryResponse,
    FreeSlotDto,
    FreeSlotsResponseDto
)

router = APIRouter(
    prefix="/info",
    tags=["Public Info"]
)

# 전체 강의실 사용 가능 시간대
DEFAULT_START_TIME = time(9, 0)   # 09:00
DEFAULT_END_TIME = time(20, 0)    # 20:00


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
            building_code = building_code.strip()
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
        
        data = response.data

        # 💡 [추가] 데이터 후처리: building 객체에서 code를 꺼내 building_code로 격상
        for item in data:
            if item.get("building"):
                item["building_code"] = item["building"]["code"]
        
        return data

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
        if building_code:
            building_code = building_code.strip()
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
# GET /info/room/{room_id} (ID로 상세 조회)
# ----------------------------------------
@router.get("/room/{room_id}", response_model=RoomResponse)
async def get_room_by_id(
    room_id: int
):
    """
    Room ID(Primary Key)를 사용하여 강의실 상세 정보를 조회합니다.
    """
    query = "*, building:buildings(id, code, name)"

    try:
        # room_id를 기준으로 조회
        response = supabase.table("rooms")\
            .select(query)\
            .eq("id", room_id)\
            .single()\
            .execute()
        
        data = response.data
        if not data:
             raise HTTPException(status_code=404, detail=f"Room ID {room_id} not found")

        # 💡 [추가] 데이터 후처리: building_code 평탄화
        if data.get("building"):
            data["building_code"] = data["building"]["code"]
             
        return data

    except HTTPException as e:
        raise e
    except Exception as e:
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
        building_code = building_code.strip()
        building_res = supabase.table("buildings").select("id").eq("code", building_code).single().execute()
        if not building_res.data:
            raise HTTPException(status_code=404, detail=f"Building code '{building_code}' not found")
        building_id = building_res.data['id']

        # 2. room_number -> room_id
        room_number = room_number.strip()
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
    
# ----------------------------------------
# GET /info/room/timetable/free-slots
# ----------------------------------------
@router.get("/room/timetable/free-slots", response_model=List[FreeSlotsResponseDto])
async def get_free_slots_by_room(
    building_code: str = Query(..., description="조회할 건물 코드"),
    room_number: str = Query(..., description="조회할 강의실 번호"),
    start_time: time = Query(DEFAULT_START_TIME, description="조회 시작 시간"),
    end_time: time = Query(DEFAULT_END_TIME, description="조회 종료 시간")
):
    try:
        # 1. building_code -> building_id
        building_code = building_code.strip()
        building_res = (
            supabase.table("buildings")
            .select("id")
            .eq("code", building_code)
            .maybe_single()
            .execute()
        )

        if not getattr(building_res, "data", None):
            raise HTTPException(status_code=404, detail=f"Building code '{building_code}' not found")

        building_id = building_res.data["id"]

        # 2. room_number -> room_id
        room_number = room_number.strip()
        room_res = (
            supabase.table("rooms")
            .select("id")
            .eq("building_id", building_id)
            .eq("room_number", room_number)
            .maybe_single()
            .execute()
        )

        if not getattr(room_res, "data", None):
            raise HTTPException(status_code=404, detail=f"Room '{room_number}' in {building_code} not found")

        room_id = room_res.data["id"]

        # 3. timetable entries (0 rows allowed)
        timetable_res = (
            supabase.table("timetable_entries")
            .select("day,start_time,end_time")
            .eq("room_id", room_id)
            .order("day")
            .order("start_time")
            .execute()
        )

        occupied_entries = timetable_res.data or []

        days = ["월", "화", "수", "목", "금"]

        # free slots per day
        free_slots_by_day = {}

        for day in days:
            day_entries = [e for e in occupied_entries if e["day"] == day]

            current_start = start_time
            free_slots = []

            for entry in day_entries:
                entry_start = datetime.strptime(entry["start_time"], "%H:%M:%S").time()
                entry_end = datetime.strptime(entry["end_time"], "%H:%M:%S").time()

                if entry_end <= current_start:
                    continue

                if entry_start > current_start:
                    free_slots.append({
                        "start": current_start.strftime("%H:%M"),
                        "end": entry_start.strftime("%H:%M"),
                    })

                if entry_end > current_start:
                    current_start = entry_end

            if current_start < end_time:
                free_slots.append({
                    "start": current_start.strftime("%H:%M"),
                    "end": end_time.strftime("%H:%M"),
                })

            free_slots_by_day[day] = free_slots

        # 🔥 프론트 기대 형태로 변환
        result = [
            FreeSlotsResponseDto(
                building_code=building_code,
                room_number=room_number,
                free_slots_by_day=free_slots_by_day
                )]

        return result

    except HTTPException:
        raise

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

# ----------------------------------------
# GET /rooms/available
# ----------------------------------------
@router.get("/rooms/available")
async def get_available_rooms(
    building_code: str = Query(..., description="건물 코드 (예: 310)"),
    slots: List[str] = Query(..., description="시간 슬롯 리스트 (예: ['09:00-10:00'])"),
    room_number: Optional[str] = Query(None, description="강의실 번호 (선택, 예: 515)")
):
    """
    slots = ["09:00-10:00", "11:00-12:00"] 형태
    해당 건물에서 모든 슬롯이 비어있는 강의실 리스트 반환
    """
    try:
        # 1) building_code → building_id
        building_code = building_code.strip()
        building_res = (
            supabase.table("buildings")
            .select("id")
            .eq("code", building_code)
            .maybe_single()
            .execute()
        )

        building_data = getattr(building_res, "data", None)
        if not building_data:
            return []

        building_id = building_data["id"]

        # 2) Rooms 조회 쿼리 구성
        # 💡 room_number가 있으면 해당 방만, 없으면 건물 전체 방 조회
        rooms_query = supabase.table("rooms").select("id, room_number").eq("building_id", building_id)

        if room_number:
            # "호" 제거 및 공백 제거 (프론트에서 어떻게 보내든 처리 가능하게)
            clean_room_number = room_number.replace("호", "").strip()
            rooms_query = rooms_query.eq("room_number", clean_room_number)

        rooms_res = rooms_query.execute()

        room_list = rooms_res.data or []
        if not room_list:
            return []

        available_rooms = []

        # 3) 각 방별로 시간표 확인 (시간 객체 변환 로직 적용)
        for room in room_list:
            room_id = room["id"]

            # 해당 강의실의 예약·수업 일정 조회
            timetable_res = (
                supabase.table("timetable_entries")
                .select("day,start_time,end_time")
                .eq("room_id", room_id)
                .execute()
            )

            occupied = timetable_res.data or []

            # 4) 모든 요청 슬롯이 비어 있는지 확인
            all_free = True

            for slot in slots:
                # 슬롯 파싱 (HH:MM-HH:MM)
                parts = slot.strip().split("-")
                
                # 형식이 잘못되었거나 요일이 섞여 있으면 실패 처리
                if len(parts) != 2:
                    all_free = False
                    break
                
                try:
                    # 💡 문자열 -> time 객체 변환 (안전한 비교를 위해)
                    req_start = datetime.strptime(parts[0].strip(), "%H:%M").time()
                    req_end = datetime.strptime(parts[1].strip(), "%H:%M").time()
                except ValueError:
                    all_free = False
                    break

                for entry in occupied:
                    try:
                        # DB 시간 -> time 객체 변환
                        # DB가 HH:MM:SS.microseconds 일 수 있으므로 처리
                        entry_start_str = entry["start_time"].split(".")[0]
                        entry_end_str = entry["end_time"].split(".")[0]
                        
                        db_start = datetime.strptime(entry_start_str, "%H:%M:%S").time()
                        db_end = datetime.strptime(entry_end_str, "%H:%M:%S").time()
                    except ValueError:
                        continue # 포맷 에러 시 해당 엔트리 무시 (안전장치)

                    # 겹치는 경우 판별: (StartA < EndB) and (EndA > StartB)
                    if (db_start < req_end) and (db_end > req_start):
                        all_free = False
                        break

                if not all_free:
                    break

            if all_free:
                available_rooms.append({
                    "room_id": room["id"], # room_id는 int 타입이므로 room["id"]로 수정 (AvailableRoomDto의 room_id는 Int)
                    "building_code": building_code,
                    "room_number": room["room_number"]
                })

        return available_rooms

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB error: {str(e)}")