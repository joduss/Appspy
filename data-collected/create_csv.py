import sys
import csv
import sqlite3
import numpy
import datetime as dt


def create_csv(dbname, package_name, aggregatedTime="False"):
#dbname = sys.argv[1]
#package_name = sys.argv[2]


	db = sqlite3.connect(dbname)

	cursor = db.cursor()
	query = "SELECT record_id, record_time, package_name, foreground_time_usage, last_time_use, downloaded_data, uploaded_data, was_foreground, boot FROM Table_applications_activity where package_name=" + "'" + package_name + "'" + " ORDER BY record_time"


	cursor.execute(query)

	all_rows = cursor.fetchall()








	with open(dbname+ ".csv", 'wt') as csvfile:
		writer = csv.writer(csvfile, delimiter=',', quotechar='"')
		writer.writerow(["sep=,"])
		writer.writerow(["record_id", "record_time", "package_name", "foreground_time_usage", "last_time_use", "downloaded_data", "uploaded_data", "was_foreground", "boot", "record_time_human"])

		if(aggregatedTime=="False"):	
			for row in all_rows:
				writer.writerow(row)

		else:
			print("OFAUSDFOSADF" + str(len(all_rows)))

			if(len(all_rows) > 0):
				beginDate = dt.datetime.fromtimestamp(int(all_rows[0][1])//1000)
				day = beginDate.day
				month = beginDate.month
				year = beginDate.year

				#print(str(day) + " " + str(month) + " " + str(year))
				beginDate = dt.date(year, month, day)
				#print("millis: " + beginDate.strftime("%s"))
				beginMillis = int(beginDate.strftime("%s")) * 1000

				aggregatedTimeMillis = 1000 * int(aggregatedTime) * 60

				row_index = 0
				aggregatedIndex = 0

				aggregatedDataDownloadFore = 0
				aggregatedDataUploadFore = 0
				aggregatedDataDownloadBack = 0
				aggregatedDataUploadBack = 0
				recordTime = beginMillis


				while(row_index < len(all_rows)):
					row = all_rows[row_index]
					recordTime = int(row[1])

					wasFor = row[7]

					#print("hell:" + str(beginMillis) + "<" + str(recordTime) + "<" + str(aggregatedTimeMillis + beginMillis))
					print(row_index)
					print(len(all_rows))


					if(recordTime >= beginMillis and recordTime < (beginMillis + aggregatedTimeMillis)):
						print("coucou: " + str(int(row[6])))
						if(wasFor == True):
							aggregatedDataUploadFore = aggregatedDataUploadFore + int(row[6])
							aggregatedDataDownloadFore = aggregatedDataDownloadFore + int(row[5])
						else:
							aggregatedDataUploadBack = aggregatedDataUploadBack + int(row[6])
							aggregatedDataDownloadBack = aggregatedDataDownloadBack + int(row[5])
						row_index = row_index + 1
					else:
						#update beginMillis
						beginMillis = beginMillis + aggregatedTimeMillis
						#add actual (taht's right, after beginMillis. Because record what happens X minutes in the past compared to the record time)
						if(aggregatedDataDownloadBack > 0 or aggregatedDataUploadBack > 0):
							writer.writerow([-1, beginMillis, row[2], 0, 0, aggregatedDataDownloadBack, aggregatedDataUploadBack, 0, -1, dt.datetime.fromtimestamp(beginMillis//1000).strftime("%d.%m.%y-%H:%M")])
						if(aggregatedDataDownloadFore > 0 or aggregatedDataUploadFore > 0):
							writer.writerow([-1, beginMillis, row[2], 0, 0, aggregatedDataDownloadFore, aggregatedDataUploadFore, 1, -1, dt.datetime.fromtimestamp(beginMillis//1000).strftime("%d.%m.%y-%H:%M")])
						
						aggregatedDataDownloadFore = 0
						aggregatedDataUploadFore = 0
						aggregatedDataDownloadBack = 0
						aggregatedDataUploadBack = 0






if __name__=='__main__':
	if(len(sys.argv) == 3):
		create_csv(sys.argv[1], sys.argv[2])
	elif len(sys.argv) == 4:
		create_csv(sys.argv[1], sys.argv[2], sys.argv[3])
	else:
		print("\033[91m Usage:\n create_csv dbName packageName aggregatedTime=False \033[0m")


