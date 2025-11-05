# Backend Structure Overview (FastAPI)

This README explains the purpose of each folder in the backend project, helping collaborators and future you understand the structure.

## /api
- Defines FastAPI routers (endpoints)
- Handles HTTP requests and responses
  
**Example: `api/users.py`**
```python
from fastapi import APIRouter
from supabase.client import supabase
from schema.users import UserCreate

router = APIRouter()

@router.post("/")
def create_user(user: UserCreate):
    response = supabase.table("users").insert({
        "name": user.name,
        "email": user.email
    }).execute()
    return response.data
```


## /schema
- Defines Pydantic models
- Validates request, response, and database data structures
**Example: `schema/users.py`**
```python
from pydantic import BaseModel

class UserCreate(BaseModel):
    name: str
    email: str

class UserResponse(BaseModel):
    id: int
    name: str
```

## /db
- Handles Supabase (or ORM) related configuration
- Manages database connection and CRUD operations
**Example: `db/client.py`**
```python
from supabase import create_client
import os

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)
```
