/* 
------------------------------
Constants
------------------------------
*/

TWA_ABOX_IRI_PREFIX = "http://www.theworldavatar.com/kb/"

/* 
------------------------------
Custom classes
------------------------------
*/

class HttpError extends Error {
    constructor(statusCode) {
        super("HTTP Error")
        this.statusCode = statusCode
    }
}

/* 
------------------------------
Global variables
------------------------------
*/

let isProcessing = false
let isShowingIRI = false
let table = null

/* 
------------------------------
Functions that manipulate UI
------------------------------
*/

function hideElems() {
    const elemIds = ["preprocessed-question", "query-domain", "trans-latency", 'kg-latency', 'sparql-query-predicted-container', 'sparql-query-postprocessed-container', "error-container", "results", "toggle-iri", "chatbot-response"]
    for (const elemId of elemIds) {
        document.getElementById(elemId).style.display = "none"
    }
}

function displayDomainPredicted(domain) {
    document.getElementById("query-domain").innerHTML = `<p>Predicted query domain: ${domain}</p>`;
    document.getElementById('query-domain').style.display = "block";
}

function displayTranslationLatency(trans_latency) {
    const elem = document.getElementById("trans-latency")
    elem.innerHTML = `Translation latency: ${trans_latency.toFixed(2)}s.`
    elem.style.display = "block";
}

function displayKgExecLatency(kg_latency) {
    const elem = document.getElementById("kg-latency")
    elem.innerHTML = `SPARQL query execution latency: ${kg_latency.toFixed(2)}s.`
    elem.style.display = "block";
}

function displayPreprocessedQuestion(question) {
    elem = document.getElementById("preprocessed-question")
    elem.innerHTML = `<p style="margin: auto;"><strong>The input query has been reformatted to the following</strong></p><p style="margin: auto; color: gray;">${question}</p>`
    elem.style.display = "block";
}

function displaySparqlQueryPredicted(sparql_query) {
    document.getElementById("sparql-query-predicted").innerHTML = sparql_query
    document.getElementById('sparql-query-predicted-container').style.display = "block";
}

function displaySparqlQueryPostProcessed(sparql_query) {
    document.getElementById("sparql-query-postprocessed").innerHTML = sparql_query;
    document.getElementById('sparql-query-postprocessed-container').style.display = "block";
}

function displayTranslationResults(json) {
    if (json["question"] != json["preprocessed_question"]) {
        displayPreprocessedQuestion(json["preprocessed_question"])
    }
    displayDomainPredicted(json["domain"])
    displayTranslationLatency(json["latency"])

    displaySparqlQueryPredicted(json["sparql"]["predicted"])
    if (json["sparql"]["postprocessed"]) {
        displaySparqlQueryPostProcessed(json["sparql"]["postprocessed"])
    } else {
        displayError("The model is unable to generate a well-formed query. Please try reformulating your question.")
    }
}

function displayKgResponse(data) {
    if (!data) {
        displayError("The generated SPARQL query is malformed and cannot be executed against the knowledge base.")
        return
    }
    let content = "<table id='results-table' class='table table-striped table-bordered' style='width: 100%;'><thead><tr>"

    let vars = data["head"]["vars"].slice();
    if (data["results"]["bindings"].length > 0) {
        vars = vars.filter(varname => varname in data["results"]["bindings"][0])
    }

    content += "<th>#</th>"
    vars.forEach(varname => {
        content += `<th>${varname}</th>`
    });
    content += "</tr></thead><tbody>"

    data["results"]["bindings"].forEach((valueset, idx) => {
        content += `<tr><td>${idx + 1}</td>`
        vars.forEach(varname => {
            if (varname in valueset) {
                content += `<td>${valueset[varname]["value"]}</td>`
            } else {
                content += "<td></td>"
            }
        })
        content += "</tr>"
    })

    content += "</tbody></table>"
    document.getElementById("table-container").innerHTML = content;
    document.getElementById("toggle-iri").style.display = "block"
    document.getElementById("results").style.display = "block"

    table = new DataTable('#results-table', {
        retrieve: true,
        scrollX: true,
    });

    isShowingIRI = true
    toggleIRIColumns()
}

