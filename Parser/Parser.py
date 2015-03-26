from bs4 import BeautifulSoup
from urllib.request import *
from Crawler import *


class AppInfo:
	category = ""
	download = ""
	rating = 0
	ratingCount = 0
	packageName = ""

	def __init__(self, packageName, category, download, rating, ratingCount):
		self.category = category
		self.download = download
		self.rating = rating
		self.ratingCount = ratingCount
		self.packageName = packageName

	def printAppInfo(self):
		print(self.packageName + " :" + self.category + ", downloads:" + self.download + ", rating:" + str(self.rating) + ", ratingCount:" + str(self.ratingCount))
		#print("salut")



infos = set()

def parse(packageNames):

	i = 1

	text_file = open("appinfo.txt", "w")
	text_file.write("packageName\tcategory\tdownloads\trating\tratingCount\n")

	text_file.close()

	for packageName in packageNames:
		#packageName = "com.gamesforfriends.trueorfalse.de"
		url = "https://play.google.com/store/apps/details?hl=en&id=" + packageName
		#print(url)
		packageName.replace("\n","")

		response = urlopen(url)

		#print(response.read())

		soup = BeautifulSoup(response.read())

		#get category
		#####
		categoryTag = soup.find("span", attrs={"itemprop": "genre"})
		#print(categoryTag)
		category = ""
		if(categoryTag):
			category = categoryTag.text

		#get ratings and stats related
		####
		rating = soup.find("meta", attrs={"itemprop" : "ratingValue"}).get("content")
		ratingCount = soup.find("meta", attrs={"itemprop" : "ratingCount"}).get("content")

		#get download number
		####
		downloadsTag = soup.find("div", attrs={"itemprop": "numDownloads"})
		downloads = ""
		#print(downloadsTag)
		if(downloadsTag):
			downloads = downloadsTag.text



		info = AppInfo(packageName, category, downloads, rating, ratingCount)
		info.printAppInfo()
		print(str(i) + "/" + str(len(packageNames))+ "\n")

		infos.add(info)
		i = i+1

		text_file = open("appinfo.txt", "a")
		text_file.write(packageName + "\t" + category + "\t" + downloads + "\t" + rating + "\t" + ratingCount + "\n")
		text_file.close()



		#print(packageName)
		#text_file.write(str(packageName) + "\n")



	


