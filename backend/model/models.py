from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
from datetime import datetime

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