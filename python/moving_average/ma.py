import numpy as np
import matplotlib.pyplot as plt

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

def getwma(data, smaPeriod):
    """returned a numpy array of wma
    @param data: numpy array
    @smaPeriod: int. 5WMA or 10WMA, for instance
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
                w=np.linspace(smaPeriod,1,smaPeriod)
                res=np.dot(w,data[i-smaPeriod+1:i+1])/np.sum(w)
                sma.append(res)
    return np.array(sma)

def getplot(*args):
    """plot MAs over time
    @args[0]: time array
    @args[1]: matype('SMA' or 'WMA')
    @args[2]: list of length of MA (5,25,60,200).Length of this list must be the same as the number of lines to be plotted
    @argv.. : MA array to be plotted
    """
    timePeriod=args[0]
    matype=args[1]
    periodList=args[2]
    for i in range(3,len(args)):
        plt.plot(timePeriod,args[i],label=(str(periodList[i-3])+' '+matype))
    plt.legend()
    plt.show()

time=np.linspace(1,20,20)
a=np.random.rand(20,1)
s3=getsma(a,3)
s5=getwma(a,5)
matype='SMA'
periodList=[5,3]
getplot(time,matype,periodList,s5,s3)
