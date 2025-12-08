from fastapi import FastAPI
from api import auth, favorites, notifications, student_timetable

app = FastAPI(
    title="Classroom Informer API",
    description="Backend API for Team 7 Classroom Informer Project",
    version="1.0.0"
)

# 라우터 등록
app.include_router(auth.router)           # /auth/signup, /auth/login
app.include_router(favorites.router)      # /favorites (Protected)
app.include_router(notifications.router)  # /notifications (Protected)
app.include_router(student_timetable.router) #/timetable (Protected)


@app.get("/")
def root():
    return {"message": "Classroom Informer API is running!"}

if __name__ == "__main__":
    import uvicorn
    # 로컬 개발용 실행 설정
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)