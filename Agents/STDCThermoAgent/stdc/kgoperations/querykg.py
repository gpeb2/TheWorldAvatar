from SPARQLWrapper import SPARQLWrapper, JSON


def querykg(sparqlEndPoint=None, queryStr=None):   
    sparql = SPARQLWrapper("http://www.theworldavatar.com/blazegraph/namespace/" + sparqlEndPoint + "/sparql")
    sparql.setQuery(queryStr)
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()
    results = results['results']['bindings']

    for res in results:
        for key in res:
            if(isinstance(res[key], dict)):
                res[key] = res[key]['value']

    return results
        