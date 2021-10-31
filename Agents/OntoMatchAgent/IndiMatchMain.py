from matchManager import *
from matchers.Penalizer import *
from alignment import Alignment
from PlusImport import PlusImport

import os
import owlready2
import time
import csv

starttime = time.time()
#classmatchSteps = ['StringMatcher', 'BOWMatcher', 'DomainMatcher']
#cw = [0.4, 0.4, 0.2]

#cparas = [None, None,[("model/modellevel2/model30t5p5a.gensim", "model/modellevel2/dictionarylevel2.gensim"), 'compare']]

#cm = matchManager(classmatchSteps, "C:/Users/Shaocong/WORK/ontoMatchData/dbpedia_2014.owl", "C:/Users/Shaocong/WORK/ontoMatchData/ontology/PowerPlant.owl", thre=0.6, weight=cw, paras=cparas)

#ca = cm.runMatch()

############################################################################################

'''
ppfiles = []
for root, dirs, files in os.walk("./testFiles/germany", topdown=False):
   for name in files:
      ppfiles.append(os.path.join(root, name))
for root, dirs, files in os.walk("./testFiles/India", topdown=False):
   for name in files:
      ppfiles.append(os.path.join(root, name))

for root, dirs, files in os.walk("./testFiles/noiseSet", topdown=False):
  for name in files:
      ppfiles.append(os.path.join(root, name))

for root, dirs, files in os.walk("./testFiles/canada", topdown=False):
   for name in files:
      ppfiles.append(os.path.join(root, name))

temp = PlusImport(ppfiles,'./temp/temp3countryJPS.nt')


ppfiles=[]
for root, dirs, files in os.walk("./testFiles/gppd0722", topdown=False):
   for name in files:
      ppfiles.append(os.path.join(root, name))

temp = PlusImport(ppfiles,'./temp/gppd0722.owl')

ppfiles2=[]
for root, dirs, files in os.walk("./testFiles/kwl", topdown=False):
   count = 0
   for name in files:
      ppfiles2.append(os.path.join(root, name))
      count = count + 1
      if count == 4:
          break

tempkwl = PlusImport(ppfiles2,'./temp/kwltest3.owl')
def getFileList(folder):
    ppfiles = []
    for root, dirs, files in os.walk(folder, topdown=False):
        for name in files:
            ppfiles.append(os.path.join(root, name))
    return ppfiles


def findFile(fileId, filelist):
    for name in filelist:
        if fileId in name:
            return name
    return None
kmlFileList = getFileList("./testFiles/kwl")
gppdFileList = getFileList("./testFiles/gppd0722")

finalKmlList = set()
finalgppdList = set()

with open('./falseNegNoErr0816.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if len(row)>0:
            kmlFileName = findFile(row[3],kmlFileList)
            gppdFileName = findFile(row[4],gppdFileList)
            finalKmlList.add(kmlFileName)
            finalgppdList.add(gppdFileName)


tempkwl = PlusImport(list(finalKmlList),'./temp/kwlFalseNegSmall.owl')
tempgppd = PlusImport(list(finalgppdList),'./temp/gppdFalseNegSmall.owl')



'''
matchSteps = ['ValueMatcher','instanceStringMatcher', 'instanceBOWMatcher']

w = [0.5, 0.4, 0.1]

paras = [None,None,None]

#dbfiles = ['testFiles/dbpediaInstances/queryResultGermanCleaned.xml','./testFiles/dbpedia_2014.owl',
#           'testFiles/wgs84_pos.rdf',('testFiles/eml.ttl',"nt") ]
#todo:how to find right imports files for dbpedia?
import owlready2
#tempdb = PlusImport(dbfiles, './temp/temp2.xml')


clist = [('PowerStation', 'PowerPlant',0.9)]
sublist = ['RenewablePlant', 'FossilFuelPlant', 'HydroelectricPlant', 'HydrogenPlant', 'NuclearPlant', 'CogenerationPlant', 'GeothermalPlant', 'MarinePlant', 'BiomassPlant', 'WindPlant', 'SolarPlant','WastePlant','PowerPlant']
for subc in sublist:
    for subc in sublist:
        clist.append((subc,subc,0.9))


m = matchManager(matchSteps,'./testFiles/4_dukes_all.owl','./testFiles/4_gppdb_all.owl', thre=0.6, weight=w, paras=paras,matchIndividuals =True,penalize ={'class':True,'align':Alignment(clist)},useAttrFinder=False)

a, _ = m.runMatch("matchWrite2Matrix", to1=False, rematch = False)

#m.showResult(m.A,'individualList')
m.renderResult(" http://dbpedia.org/resource", "http://www.theworldavatar.com", 'Test0916Dukes.owl', True)

#a = m.runMatch("matchIndividuals", to1 = 'True')

#m.save(a, './result/indiResultIndia.pkl')
timenow = time.time()-starttime

print(timenow)