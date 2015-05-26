import csv
import matplotlib.pyplot as plt
import sys
import datetime
import utils


#use: python3 plot.py inputCSV [1/0 to show also downloads]

def plot(file, alsoDownload, wasForeground=False, blocking=True):
	print("PLOT: file:" + file + "alsoDownload:"+ str(alsoDownload) + "  wasForeground:" + str(wasForeground) + "  blocking:" + str(blocking))
	record_id_idx = 0
	record_time_idx = 1
	package_name_idx = 2
	foreground_time_usage_idx = 3 
	last_time_use_idx = 4 
	downloaded_data_idx = 5 
	uploaded_data_idx = 6
	was_foreground_idx = 7
	boot_idx = 8

	alsoDownload = utils.toBool(alsoDownload)
	wasForeground = utils.toBool(wasForeground)
	blocking = utils.toBool(blocking)

	dataY = []
	dataY_download = []

	dataX = []
	dataX_download = []

	dataX_o = []
	dataY_o = []

	dataX_download_o = []
	dataY_download_o = []





	with open(file, 'rt') as csvfile:
		reader = csv.reader(csvfile, delimiter=',', quotechar='"')




		lastF = 1
		lastF1 = 1
		title = ""

		next(reader)
		next(reader)

		for row in reader :
			#print("row 9")
			#print(row[9])
			if(wasForeground == False):
				title = "data in background"
				if(row[was_foreground_idx] == "0" and lastF == 0 and lastF1 == 0):
					#print("addded")
					if (int(row[uploaded_data_idx]) > 0):
						dataX.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY.append(int(row[uploaded_data_idx])/1024)
					if int(row[downloaded_data_idx]) > 0 :
						dataY_download.append(int(row[downloaded_data_idx])/1024)
						dataX_download.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
				lastF1 = lastF		
				lastF = int(row[was_foreground_idx])
				print(lastF)
				print(lastF1)
				print("___")
			else:
				title = "data in foreground"
				if(row[was_foreground_idx] == "1"):
					if (int(row[uploaded_data_idx]) > 0):
						dataX.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY.append(int(row[uploaded_data_idx])/1024)
					if int(row[downloaded_data_idx]) > 0 :
						dataY_download.append(int(row[downloaded_data_idx])/1024)
						dataX_download.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
				lastF1 = lastF		
				lastF = int(row[was_foreground_idx])
			if(row[was_foreground_idx] == "0" and (lastF == 1 or lastF1 == 1)):
				if (int(row[uploaded_data_idx]) > 0):
					dataX_o.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
					dataY_o.append(int(row[uploaded_data_idx])/1024)
				if int(row[downloaded_data_idx]) > 0 :
					dataY_download_o.append(int(row[downloaded_data_idx])/1024)
					dataX_download_o.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))


	#print(dataX)
	f, ax = plt.subplots()
	plt.plot_date(dataX, dataY, 'ro')
	plt.plot_date(dataX_o, dataY_o, 'yo')



	if(alsoDownload == True):
		plt.plot_date(dataX_download, dataY_download, 'bo')
		plt.plot_date(dataX_download_o, dataY_download_o, 'co')
		ax.legend(('Upload','upload after user activity', 'download', 'download after user activity'), numpoints=1) 
	else:
		ax.legend(('Upload','upload after user activity'), numpoints=1) 

	plt.ylabel('Uploaded data [kB]')
	plt.title(title)
	plt.show(blocking)



################

if __name__=='__main__':
	if(len(sys.argv) == 3):
		plot(sys.argv[1], sys.argv[2])
	elif len(sys.argv) == 4:
		plot(sys.argv[1], sys.argv[2], sys.argv[3])
	elif len(sys.argv) == 5:
		plot(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
	else:
		print("\033[91m Usage:\n plot csvFile includeDownload wasForeground=False blocking=True \033[0m")
		print("wasForeground indicate what data will be shown: one on the foreground or on background")

