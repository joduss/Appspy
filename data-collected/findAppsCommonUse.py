import sys
import glob
import plot_updown as plot
import utils
import os
import sqlite3

db_files = glob.glob('data-processing/*.db')



apps = []

for db in db_files:

	print(db)

	db = sqlite3.connect(db)

	cursor = db.cursor()
	query = "SELECT package_name FROM Table_installed_apps"

	cursor.execute(query)

	all_rows = cursor.fetchall()


	for row in all_rows:
		apps.append(row[0])


#count

dic = {}

for pn in apps:
	if pn in dic.keys():
		dic[pn] = dic[pn] + 1
	else:
		dic[pn] = 1


ok = [(x, dic[x]) for x in dic.keys() if dic[x] == 3 ]

for o in ok:
	print(o)