async function streamChatbotResponseBodyReader(reader) {
    const elem = document.getElementById("chatbot-response")

    // read() returns a promise that resolves when a value has been received
    reader.read().then(function pump({ done, value }) {
        if (done) {
            // Do something with last chunk of data then exit reader
            return;
        }
        // Otherwise do something here to process current chunk
        value = value.trim()
        if (value.startsWith("data: ")) {
            value = value.substring("data: ".length)
        }
        datum = JSON.parse(value)

        elem.innerHTML += datum["content"]
        if (/\s/.test(elem.innerHTML.charAt(0))) {
            elem.innerHTML = elem.innerHTML.trimStart()
        }

        // Read some more, and call this function again
        return reader.read().then(pump);
    });
}

function displayError(message) {
    elem = document.getElementById("error-container")
    elem.innerHTML = message
    elem.style.display = "block"
}


/* 
----------------------------------------
Functions that respond to onclick events
----------------------------------------
*/

function populateInputText(text) {
    document.getElementById('input-field').value = text
    window.scrollTo(0, 0);
}

function addToInputText(text) {
    document.getElementById('input-field').value += text
}

async function askQuestion() {
    if (isProcessing) { // No concurrent questions
        return;
    }

    const question = document.getElementById("input-field").value;
    if (question === "") {
        return;
    }

    hideElems();

    isProcessing = true;
    document.getElementById('ask-button').className = "mybutton spinner"

    const trans_results = await fetchTranslation(question)
    displayTranslationLatency(trans_results["latency"])
    displayTranslationResults(trans_results)

    const kg_results = await fetchKgResults(trans_results["domain"], trans_results["sparql"]["postprocessed"])
    displayKgExecLatency(kg_results["latency"])
    displayKgResponse(kg_results["data"])

    const chatbotResponseReader = await fetchChatbotResponseReader(question, kg_results["data"])
    streamChatbotResponseBodyReader(chatbotResponseReader)

    isProcessing = false;
    document.getElementById('ask-button').className = "mybutton"
}

function toggleIRIColumns() {
    if (table === null) {
        return
    }

    const rowNum = table.rows().count()
    if (rowNum == 0) {
        return
    }

    isShowingIRI = !isShowingIRI
    const rowData = table.row(0).data()
    const IRIcolIdx = rowData.reduce((arr, val, idx) => {
        if (val.startsWith(TWA_ABOX_IRI_PREFIX)) {
            arr.push(idx)
        }
        return arr
    }, [])
    IRIcolIdx.forEach(colIdx => {
        const col = table.column(colIdx)
        col.visible(isShowingIRI)
    })

    if (isShowingIRI) {
        document.getElementById("toggle-iri").innerHTML = "Hide IRIs"
    } else {
        document.getElementById("toggle-iri").innerHTML = "Show IRIs"
    }
}

/* 
----------------------------------------
API calls
----------------------------------------
*/

function handleError(error) {
    if (error instanceof HttpError) {
        if (error.statusCode == 500) {
            displayError("An internal server error is encountered. Please try again.");
        }
    }
}

function fetchTranslation(question) {
    return fetch("/translate", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ question })
    }).then(res => {
        if (!res.ok) {
            throw new HttpError(res.status)
        }
        return res.json()
    }).catch(handleError)
}

function fetchKgResults(domain, sparql_query) {
    return fetch("/kg", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ domain, sparql_query })
    }).then(res => {
        if (!res.ok) {
            throw new HttpError(res.status)
        }
        return res.json()
    }).catch(handleError)
}

function fetchChatbotResponseReader(question, data) {
    const elem = document.getElementById("chatbot-response")
    elem.innerText = ""
    elem.style.display = "block"

    const bindings = data["results"]["bindings"].map(binding => Object.keys(binding).reduce((obj, k) => {
        if (!binding[k]["value"].startsWith(TWA_ABOX_IRI_PREFIX)) {
            obj[k] = binding[k]["value"]
        }
        return obj
    }, {}))

    return fetch("/chatbot", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ question, data: JSON.stringify(bindings) })
    }).then(res => {
        if (!res.ok) {
            throw new HttpError(res.status)
        }
        return res.body.pipeThrough(new TextDecoderStream()).getReader()
    }).catch(handleError)
}
