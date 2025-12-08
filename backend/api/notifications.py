from fastapi import APIRouter, Depends, HTTPException
from datetime import datetime, timedelta, timezone
import pytz
from typing import List, Dict, Any
from core.config import supabase
from core.dependencies import get_current_user_id
from model.models import NotificationCheckRequest

router = APIRouter(
    prefix="/notifications",
    tags=["Notifications"]
)

# 한국 시간대 설정
KST = pytz.timezone('Asia/Seoul')

@router.post("/check-availability")
async def check_favorites_availability(
    req: NotificationCheckRequest,
    user_id: str = Depends(get_current_user_id)
):
    """
    사용자가 즐겨찾기한 방들 중, 
    '현재 시각 + N분' 시점에 수업/예약이 없는 방을 찾아 알림 데이터를 반환합니다.
    
    Ex) 수업이 10시 50분에 끝나는데, 10시 40분(10분 전)에 이 API를 호출하면,
        10시 50분부터 비어있게 되므로 '알림 대상'으로 판단합니다.
    """
    try:
        # 1. 확인 기준 시간 계산 (현재 시간 + n분)
        now_kst = datetime.now(KST)
        target_time = now_kst + timedelta(minutes=req.minutes_before)
        
        # 요일 포맷 (DB의 day_of_week ENUM과 일치시켜야 함. 예: 'Mon', 'Tue'...)
        # Python weekday(): 0=Mon, 6=Sun
        days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        current_day_str = days[target_time.weekday()]
        
        target_time_str = target_time.strftime("%H:%M:%S") # 시간 비교용 문자열

        # 2. 사용자의 즐겨찾기 방 ID 목록 가져오기
        fav_res = supabase.table("favorites")\
            .select("room_id, rooms(room_number, buildings(code))")\
            .eq("user_id", user_id)\
            .execute()
            
        if not fav_res.data:
            return {"message": "No favorites found", "alerts": []}

        alerts = []

        for item in fav_res.data:
            room_id = item['room_id']
            room_info = item['rooms']
            building_code = room_info['buildings']['code']
            room_number = room_info['room_number']

            # 3. 해당 시각(target_time)에 '수업'이 있는지 확인 (timetable_entries)
            # 조건: 해당 요일 AND (시작시간 <= 타겟 <= 종료시간)
            # 겹치는 수업이 '하나라도 있으면' 사용 중인 것.
            timetable_res = supabase.table("timetable_entries")\
                .select("id")\
                .eq("room_id", room_id)\
                .eq("day", current_day_str)\
                .lte("start_time", target_time_str)\
                .gt("end_time", target_time_str)\
                .execute()

            # 4. 해당 시각(target_time)에 '예약'이 있는지 확인 (reservations)
            # 조건: 확정된 예약 AND (시작시간 <= 타겟 <= 종료시간)
            reservation_res = supabase.table("reservations")\
                .select("id")\
                .eq("room_id", room_id)\
                .eq("status", "confirmed")\
                .lte("start_at", target_time.isoformat())\
                .gt("end_at", target_time.isoformat())\
                .execute()

            # 수업도 없고, 예약도 없으면 -> '비어있음(Available)' -> 알림 대상
            is_occupied = (len(timetable_res.data) > 0) or (len(reservation_res.data) > 0)
            
            if not is_occupied:
                # 알림 메시지 생성
                alerts.append({
                    "room_id": room_id,
                    "room_name": f"{building_code}관 {room_number}호",
                    "target_time": target_time.strftime("%H:%M"),
                    "minutes_left": req.minutes_before,
                    "message": f"곧 {building_code}관 {room_number}호가 빕니다! ({req.minutes_before}분 후)"
                })

        # 실제 앱에서는 여기서 FCM(Firebase Cloud Messaging)을 호출하여 Push를 보냄
        return {
            "checked_at": now_kst.isoformat(),
            "alerts_count": len(alerts),
            "alerts": alerts
        }

    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))