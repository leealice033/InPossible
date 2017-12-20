import requests
import datetime
import time
import csv
import datetime
"""return a tuple (utc timestamp, bitcoin's price)"""
def getBtcPrice():
    url = "https://api.coindesk.com/v1/bpi/currentprice.json"
    response = requests.get(url)
    bitJson = response.json()
    time = bitJson["time"]["updated"]
    monthToNumber = {"Jan":1, "Feb":2, "Mar":3, "Apr":4, "May":5, "Jun":6, "Jul":7, "Aug":8, "Sep":9,
                     "Oct":10, "Nov":11, "Dec":12}
    timeSplitted = time.replace(",", "").split()
    #print(timeSplitted)
    month = monthToNumber[timeSplitted[0]]
    day = int(timeSplitted[1])
    year = int(timeSplitted[2])
    h_m = timeSplitted[3].split(":")
    hour = int(h_m[0])
    minute = int(h_m[1])

    date = datetime.datetime(year, month, day, hour, minute)
    UTC = int(date.timestamp())
    #print(UTC)
    price = bitJson["bpi"]["USD"]["rate_float"]
    #print(time)
    #print(price)
    return UTC, price



if __name__ == "__main__":
   while(True):
       timestamp, price = getBtcPrice()
       value = datetime.datetime.fromtimestamp(timestamp)
       print(value.strftime('%Y-%m-%d %H:%M:%S'), price)
       new_data = [timestamp, price]
       with open(r'bitcoin_price.csv', 'a') as f:
           writer = csv.writer(f)
           writer.writerow(new_data)
       time.sleep(60)
