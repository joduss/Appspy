SELECT  * MINUS (
        SELECT  *
        FROM    view_app_activity
        WHERE   m2.record_id < m1.record_id and package_name like '%what%'
        ORDER BY 
                record_time
		LIMIT 1
        )
FROM view_app_activity m1
where package_name like '%what%'
ORDER BY
      record_id