import requests

def get_data(period='1hr', type='BTC'):
    acceptedType = ['BTC', 'ETH', 'LTC']
    acceptedPeriod = {'1hr': 60, '12hr': 60*12, '24hr': 60*24, '7days': 24*7, '30days': 24*30}
    if (type not in acceptedType) or (period not in acceptedPeriod):
        return None
    urlHead = "https://min-api.cryptocompare.com/data/histominute?tsym=USD&fsym="
    if period=='30days' or period=='7days':
        urlHead = urlHead.replace('minute', 'hour')
    url = urlHead+type+'&limit='+str(acceptedPeriod[period])
    response = requests.get(url)
    response = response.json()
    #print(len(response['Data']))
    timePriceDict = {}
    timePriceDict['timestamp'] = []
    timePriceDict['price'] = []
    data = response['Data']
    for i in data:
        timePriceDict['timestamp'].append(i['time']+8*60)
        timePriceDict['price'].append(i['open'])
    return timePriceDict

if __name__ == "__main__":
    a = get_data('7days', 'LTC')
    print(a['timestamp'])
    print(a['price'])