import sys
import glob
import plot_updown as plot
import utils
import os
import sqlite3



package_name = "'com.google.android.talk'"



db_files = glob.glob('data-processing/*.db')


print("WAS_FOREGROUND USAGES")
for dbName in db_files:
	db = sqlite3.connect(dbName)

	cursor = db.cursor()
	query = "SELECT sum(was_foreground) FROM Table_applications_activity where package_name=" + package_name

	cursor.execute(query)

	all_rows = cursor.fetchall()

	if(len(all_rows) == 1):
		print(dbName + ": " + str(all_rows[0][0]))


print("")
print("DATA UPLOADED[kB] ON BACKGROUND")
for dbName in db_files:
	db = sqlite3.connect(dbName)

	cursor = db.cursor()
	query = "SELECT sum(uploaded_data/1024.0) FROM Table_applications_activity where package_name=" + package_name + " AND was_foreground=0 AND uploaded_data > 0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1)"

	cursor.execute(query)

	all_rows = cursor.fetchall()

	if(len(all_rows) == 1):
		print(dbName + ": " + str(all_rows[0][0]))


print("")
print("DATA UPLOADED[kB] ON FOREGROUND")
for dbName in db_files:
	db = sqlite3.connect(dbName)

	cursor = db.cursor()
	query = "SELECT sum(uploaded_data/1024.0) FROM Table_applications_activity where package_name=" + package_name + " AND was_foreground=1 and downloaded_data > 0  AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1)"

	cursor.execute(query)

	all_rows = cursor.fetchall()

	if(len(all_rows) == 1):
		print(dbName + ": " + str(all_rows[0][0]))




#count


