from fastapi import Request, HTTPException, status, Depends, Header
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from .config import supabase

bearer_scheme = HTTPBearer()

# 인증된 사용자 ID(UUID)를 가져오는 의존성 함수
async def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme)
) -> str:
    token = credentials.credentials

    try:
        user_response = supabase.auth.get_user(token)

        if not user_response or not user_response.user:
            raise HTTPException(
                status_code=401,
                detail="Invalid or expired token"
            )
        
        return user_response.user.id

    except Exception:
        raise HTTPException(
            status_code=401,
            detail="Could not validate credentials"
        )