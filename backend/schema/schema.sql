-- schema.sql
-- Core database schema for Classroom Informer (Supabase / Postgres)

-- 1. Enum for Korean weekdays
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_type
    WHERE typname = 'day_of_week'
      AND typnamespace = 'public'::regnamespace
  ) THEN
    CREATE TYPE public.day_of_week AS ENUM ('월','화','수','목','금','토','일');
  END IF;
END;
$$;

-- 2. Buildings (207, 208, 310, 505, ...)
CREATE TABLE IF NOT EXISTS public.buildings (
  id   BIGSERIAL PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,  -- e.g. '310'
  name TEXT                   -- e.g. '310관'
);

-- 3. Rooms (per building)
CREATE TABLE IF NOT EXISTS public.rooms (
  id           BIGSERIAL PRIMARY KEY,
  building_id  BIGINT NOT NULL REFERENCES public.buildings(id) ON DELETE CASCADE,
  room_number  TEXT   NOT NULL,      -- e.g. '603'
  capacity     INTEGER,
  room_type    TEXT,
  features     TEXT,
  CONSTRAINT rooms_unique_building_room UNIQUE (building_id, room_number)
);

-- 4. Raw timetable CSV staging table
--    Used only for import; data loaded from Supabase CSV.
CREATE TABLE IF NOT EXISTS public.raw_timetable_csv (
  day         TEXT NOT NULL,              -- '월','화',...
  start_time  TIME NOT NULL,
  end_time    TIME NOT NULL,
  code        TEXT,                       -- course code
  course      TEXT,                       -- course name
  department  TEXT,
  professor   TEXT,
  room        TEXT,                       -- room number, e.g. '603'
  building    TEXT,                       -- building code, e.g. '310'
  source_file TEXT                        -- e.g. 'Final_timetable.csv'
);

-- 5. Normalized timetable entries
CREATE TABLE IF NOT EXISTS public.timetable_entries (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  room_id      BIGINT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
  day          public.day_of_week NOT NULL,
  start_time   TIME NOT NULL,
  end_time     TIME NOT NULL,
  course_code  TEXT,
  course_name  TEXT,
  department   TEXT,
  instructor   TEXT,
  source       TEXT DEFAULT 'Final_timetable.csv',
  created_at   TIMESTAMPTZ DEFAULT now()
);

-- 6. Profiles (linked to Supabase auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
  id         UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  name       TEXT,
  role       TEXT DEFAULT 'student',  -- 'student','professor','admin'
  created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. Favorites (user’s favorite rooms)
CREATE TABLE IF NOT EXISTS public.favorites (
  user_id    UUID   NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  room_id    BIGINT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT now(),
  CONSTRAINT favorites_pkey PRIMARY KEY (user_id, room_id)
);

-- 8. Reservations (professors reserving rooms)
CREATE TABLE IF NOT EXISTS public.reservations (
  id         BIGSERIAL PRIMARY KEY,
  room_id    BIGINT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
  user_id    UUID   NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  start_at   TIMESTAMPTZ NOT NULL,
  end_at     TIMESTAMPTZ NOT NULL,
  purpose    TEXT,
  status     TEXT DEFAULT 'confirmed', -- 'pending','confirmed','cancelled'
  created_at TIMESTAMPTZ DEFAULT now()
);

-- 9. Helpful indexes for performance
CREATE INDEX IF NOT EXISTS idx_rooms_building_id
  ON public.rooms (building_id);

CREATE INDEX IF NOT EXISTS idx_timetable_room_day_time
  ON public.timetable_entries (room_id, day, start_time, end_time);

CREATE INDEX IF NOT EXISTS idx_reservations_room_time_status
  ON public.reservations (room_id, start_at, end_at, status);
