#KEY: iwx6raw9m7nqq0f (this should not be in the Version Control). 

#TEST from page 193: https://www.elexon.co.uk/documents/training-guidance/bsc-guidance-notes/bmrs-api-and-data-push-user-guide-2/
#This corresponds to this information: https://www.bmreports.com/bmrs/?q=actgenration/actualgeneration

###Libraries###
import httplib2
from pprint import pformat

import re
import pandas as pd
import sys
from datetime import datetime, timedelta


#Also note that the "post_elexon_basic" function is not used, but might be useful for just querying without processing at all if you are learning / testing. 



####Functions###
def post_elexon_basic(url):
    #Query BMRS. This function is not used, but is good if you are learning. Simply run this with the commented out lines to see the output. 
    http_obj = httplib2.Http()
    resp, content = http_obj.request(
        uri=url,
        method='GET',
        headers={'Content-Type': 'application/xml; charset=UTF-8'},
    )
    print('===Response===')
    print(pformat(resp))
    
    print('===Content===')
    print(pformat(content))
    print(type(pformat(content)))

    print('===Finished===')


def post_elexon(url):
    #Query BMRS. 
    http_obj = httplib2.Http()
    resp, content = http_obj.request(
        uri=url,
        method='GET',
        headers={'Content-Type': 'application/xml; charset=UTF-8'},
    )
    #print('===Response===')
    #print(pformat(resp))
    
    #print('===Content===')
    return str(pformat(content))
    #print(type(pformat(content)))

    #print('===Finished===')


def float_chars(test):
    #Check if all characters in the string "test" are valid for a float.
    valid = "0987654321."
    for i in test:
        if i not in valid:
            return 0
    return 1


def ext_chars(test):
    #Check if all characters in the string "test" are valid for a float.
    valid = "0987654321.-_ "
    for i in test:
        if i not in valid:
            return 0
    return 1


def xmlStringProcessToArray(xmlInfo):
    #Takes the big bulk of text and splits it up into a readable array.
    xmlInfo = xmlInfo.replace("'\n b'","")
    xmlInfo = xmlInfo.split("<item>")
    #Now each element in xmlInfo should contain a potential generator and its output.
    #A dictionary should be created such that each ID obtains an output.
    unitOutput = {}
    for item in xmlInfo:
        #Check if this is a valid generator, with an output.
        #Will check if there is a single ID (NGCBMUnitID) and output (quantity). Thus, these terms should exist twice each, with a <> and </> case. 
        if (2 == len(item.split("<nGCBMUnitID>"))) and (2 == len(item.split("</nGCBMUnitID>"))) and (3 == len(item.split("nGCBMUnitID"))) and (2 == len(item.split("<quantity>"))) and (2 == len(item.split("</quantity>"))) and (3 == len(item.split("quantity"))):
            #Extract the ID and Quantity values. 
            ID = re.search("<nGCBMUnitID>(.+?)</nGCBMUnitID>", str(item))
            quantity = re.search("<quantity>(.+?)</quantity>", str(item))
            #Check if this worked, if so, just use the values extracted, rather than the current information, which includes if the extraction worked. 
            if (ID) and (quantity):
                ID = str(ID.group(1))
                quantity = str(quantity.group(1))
                #Check the value contains only valid characters. 
                if (float_chars(quantity)):
                    #Add these values to the dictionary. 
                    unitOutput[ID] = float(quantity)
                    #print(item)
                    #print(ID)
                    #print(quantity)
    return unitOutput


