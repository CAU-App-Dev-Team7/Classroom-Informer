from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List, Dict
from datetime import time, datetime

# --- Buildings 모델 ---
class BuildingResponse(BaseModel):
    id: int
    code: str
    name: Optional[str] = None

# --- Rooms 모델 ---
class RoomResponse(BaseModel):
    id: int
    building_id: int
    room_number: str
    capacity: Optional[int] = None
    room_type: Optional[str] = None
    features: Optional[str] = None
    photo_url: Optional[str] = None
    building_code: Optional[str] = None     
    # (참고) 내부적으로 JOIN용 객체는 유지하거나, 필요 없으면 제외 가능
    building: Optional[BuildingResponse] = None

# --- Timetable Entries 모델 ---
class TimetableEntryResponse(BaseModel):
    id: int
    room_id: int
    day: str  # public.day_of_week ENUM은 문자열로 처리
    start_time: time
    end_time: time
    course_code: Optional[str] = None
    course_name: Optional[str] = None
    department: Optional[str] = None
    instructor: Optional[str] = None
    source: Optional[str] = None
    created_at: Optional[datetime] = None

class FreeSlotDto(BaseModel):
    start: str
    end: str

class FreeSlotsResponseDto(BaseModel):
    building_code: str
    room_number: str
    free_slots_by_day: Dict[str, List[FreeSlotDto]]

# --- 즐겨찾기 관련 모델 ---
class FavoriteToggleRequest(BaseModel):
    room_id: int

class RoomDetail(BaseModel):
    id: int
    room_number: str
    building_code: str # building 테이블 조인 결과

class FavoriteResponse(BaseModel):
    user_id: str
    room_id: int
    created_at: datetime
    room: Optional[RoomDetail] = None # 상세 정보 포함

# --- 알림 설정 관련 모델 ---
class NotificationCheckRequest(BaseModel):
    # 사용자가 설정한 알림 시간 (예: 10분 전 알림이면 10)
    minutes_before: int = 10


# --- 회원가입, 로그인 관련 모델 ---
class UserSignupSchema(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6)
    name: Optional[str] = None # 프로필 이름

class UserLoginSchema(BaseModel):
    email: EmailStr
    password: str

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user_id: str