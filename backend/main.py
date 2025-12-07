from fastapi import FastAPI
from api import auth, favorites, notifications

app = FastAPI(
    title="Classroom Informer API",
    description="Backend API for Team 7 Classroom Informer Project",
    version="1.0.0"
)

@app.get("/")
def root():
    return {"message": "Classroom Informer API is running!"}

if __name__ == "__main__":
    import uvicorn
    # 로컬 개발용 실행 설정
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)