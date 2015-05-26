import csv
import matplotlib
matplotlib.use('tkAgg')
import matplotlib.pyplot as plt
import sys
import datetime
import utils



#use: python3 plot.py inputCSV [1/0 to show also downloads]

def plot(file, updown="upload", bar=True, aggregatedTime=1):
	print("PLOT: file:" + file + "updown:"+ str(updown) + "  bar:" + str(bar))
	record_id_idx = 0
	record_time_idx = 1
	package_name_idx = 2
	foreground_time_usage_idx = 3 
	last_time_use_idx = 4 
	downloaded_data_idx = 5 
	uploaded_data_idx = 6
	was_foreground_idx = 7
	boot_idx = 8

	bar = utils.toBool(bar)

	dataY_back = []
	dataY_fore = []

	dataX_back = []
	dataX_fore = []

	dataX_bf = []
	dataY_bf = []

	aggregatedTime = int(aggregatedTime)





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

			if(updown == "download"):
					#print("addded")
				if (int(row[downloaded_data_idx]) > 0):
					if(row[was_foreground_idx] == "0" and lastF == 0 and lastF1 == 0):
						dataX_back.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY_back.append(int(row[downloaded_data_idx])/1024)
					elif(row[was_foreground_idx] == "1"):
						dataY_fore.append(int(row[downloaded_data_idx])/1024)
						dataX_fore.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
					else:
						dataY_bf.append(int(row[downloaded_data_idx])/1024)
						dataX_bf.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))

				lastF1 = lastF		
				lastF = int(row[was_foreground_idx])
				print(lastF)
				print(lastF1)
				print("___")
			else:
				if (int(row[uploaded_data_idx]) > 0):
					if(row[was_foreground_idx] == "0" and lastF == 0 and lastF1 == 0):
						dataX_back.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
						dataY_back.append(int(row[uploaded_data_idx])/1024)
					elif(row[was_foreground_idx] == "1"):
						dataY_fore.append(int(row[uploaded_data_idx])/1024)
						dataX_fore.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))
					else:
						dataY_bf.append(int(row[uploaded_data_idx])/1024)
						dataX_bf.append(datetime.datetime.fromtimestamp(int(row[record_time_idx])/1000))

				lastF1 = lastF		
				lastF = int(row[was_foreground_idx])				


	#print(dataX)

	if(updown == "download"):
		plt.title("download")		
	else:
		plt.title("upload")

	if(bar == False):
		plt.figure()



		plt.plot_date(dataX_back, dataY_back, 'ro')
		plt.plot_date(dataX_bf, dataY_bf, 'co')
		plt.plot_date(dataX_fore, dataY_fore, 'go')


		plt.ylabel('Data[kB]')
	else:
		fig, f = plt.subplots()
		#plt.plot_date(dataX, dataY, 'ro')
		width = 1 / ((end-begin)/aggregatedTime/1000/60/5) * 2


		print(width )

		r1 = f.bar(dataX_back, dataY_back, width,color="r")
		r2 = f.bar(dataX_bf, dataY_bf, width, color="c")
		r3 = f.bar(dataX_fore, dataY_fore, width, color="b")

		f.legend(["background","ibtw","foreground"])

		plt.ylabel('Uploaded data [kB]')
		plt.title(title)
		f.xaxis_date()

		f.set_yscale('log')



	plt.show(True)



################

if __name__=='__main__':
	if(len(sys.argv) == 2):
		plot(sys.argv[1])
	elif len(sys.argv) == 3:
		plot(sys.argv[1], sys.argv[2])
	elif len(sys.argv) == 4:
		plot(sys.argv[1], sys.argv[2], sys.argv[3])
	elif len(sys.argv) == 5:
		plot(sys.argv[1], sys.argv[2], sys.argv[3],sys.argv[4])
	else:
		print("\033[91m Usage:\n plot csvFile updown bar=False aggregatedTime=1 \033[0m")
		print("updown: indicate what to show [download, upload]")

