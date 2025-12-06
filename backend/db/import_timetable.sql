
--from raw_timetable_csv → buildings, rooms, timetable_entries

-- 0. Make sure source_file has a value
UPDATE public.raw_timetable_csv
SET source_file = 'Final_timetable.csv'
WHERE source_file IS NULL;

-- 1. Insert buildings (distinct building codes from CSV)
INSERT INTO public.buildings (code, name)
SELECT DISTINCT
  raw.building AS code,
  raw.building || '관' AS name
FROM public.raw_timetable_csv raw
WHERE raw.building IS NOT NULL
ON CONFLICT (code) DO NOTHING;

-- 2. Insert rooms (per building)
INSERT INTO public.rooms (building_id, room_number)
SELECT DISTINCT
  b.id        AS building_id,
  raw.room    AS room_number
FROM public.raw_timetable_csv raw
JOIN public.buildings b
  ON b.code = raw.building
WHERE raw.building IS NOT NULL
  AND raw.room IS NOT NULL
ON CONFLICT (building_id, room_number) DO NOTHING;

-- 3. Insert normalized timetable entries
INSERT INTO public.timetable_entries
  (room_id, day, start_time, end_time,
   course_code, course_name, department, instructor, source)
SELECT
  r.id AS room_id,
  raw.day::public.day_of_week AS day,
  raw.start_time,
  raw.end_time,
  raw.code       AS course_code,
  raw.course     AS course_name,
  raw.department,
  raw.professor  AS instructor,
  COALESCE(raw.source_file, 'Final_timetable.csv') AS source
FROM public.raw_timetable_csv raw
JOIN public.buildings b
  ON b.code = raw.building
JOIN public.rooms r
  ON r.building_id = b.id
 AND r.room_number = raw.room;
