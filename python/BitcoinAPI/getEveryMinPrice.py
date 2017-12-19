import csv
import time
from datetime import datetime
import getBitcoinAPI

if __name__=="__main__":
    #outputFile = open("btcPrice.csv", 'a')
    while True:
        nowTime = int(time.time())
        nextTime = nowTime+60
        print(nowTime, datetime.fromtimestamp(nowTime).strftime('%Y-%m-%d %H:%M'))
        time_price = getBitcoinAPI.getBtcPrice()
        print(time_price)
        with open(r'btcPrice.csv', 'a') as outputFile:
            outputFile.write('%d,%f\n' % (nowTime, time_price[1]))
        newTime = int(time.time())
        time.sleep(nextTime-newTime)
