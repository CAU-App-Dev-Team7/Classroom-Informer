-- schema.sql
-- Classroom Informer – core database schema
-- Run this on a fresh database (or compare carefully with an existing one before running).

-- =========================
-- Extensions
-- =========================
create extension if not exists "uuid-ossp";
create extension if not exists "btree_gist";

-- =========================
-- Types
-- =========================
-- Day of week enum used by timetable_entries
do $$
begin
  if not exists (
    select 1
    from pg_type
    where typname = 'day_of_week'
  ) then
    create type public.day_of_week as enum (
      '월', '화', '수', '목', '금', '토', '일'
    );
  end if;
end$$;

-- =========================
-- TABLE: buildings
-- =========================
create table if not exists public.buildings (
  id      bigint generated always as identity primary key,
  code    text not null unique,
  name    text
);

-- =========================
-- TABLE: rooms
-- =========================
create table if not exists public.rooms (
  id          bigint generated always as identity primary key,
  building_id bigint not null references public.buildings(id),
  room_number text   not null,
  capacity    integer,
  room_type   text,
  features    text,
  photo_url   text
);

-- =========================
-- TABLE: raw_timetable_csv
-- (staging table for original CSV rows)
-- =========================
create table if not exists public.raw_timetable_csv (
  day         text not null,
  start_time  time not null,
  end_time    time not null,
  code        text,
  course      text,
  department  text,
  professor   text,
  room        text,
  building    text,
  source_file text
);

-- =========================
-- TABLE: profiles
-- (user profiles, linked to auth.users)
-- =========================
create table if not exists public.profiles (
  id         uuid primary key references auth.users(id),
  name       text,
  role       text default 'student',   -- 'student' | 'professor' | 'admin'
  created_at timestamptz default now()
);

-- =========================
-- TABLE: favorites
-- (user's favorite rooms)
-- =========================
create table if not exists public.favorites (
  user_id    uuid   not null references public.profiles(id) on delete cascade,
  room_id    bigint not null references public.rooms(id)     on delete cascade,
  created_at timestamptz default now(),
  constraint favorites_pkey primary key (user_id, room_id)
);

-- =========================
-- TABLE: reservations
-- (room reservations made by professors/admins)
-- =========================
create table if not exists public.reservations (
  id         bigint generated always as identity primary key,
  room_id    bigint not null references public.rooms(id),
  user_id    uuid   not null default auth.uid()
                     references public.profiles(id),
  start_at   timestamptz not null,
  end_at     timestamptz not null,
  purpose    text,
  status     text default 'confirmed',  -- e.g. 'confirmed', 'cancelled'
  created_at timestamptz default now()
);

-- Prevent overlapping reservations for the same room
-- NOTE: columns are timestamptz, so use tstzrange (not tsrange).
alter table public.reservations
  add constraint reservation_no_overlap
  exclude using gist (
    room_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
  );

-- Optional: ensure start_at < end_at
alter table public.reservations
  add constraint reservation_valid_time
  check (start_at < end_at);

-- =========================
-- TABLE: timetable_entries
-- (normalized semester timetable)
-- =========================
create table if not exists public.timetable_entries (
  id          bigint generated always as identity primary key,
  room_id     bigint not null references public.rooms(id) on delete cascade,
  day         public.day_of_week not null,
  start_time  time not null,
  end_time    time not null,
  course_code text,
  course_name text,
  department  text,
  instructor  text,
  source      text default 'Final_timetable.csv',
  created_at  timestamptz default now()
);

-- Helpful indexes for queries
create index if not exists idx_rooms_building_room
  on public.rooms (building_id, room_number);

create index if not exists idx_timetable_room_day_time
  on public.timetable_entries (room_id, day, start_time, end_time);

create index if not exists idx_reservations_room_time
  on public.reservations (room_id, start_at, end_at);

create index if not exists idx_favorites_user
  on public.favorites (user_id);

-- 1) Needed once per database
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 2) Start must be before end
DO $$
BEGIN
  ALTER TABLE public.reservations
  ADD CONSTRAINT reservation_start_before_end
  CHECK (start_at < end_at);
EXCEPTION
  WHEN duplicate_object THEN
    NULL; -- constraint already exists
END $$;

-- 3) No overlapping reservations for the same room
DO $$
BEGIN
  ALTER TABLE public.reservations
  ADD CONSTRAINT reservation_no_overlap
  EXCLUDE USING gist (
    room_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
  );
EXCEPTION
  WHEN duplicate_object THEN
    NULL; -- constraint already exists
END $$;

