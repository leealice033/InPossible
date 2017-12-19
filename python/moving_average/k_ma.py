import sys
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.finance as mpf
import matplotlib.dates as md
import datetime as dt
#@param data 
def getsma(data, smaPeriod):
    """returned a numpy array of sma
    @param data: numpy array
    @smaPeriod: int. 5SMA or 10SMA, for instance
    """
    sma = []
    count = 0
    for i in range(data.size):
        if data[i] is None:
            sma.append(None)
        else:
            count += 1
            if count < smaPeriod:
                sma.append(None)
            else:
                sma.append(np.mean(data[i-smaPeriod+1:i+1]))
    return np.array(sma)

def getNewData(time,data,viewPeriod):
    tmp1=data[:(int)(len(data)/viewPeriod)*viewPeriod]
    tmpt1=time[:(int)(len(time)/viewPeriod)*viewPeriod]
    d=np.split(tmp1,len(tmp1)/viewPeriod)
    dt=np.split(tmpt1,len(tmpt1)/viewPeriod)
    if len(data)%viewPeriod is not 0:
        d.append(data[(int)(len(data)/viewPeriod)*viewPeriod:])
        dt.append(time[(int)(len(time)/viewPeriod)*viewPeriod:])
    time=[]
    openn=[]
    close=[]
    high=[]
    low=[]
    for i in d:
        close.append(i[-1][0])
        openn.append(i[0][0])
        high.append(np.amax(i))
        low.append(np.amin(i))
    for i in dt:
        time.append(i[-1])
    return np.array(time),np.array(openn),np.array(close),np.array(high),np.array(low)

def getplot(*args):
    """plot MAs over time
    @args[0]: time array
    @args[1]: orig data, containing open, close, high, and low
    @args[2]: matype('SMA' or 'WMA')
    @args[3]: list of length of MA (5,25,60,200).Length of this list must be the same as the number of lines to be plotted
    @argv[4] : list of MA np-array to be plotted
    """
    TIME_LABEL_DENSE=5
    timePeriod=args[0]
    data=args[1]
    matype=args[2]
    periodList=args[3]
    ma=args[4]
    t=[]
    for i in timePeriod:
        date = dt.datetime.fromtimestamp(i/1e3)
        t.append(date)
        #t.append((md.date2num(date)))
    fig, ax = plt.subplots()
    plt.subplots_adjust(bottom=0.2)
    plt.xticks(rotation=25)
    ax.set_xticks(range(0, len(t), TIME_LABEL_DENSE))
    ax.set_xticklabels(t[::TIME_LABEL_DENSE])
    for i in range(len(ma)):
        plt.plot(ma[i],label=(str(periodList[i])+' '+matype))
    mpf.candlestick2_ochl(ax, data[0], data[1], data[2], data[3], width=0.5, colorup='r', colordown='g', alpha=0.75)
    plt.legend()
    plt.show()



def main(*argv):
    """ turn raw data into [open,close,high,low]; calculate MAs; plot candle diagrm and MA diagram
    @argv[0]: 1D np-array,Time
    @argv[1]: 1D np-array,Price
    @argv[2]: int,viewPriod(5min diagram / 10min / ....)
    @argv[3]: string,type of MA (SMA or WMA)
    @argv[4:]:int,the periods that want to calculate MA 
    """
    time=argv[0]
    price=argv[1]
    viewPeriod=argv[2]
    matype=argv[3]
    maPeriodList=argv[4:]
    
    #o stands for opening price of viewPeriod,c for closing price, h for highest price, l for lowest price
    #length of each array = ceil(len(orig data)/viewPeriod)
    time,o,c,h,l=getNewData(time,price,int(viewPeriod))
    dataList=[o,c,h,l]
    #when calculating MA, we use close price as data
    maList=[]
    for i in range(len(maPeriodList)):
        if matype is 'SMA':
            maList.append(getsma(c,maPeriodList[i]))
        elif matype is 'WMA':
            maList.append(getwma(c,maPeriodList[i]))
    getplot(time,dataList,matype,maPeriodList,maList)

if __name__ == "__main__":
   main(sys.argv[1:])
'''
a=np.linspace(1513641600,1513704540,200)
b=np.random.rand(200,1)
main(a,b,5,'SMA',3,5)
'''