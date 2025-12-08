from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException
from core.config import supabase
from core.dependencies import get_current_user_id

router = APIRouter(
    prefix="/timetable",
    tags=["Timetable"]
)

# GET /timetable  → 현재 로그인한 학생의 시간표 조회
@router.get("/", summary="Get student timetable")
def get_student_timetable(
    student_id: str = Depends(get_current_user_id)
):
    try:
        response = (
            supabase
            .table("student_timetable")
            .select("*")
            .eq("student_id", student_id)
            .order("day")
            .order("start_time")
            .execute()
        )

        if response.data is None:
            return []

        return response.data

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to fetch timetable: {str(e)}"
        )
