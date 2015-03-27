from bs4 import BeautifulSoup
from urllib.request import *
from Crawler import *
from multiprocessing import Pool, Lock
import urllib
import time
import math
from multiprocessing import Process

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
      print(self.packageName + " :" + self.category + ", downloads:" + self.download +
            ", rating:" + str(self.rating) + ", ratingCount:" + str(self.ratingCount))
      # print("salut")


infos = list()


def sq(ab):
   print(ab)


def parse(packageNames):


   #	p = Pool(4)

   #	p.map(sq, list(range(1,100)))

   text_file = open("appinfo.txt", "w")
   text_file.write("packageName\tcategory\tdownloads\trating\tratingCount\n")

   text_file.close()

   #p = Pool(12)

   proc = list()
   #infos = p.map(parallelFunc, packageNames)

   lenDiv = math.floor(len(packageNames) / 12)

   lock = Lock()

   for i in range(0,12):
      start = i * lenDiv
      stop = (i+1) * lenDiv - 1
      print(start)
      print(stop)
      if(i != 11):
         proc.append(Process(target=parallelFunc,args=(packageNames[start:stop],lock,)))
      else:
         proc.append(Process(target=parallelFunc,args=(packageNames[start:],lock,)))

      #p.join()

   for i in range(0,12) : 
      proc[i].start()
      print("starting " + str(i))

   # for packageName in packageNames:
   #packageName = "com.gamesforfriends.trueorfalse.de"

   # print(packageName)
   #text_file.write(str(packageName) + "\n")

   # for info in infos:
   #    category = info.category
   #    downloads = info.download
   #    rating = info.rating
   #    ratingCount = info.ratingCount
   #    packageName = info.packageName

   #    if(category != "ERROR"):
   #       text_file = open("appinfo.txt", "a")
   #       text_file.write(packageName + "\t" + category + "\t" + downloads + "\t" + rating + "\t" + ratingCount + "\n")
   #       text_file.close()
   #    else:
   #       error_file = open("error.txt", "a")
   #       error_file.write(packageName + "\n")
   #       error_file.close()


def parallelFunc(packageNames, lock):

   print("CCCC " + packageNames[0] + str(len(packageNames)))

   numOk = 0
   for packageName in packageNames:
      numOk = numOk + 1
      print("Processing: " + str(numOk) + "\n")

      trials = 0

      while(trials <=20):
         try:

            url = "https://play.google.com/store/apps/details?hl=en&id=" + packageName
            # print(url)
            packageName.replace("\n", "")

            response = urlopen(url)

            # print(response.read())

            soup = BeautifulSoup(response.read())

            # get category
            #####
            categoryTag = soup.find("span", attrs={"itemprop": "genre"})
            # print(categoryTag)
            

            # get ratings and stats related
            ####
            ratingTag = soup.find("meta", attrs={"itemprop": "ratingValue"})
            ratingCountTag = soup.find("meta", attrs={"itemprop": "ratingCount"})

            trials = trials + 1

            if((ratingTag is not None) & (ratingCountTag is not None) & (categoryTag is not None)):
               category = categoryTag.text
               rating = ratingTag.get("content")
               ratingCount = ratingCountTag.get("content")

               # get download number
               ####
               downloadsTag = soup.find("div", attrs={"itemprop": "numDownloads"})
               downloads = ""
               # print(downloadsTag)
               if(downloadsTag):
                  downloads = downloadsTag.text

               info = AppInfo(packageName, category, downloads, rating, ratingCount)
               info.printAppInfo()
               #print(str(i) + "/" + str(len(packageNames))+ "\n")


               trials = 50
               #print("1 done")
               lock.acquire()
               text_file = open("appinfo.txt", "a")
               text_file.write(packageName + "\t" + category + "\t" + downloads + "\t" + rating + "\t" + ratingCount + "\n")
               text_file.close()
               lock.release()
         
               #return info

            elif(trials >= 20):
               print("Huge error for packageName:" + packageName)
               lock.acquire()
               error_file = open("error.txt", "a")
               error_file.write(packageName + "\n")
               error_file.close()
               lock.release()
            else:
               time.sleep(2)

         except urllib.error.HTTPError:
            print("http error. Continue...")
            trials = trials + 1
            time.sleep(2)
