import glob
import sys
import create_csv as cc
import matplotlib.pyplot as plt
import plot_all_updown as plot
import create_all_csv
import utils



	
def process(dbDirectory, packageName, plotDownload=False, wasForeground=False):

	#print(dbDirectory + " " + packageName + " " + str(plotDownload))

	create_all_csv.create_all_csv(dbDirectory, packageName)

	plot.plotAll(dbDirectory, plotDownload, wasForeground)




if __name__=='__main__':
	l = len(sys.argv)
	if(l >= 3 and l <= 5):
		process(*sys.argv[1:])
	else:
		print("\033[91m Usage:\n create_all_csv dbDirectory packageName plotDownload=False wasForeground=False \033[0m\n")