def run_query(Key, Year, Month, Day, Period):
    #The query function
    Key = str(Key)
    Year = str(Year)
    Month = str(Month)
    Day = str(Day)
    Period = str(Period)
    querystring = 'https://api.bmreports.com/BMRS/B1610/v2?APIKey='+Key+'&SettlementDate='+Year+'-'+Month+'-'+Day+'&Period='+Period+'&NGCBMUnitID=&ServiceType=xml'
    
    #Query All
    #post_elexon_basic(url='https://api.bmreports.com/BMRS/B1610/v2?APIKey=iwx6raw9m7nqq0f&SettlementDate=2021-01-01&Period=1&NGCBMUnitID=&ServiceType=xml',) #All 
    #xmlString = post_elexon(url='https://api.bmreports.com/BMRS/B1610/v2?APIKey=iwx6raw9m7nqq0f&SettlementDate=2021-01-01&Period=1&NGCBMUnitID=&ServiceType=xml',) #All
    xmlString = post_elexon(url=querystring,) #All 
    
    #Specific
    #WHILW #48WSTN1000WHILWQ
    #post_elexon_basic(url='https://api.bmreports.com/BMRS/B1610/v2?APIKey=iwx6raw9m7nqq0f&SettlementDate=2021-01-01&Period=1&NGCBMUnitID=WHILW&ServiceType=xml',) #HEYM2

    #Try to format information
    IDOutput = xmlStringProcessToArray(xmlString) #The ID Output dictionary. 
    #print(IDOutput)
    #print(xmlString)
    
    return IDOutput

def str0_2(value):
    #Converts the day or month int to a string of length 2. Thus, 12 -> "12", and 1 -> "01", the leading 0 is important.
    #This function thus performs a similar role as str(), but also can add the 0 at the start, and is applied to length 2 instances.
    #Must use for day or month, but use for period is optional, as length 1 and 2 is accepted by the API format the period, but not the month or day. 
    value = str(value)
    if len(value) == 1:
        value = "0"+value
    return value

def empty_query_response(liveGeneratorData):
    #See if there are a few outputs recieved. If not, this should not replace the old data.
    if (len(liveGeneratorData) > 3):
        return 0 #It is NOT empty.
    return 1 #It is empty

def check_valid_time(Key, Year, Month, Day, Period):
    #Very similar to the Primary Function (live_power), but does not proceed to proces the data...
    #... instead, it only checks if data for a given time was recieved.
    #Returns 0 if data is not recieved, and 1 if it was. 
    liveGeneratorData = run_query(Key, Year, Month, Day, Period) #Dict of generation units and their outputs.
    if empty_query_response(liveGeneratorData):
        #See if anything was recieved in this query.
        return 0
    return 1

def incriment_time(TryYear, TryMonth, TryDay, TryPeriod):
    #Incriment the checked time by 1 period.
    TryPeriod += 1 #1 for a 1 period increase, in testing make this 48 to go up in days. 
    if TryPeriod > 48:
        #Have incrimented to a new day. 
        TryPeriod = 1
        date = datetime(TryYear, TryMonth, TryDay)
        date += timedelta(days=1)
        TryYear = int(date.year)
        TryMonth = int(date.month)
        TryDay = int(date.day)
    return TryYear, TryMonth, TryDay, TryPeriod

def find_recent_time(Key, Year, Month, Day, Period):
    #Keep incrimenting (not in excess of the current day), to try to find the most recent time since the one given as a starting point (Year, Month, Day Period).
    TryYear = int(Year)
    TryMonth = int(Month)
    TryDay = int(Day)
    TryPeriod = int(Period)
    today = datetime.today() #Loop breaks if it tries to look past this date, as data won't exist for the future, given that it notes past data. 
    while ((check_valid_time(Key, str(TryYear), str0_2(TryMonth), str0_2(TryDay), str0_2(TryPeriod)) == 1) and (today > datetime(TryYear, TryMonth, TryDay))):
        #This time works, so accept it. 
        Year = str(TryYear)
        Month = str0_2(TryMonth)
        Day = str0_2(TryDay)
        Period = str0_2(TryPeriod)
        #Incriment the tried time again.
        TryYear, TryMonth, TryDay, TryPeriod = incriment_time(TryYear, TryMonth, TryDay, TryPeriod)
    return Year, Month, Day, Period


