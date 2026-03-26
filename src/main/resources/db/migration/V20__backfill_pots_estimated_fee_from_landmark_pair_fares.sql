UPDATE pots p
JOIN landmark_pair_fares lpf
  ON lpf.departure_id = p.departure_id
 AND lpf.destination_id = p.destination_id
SET p.estimated_fee = ROUND(
    (
      lpf.estimated_fare *
      (
        CASE
          WHEN HOUR(p.departure_time) IN (22, 2, 3) THEN 1.2
          WHEN HOUR(p.departure_time) IN (23, 0, 1) THEN 1.4
          ELSE 1.0
        END
      )
    ) / 10
  ) * 10
WHERE p.estimated_fee = 0;
