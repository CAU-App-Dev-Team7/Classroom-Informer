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
    r.id AS room_id,
    b.code AS building_code,
    r.room_number AS room_number
  FROM public.rooms r
  JOIN public.buildings b
    ON r.building_id = b.id
  CROSS JOIN now_korea n
  LEFT JOIN public.timetable_entries te
    ON te.room_id = r.id
   AND te.day = n.dow
   AND n.t BETWEEN te.start_time AND te.end_time
  WHERE
    te.id IS NULL
    AND (p_building_code IS NULL OR b.code = p_building_code)
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

CREATE EXTENSION IF NOT EXISTS btree_gist;
