from fastapi import Header, HTTPException, status
from config import supabase

# 인증된 사용자 ID(UUID)를 가져오는 의존성 함수
async def get_current_user_id(
    authorization: str = Header(..., description="Format: Bearer <token>")
) -> str:
    """
    HTTP Header의 'Authorization'에서 Bearer Token을 추출하여
    Supabase Auth 서버를 통해 유효성을 검증하고 User UUID를 반환합니다.
    """
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing or invalid Authorization header"
        )

    token = authorization.split(" ")[1]

    try:
        # Supabase 클라이언트를 사용하여 토큰 유효성 검사 (User 정보 조회)
        # 토큰이 만료되었거나 조작되었다면 여기서 예외가 발생하거나 user가 None으로 반환됨
        user_response = supabase.auth.get_user(token)
        
        if not user_response or not user_response.user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid or expired token"
            )
            
        return user_response.user.id

    except Exception as e:
        # Supabase Auth 관련 에러 처리
        print(f"Auth Error: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials"
        )