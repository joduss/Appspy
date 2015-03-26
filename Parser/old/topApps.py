from bs4 import BeautifulSoup
from urllib.request import *
import re


class Crawler:
	def topAppsPackageName():
		topAppsURL = ["https://play.google.com/store/apps/collection/topselling_free?hl=en", 
		"https://play.google.com/store/apps/collection/topselling_paid?hl=en",
		"https://play.google.com/store/apps/category/GAME/collection/topselling_free?hl=en",
		"https://play.google.com/store/apps/category/GAME/collection/topselling_paid?hl=en",
		"https://play.google.com/store/apps/category/SOCIAL/collection/topselling_free?hl=en", #TOP APP IN SOCIAL (potentially the most vulnerable apps)
		"https://play.google.com/store/apps/category/GAME_CASUAL/collection/topselling_free?hl=en",
		"https://play.google.com/store/apps/category/GAME_CASUAL/collection/topselling_paid?hl=en",
		"https://play.google.com/store/apps/category/GAME_STRATEGY/collection/topselling_paid",
		"https://play.google.com/store/apps/category/GAME_STRATEGY/collection/topselling_free"
		]


		packageName = set()


		for url in topAppsURL:
			print("new url is being processed")
			for start in range(1,500,120):
				pageUrl = ""
				pageUrl = url + "&start=" + str(start) + "&num=120"
				#print(pageUrl + "\n")

				response = urlopen(pageUrl)

				soup = BeautifulSoup(response.read())

				#tag = soup.find_all("div", attrs={"data-docid":u re.compile("(([a-zA-Z]+){1}(\.[a-zA-Z]+)+)")})



				#def containsPackageName(tag):
				#	return tag.has_attr('div')  #& (tag.get("data-docid") != "none")

				#tag = soup.find_all(containsPackageName)

				tag = soup.find_all('span', attrs = {'class':'preview-overlay-container'})


				for x in tag: 
					#print(x.encode('utf-8'))
					#print(x.get("data-docid"))
					packageName.add(x.get("data-docid"))


				#print(len(tag))

		print( "Num of apps " + str(len(packageName)))

		for n in packageName:
			print(n)
		# https://play.google.com/store/apps/category/ARCADE/collection/topselling_paid?start=#{start}&num=24&hl=en




