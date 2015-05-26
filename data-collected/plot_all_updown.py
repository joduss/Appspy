import csv
import matplotlib.pyplot as plt
import sys
import glob
import plot_updown as plot
import utils
import os

#use: python3 plot.py inputCSV [1/0 to show also downloads]

def plotAll(csvDirectory, includeDownload=False, wasForeground=False, blocking=False):



	if(os.path.isfile(csvDirectory)):
		plot.plot(csvDirectory + '.csv', utils.toBool(includeDownload), utils.toBool(wasForeground), utils.toBool(blocking))
		plt.show()		
	else:
		csv_files = glob.glob(csvDirectory + '/*.csv')

		for csv in csv_files:
			print(csv)
			plot.plot(csv, utils.toBool(includeDownload), utils.toBool(wasForeground), utils.toBool(blocking))
		plt.show()




if __name__=='__main__':
	if(len(sys.argv) == 3):
		plotAll(sys.argv[1], sys.argv[2],True)
	elif len(sys.argv) == 4:
		plotAll(sys.argv[1], sys.argv[2], sys.argv[3])
	elif len(sys.argv) == 5:
		plotAll(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
	else:
		print("\033[91m Usage:\n plot csvDirectory includeDownload wasForeground=False blocking=True \033[0m")
		print("wasForeground indicate what data will be shown: one on the foreground or on background")