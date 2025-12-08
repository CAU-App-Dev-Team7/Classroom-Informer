from fastapi import APIRouter, HTTPException, status
from core.config import supabase
from model.models import UserSignupSchema, UserLoginSchema, TokenResponse

router = APIRouter(
    prefix="/auth",
    tags=["Authentication"]
)

# 1. 회원가입 (Sign Up)
@router.post("/signup", status_code=status.HTTP_201_CREATED)
async def signup(user_data: UserSignupSchema):
    try:
        # Supabase Auth 회원가입 요청
        auth_response = supabase.auth.sign_up({
            "email": user_data.email,
            "password": user_data.password,
            "options": {
                "data": {
                    "full_name": user_data.name or "" 
                    # 여기서 보낸 메타데이터는 trigger에 의해 profiles 테이블로 복사될 수 있음
                }
            }
        })
        
        if not auth_response.user:
            raise HTTPException(status_code=400, detail="Signup failed")

        return {"message": "User created successfully. Please check your email for verification if enabled."}

    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


# 2. 로그인 (Log In)
@router.post("/login", response_model=TokenResponse)
async def login(user_data: UserLoginSchema):
    try:
        # Supabase Auth 로그인 요청
        auth_response = supabase.auth.sign_in_with_password({
            "email": user_data.email,
            "password": user_data.password
        })

        if not auth_response.session:
            raise HTTPException(status_code=401, detail="Invalid credentials")

        return TokenResponse(
            access_token=auth_response.session.access_token,
            refresh_token=auth_response.session.refresh_token,
            user_id=auth_response.user.id
        )

    except Exception as e:
        # 보안을 위해 에러 메시지는 모호하게 처리하거나 로그로 남김
        print(f"Login Error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, 
            detail="Invalid email or password"
        )