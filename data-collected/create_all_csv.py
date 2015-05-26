import sys
import glob
import create_csv
import os



def create_all_csv(dbDirectory, package_name, aggregatedTime = False):
#dbname = sys.argv[1]
#package_name = sys.argv[2]

	if(os.path.isfile(dbDirectory)):
		create_csv.create_csv(dbDirectory,package_name, aggregatedTime)
	else:
		db_files = glob.glob(dbDirectory + '/*.db')

		for db in db_files:
			create_csv.create_csv(db,package_name, aggregatedTime)
	





################

if __name__=='__main__':
	if(len(sys.argv) == 3):
		create_all_csv(sys.argv[1], sys.argv[2])
	elif len(sys.argv) == 4:
		create_all_csv(sys.argv[1], sys.argv[2], sys.argv[3])
	else:
		print("\033[91m Usage:\n create_all_csv dbName packageName aggregatedTime=False \033[0m")		
