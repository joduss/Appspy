from bs4 import BeautifulSoup
from urllib.request import *
from Crawler import *



packageNames = topAppsPackageName(true)
text_file = open("p.txt", "w")


for packageName in packageNames:
	#packageName = "com.gamesforfriends.trueorfalse.de"

	response = urlopen("https://play.google.com/store/apps/details?id=" + packageName + "&hl=en")

	#print(response.read())

	soup = BeautifulSoup(response.read())
	tag = soup.find("span", attrs={"itemprop":u"genre"})

	#category = print(tag.text)

	#print(packageName)
	text_file.write(str(packageName) + "\n")

text_file.close()


