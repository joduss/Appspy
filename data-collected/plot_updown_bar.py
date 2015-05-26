import csv
import matplotlib.pyplot as plt
import sys
import datetime
import utils


#use: python3 plot.py inputCSV [1/0 to show also downloads]

def plot(file, showDownload, wasForeground, aggregatedTime=1, blocking=True):
	print("PLOT: file:" + file + "showDownload:"+ str(showDownload) + "  wasForeground:" + str(wasForeground) + "  blocking:" + str(blocking))
	record_id_idx = 0
	record_time_idx = 1
	package_name_idx = 2
	foreground_time_usage_idx = 3 
	last_time_use_idx = 4 
	downloaded_data_idx = 5 
	uploaded_data_idx = 6
	was_foreground_idx = 7
	boot_idx = 8

	showDownload = utils.toBool(showDownload)
	wasForeground = utils.toBool(wasForeground)
	blocking = utils.toBool(blocking)
	aggregatedTime = int(aggregatedTime)

	dataY = []
	dataY_download = []

	dataX = []
	dataX_download = []



	with open(file, 'rt') as csvfile:
		reader = csv.reader(csvfile, delimiter=',', quotechar='"')




		lastF = 1
		lastF1 = 1
		title = ""

		next(reader)
		next(reader)

		begin = 0
		end = 0

		for row in reader :
			#print("row 9")
			#print(row[9])
			if (begin == 0):
				begin = int(row[record_time_idx])

			if (int(row[record_time_idx]) > end):
				end = int(row[record_time_idx])


			if(wasForeground == False):
				title = "data in background"
				if(row[was_foreground_idx] == "0" and int(lastF) == 0 and int(lastF1) == 0):
					#print("addded")
					if (int(row[uploaded_data_idx]) > 0):
						dataX.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY.append(int(row[uploaded_data_idx])/1024)
					if int(row[downloaded_data_idx]) > 0 :
						dataY_download.append(int(row[downloaded_data_idx])/1024)
						dataX_download.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
				lastF1 = lastF		
				lastF = row[was_foreground_idx]
			else:
				title = "data in foreground"
				if(row[was_foreground_idx] == "1" or int(lastF) == 1 or int(lastF1) == 1):
					if (int(row[uploaded_data_idx]) > 0):
						dataX.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY.append(int(row[uploaded_data_idx])/1024)
					if int(row[downloaded_data_idx]) > 0 :
						dataY_download.append(int(row[downloaded_data_idx])/1024)
						dataX_download.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))




	width = 1 / ((end-begin)/aggregatedTime/1000/60/5) * 2

	#print(dataX)
	fig, f = plt.subplots()
	#plt.plot_date(dataX, dataY, 'ro')



	if(showDownload == True):
		r1 = f.bar(dataX, dataY, width, color="b")
		f.legend(['download'])
	else:
		r1 = f.bar(dataX, dataY, width, color="r")
		f.legend(['upload'])

	plt.ylabel('Uploaded data [kB]')
	plt.title(title)
	f.xaxis_date()



	plt.show(blocking)



################

if __name__=='__main__':
	if(len(sys.argv) == 4):
		plot(sys.argv[1], sys.argv[2], sys.argv[3])
	elif len(sys.argv) == 5:
		plot(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
	elif len(sys.argv) == 6:
		plot(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
	else:
		print("\033[91m Usage:\n plot csvFile showDownload wasForeground aggregatedTime=1 blocking=True \033[0m")
		print("wasForeground indicate what data will be shown: one on the foreground or on background")
		print("showDownload: show download if set to true. If false, show upload")


