-- functions.sql
-- Classroom Informer – custom functions

-- Make sure required extensions exist
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- =====================================================
-- 1) Available rooms right now
--    Uses timetables + reservations, filtered by building
-- =====================================================
CREATE OR REPLACE FUNCTION public.available_rooms_now(p_building_code text)
RETURNS TABLE(room_id bigint, building_code text, room_number text)
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
  RETURN QUERY
  WITH now_korea AS (
    SELECT
      now() AT TIME ZONE 'Asia/Seoul' AS now_ts,
      (now() AT TIME ZONE 'Asia/Seoul')::time AS t,
      CASE
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 1 THEN '월'::day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 2 THEN '화'::day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 3 THEN '수'::day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 4 THEN '목'::day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 5 THEN '금'::day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 6 THEN '토'::day_of_week
        ELSE '일'::day_of_week
      END AS dow
  )
  SELECT
    r.id         AS room_id,
    b.code       AS building_code,
    r.room_number
  FROM public.rooms r
  JOIN public.buildings b
    ON r.building_id = b.id
  CROSS JOIN now_korea n
  WHERE
    -- if p_building_code is given, limit to that building
    (p_building_code IS NULL OR b.code = p_building_code)
    -- not in a scheduled class
    AND NOT EXISTS (
      SELECT 1
      FROM public.timetable_entries te
      WHERE te.room_id = r.id
        AND te.day = n.dow
        AND n.t BETWEEN te.start_time AND te.end_time
    )
    -- not reserved at this moment
    AND NOT EXISTS (
      SELECT 1
      FROM public.reservations rs
      WHERE rs.room_id = r.id
        AND rs.status = 'confirmed'
        AND n.now_ts BETWEEN rs.start_at AND rs.end_at
    )
  ORDER BY r.room_number;
END;
$function$;

-- =====================================================
-- 2) handle_new_user trigger
--    Automatically create a profile row when a new auth user is created
-- =====================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
  -- Create a profile row for this new auth user.
  -- Only 'id' is required; other columns use defaults.
  INSERT INTO public.profiles (id)
  VALUES (NEW.id)
  ON CONFLICT (id) DO NOTHING;  -- avoid error if row already exists

  RETURN NEW;
END;
$function$;