###Primary Function###
def live_power(ExcelName, Key, Year, Month, Day, Period, Search):
    #The first input is the name of the excel spreadsheet to use.
    #The Key is the API key for BMRS. 
    #The other are for the time (
        #Year, eg. 2020 (four characters),
        #Month, eg. 01 (two characters, the first being a zero if it is <10, and a number, not the name of the month),
        #Day, eg. 01 (two characters, the first being a zero if it is <10, this being the day into the month, so 1-31),
        #Period, eg. 01 (could be 1 or two characters, as using a zero if it is <10 is optional).  The period is the half hour into the day, from 1-48.
        #Search (0 or 1), if set to 0, the only time queried will be the one given. If it is 1, it will continue to try until it obtains the most recent time (incrimenting by periods).

    #As only a length of 2 is accepted for the day or month, (period can be either), i.e. "01" used instead of "1" ("20" would stay as "20"), we can make sure they are converted if they aren't already in this form.
    Month = str0_2(Month)
    Day = str0_2(Day)
    Period = str0_2(Period) #Optional step, but do it for consistency. 
    
    #The same time is used for the whole query.
    if Search == 1:
        Year, Month, Day, Period = find_recent_time(Key, Year, Month, Day, Period)
    liveGeneratorData = run_query(Key, Year, Month, Day, Period) #Dict of generation units and their outputs.
    if empty_query_response(liveGeneratorData):
        #See if anything was recieved in this query.
        return 0
    
    #Read DUKES Stations from Excel
    data = pd.read_excel(ExcelName) #Dataframe including DUKES stations.

    #Clear the outputs.
    for i in range(0,len(data['Output'])):
        data.iloc[i, data.columns.get_loc('Output')] = 0
        data.iloc[i, data.columns.get_loc('Year')] = Year
        data.iloc[i, data.columns.get_loc('Month')] = Month
        data.iloc[i, data.columns.get_loc('Day')] = Day
        data.iloc[i, data.columns.get_loc('Period')] = Period
    
    #Now go through the dataframe to get the IDs for each DUKES station. 
    for i in range(0,len(data['outputDUKESToBMRSID'])):
        #See if an ID is contained.
        if (data['outputDUKESToBMRSID'][i] != "na") and (data['outputDUKESToBMRSID'][i] != "none") and (data['outputDUKESToBMRSID'][i] != "None") and (data['outputDUKESToBMRSID'][i] != "") and (data['outputDUKESToBMRSID'][i] != "NA") and (data['outputDUKESToBMRSID'][i] != "nan"):
            #Now loop through the generators
            for gen in liveGeneratorData: 
                #Now see if there is a match. This is based on the generator ID containing the station ID, and the generator ID, when specifics are removed, being the same as the station ID.
                if (data['outputDUKESToBMRSID'][i] in gen):
                    if (gen.strip("0987654321.-_ ") == data['outputDUKESToBMRSID'][i]):
                        data.iloc[i, data.columns.get_loc('Output')] = float(data['Output'][i]) + liveGeneratorData[gen]
                    elif ext_chars(gen.replace(data['outputDUKESToBMRSID'][i], "")): 
                        #The station name could contain a number, which we can check with a looser method here. Here we see if, after removing the station name from the gen name, if the only remaining characters are numbers or in . -_". 
                        #print("TAKE2: " + data['outputDUKESToBMRSID'][i] + ", " + gen)
                        data.iloc[i, data.columns.get_loc('Output')] = float(data['Output'][i]) + liveGeneratorData[gen]
                    else:
                        #Code should not get here. Print differences for debugging. 
                        print(data['outputDUKESToBMRSID'][i] + ", " + gen)

    #Re-export to excel, with times and outputs. 
    data.to_excel(ExcelName, index = False)
    return 1



###Main Function###
if __name__ == "__main__":
    live_power('Input.xlsx', '', '2021', '11', '14', '46', 0)

