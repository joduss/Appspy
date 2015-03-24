from bs4 import BeautifulSoup
from urllib.request import *
import re

topAppsURL = ["https://play.google.com/store/apps/collection/topselling_free?hl=en", 
"https://play.google.com/store/apps/collection/topselling_paid?hl=en"
"https://play.google.com/store/apps/category/GAME/collection/topselling_free?hl=en",
"https://play.google.com/store/apps/category/GAME/collection/topselling_paid?hl=en",
"https://play.google.com/store/apps/category/SOCIAL/collection/topselling_free?hl=en", #TOP APP IN SOCIAL (potentially the most vulnerable apps)
"https://play.google.com/store/apps/category/GAME_CASUAL/collection/topselling_free?hl=en",
"https://play.google.com/store/apps/category/GAME_CASUAL/collection/topselling_paid?hl=en"
]

response = urlopen(topAppsURL[0])

soup = BeautifulSoup(response.read())

#tag = soup.find_all("div", attrs={"data-docid":u re.compile("(([a-zA-Z]+){1}(\.[a-zA-Z]+)+)")})


def containsPackageName(tag):
	return tag.has_attr('div')  #& (tag.get("data-docid") != "none")

#tag = soup.find_all(containsPackageName)

tag = soup.find_all('span', attrs = {'class':'preview-overlay-container'})

packageName = set()

for x in tag: 
	#print(x.encode('utf-8'))
	print(x.get("data-docid"))
	packageName.add(x.get("data-docid"))


print(len(tag))


# https://play.google.com/store/apps/category/ARCADE/collection/topselling_paid?start=#{start}&num=24&hl=en




