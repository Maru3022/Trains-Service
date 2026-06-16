import urllib.request, urllib.parse, json
service='registry.docker.io'
scope='repository:bitnami/kafka:pull'
url='https://auth.docker.io/token?service={}&scope={}'.format(urllib.parse.quote(service, safe=''), urllib.parse.quote(scope, safe=''))
with urllib.request.urlopen(url) as resp:
    token=json.load(resp)['token']
req=urllib.request.Request('https://registry-1.docker.io/v2/bitnami/kafka/tags/list?n=100')
req.add_header('Authorization', 'Bearer ' + token)
with urllib.request.urlopen(req) as resp:
    data=json.load(resp)
print(data.get('count'))
print('\n'.join(data.get('results', [])))
