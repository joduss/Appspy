import glob
import sys
import create_csv as cc
import matplotlib.pyplot as plt
import plot_all_updown as plot
import create_all_csv
import utils


def processWithAggregation(dbDirectory, packageName, aggregatedTime, plotDownload=False , backfore=False):

	if(str(backfore) == "False" or backfore == "Background"):
		wasForeground = False
	elif(backfore == "Foreground"):
		wasForeground = True
	else:
		raise NameError("backfore not found (processWithAggregation)")

	create_all_csv.create_all_csv(dbDirectory=dbDirectory, package_name=packageName, aggregatedTime=aggregatedTime, keepForeBack=backfore)
	plot.plotAll(dbDirectory, plotDownload, wasForeground)


if __name__=='__main__':
	l = len(sys.argv)
	if(l >= 4 and l <= 6):
		processWithAggregation(*sys.argv[1:])
	else:
		print("\033[91m Usage:\n create_all_csv dbDirectory packageName aggregatedTime=False plotDownload=False  backfore=False \033[0m\n" + 
			"backfore: if false, will mix fore+back ground, else, specify \"Background\" or \"Foreground\"")
