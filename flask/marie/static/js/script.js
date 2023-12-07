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
        super("HTTP error")
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
    let elemIds = ["trans-metadata-card", "chatbot-response-card", 'sparql-query-predicted-container', 'sparql-query-postprocessed-container', "error-container", "results", "toggle-iri"]
    for (const elemId of elemIds) {
        document.getElementById(elemId).style.display = "none"
    }

    elemIds = ["trans-metadata-card", "chatbot-response-card"]
    for (const elemId of elemIds) {
        document.getElementById(elemId).innerHTML = ""
    }
}

function getTransMetadataCardUl() {
    const transMetadataCard = document.getElementById("trans-metadata-card")
    transMetadataCard.style.display = "block"

    const ulChildren = transMetadataCard.getElementsByTagName("ul")
    if (ulChildren.length === 0) {
        const ul = document.createElement("ul")
        ul.className = "list-group list-group-flush"
        transMetadataCard.appendChild(ul)
        return ul
    } else {
        return ulChildren[0]
    }
}

function displayDomainPredicted(domain) {
    const ul = getTransMetadataCardUl()
    ul.insertAdjacentHTML("beforeend", `<li class="list-group-item"><p style="margin: auto;">Predicted query domain: ${domain}</p></li>`)
}

function getLatencyLi() {
    const optional = document.getElementById("latency-info")
    if (!optional) {
        getTransMetadataCardUl().insertAdjacentHTML("beforeend", `
            <li class="list-group-item" id="latency-info"></li>`)
        return document.getElementById("latency-info")
    } else {
        return optional
    }
}

function formatLatency(latency) {
    if (typeof latency === "number") {
        return latency.toFixed(2)
    } else {
        return latency
    }
}

function displayLatency(id, desc, latency) {
    const li = getLatencyLi()
    li.insertAdjacentHTML("beforeend", `<p style="margin: auto;">${desc} latency: <span id="${id}">${formatLatency(latency)}</span>s.</p>`)
}

function updateLatency(id, latency) {
    document.getElementById(id).innerHTML = formatLatency(latency)
}

function displayPreprocessedQuestion(question) {
    const ul = getTransMetadataCardUl()
    ul.insertAdjacentHTML("beforeend", `
        <li class="list-group-item">
            <p style="margin: auto;">
                <strong>The input query has been reformatted to the following</strong>
            </p>
            <p style="margin: auto; color: gray;">${question}</p>
        </li>`)
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
    displayLatency("trans-latency", "Translation", json["latency"])

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

function displayKgResults(json) {
    displayLatency("kg-latency", "SPARQL query execution", json["latency"])
    displayKgResponse(json["data"])
}

function initChatbotResponseCard() {
    document.getElementById("chatbot-response-card").innerHTML = `
    <div class="card-body">
        <h5 class="card-title">Marie's response</h5>
        <p id="chatbot-response" style="margin: 0"></p>
        <div class="spinner-grow spinner-grow-sm text-primary" role="status" id="chatbot-spinner">
            <span class="sr-only">Loading...</span>
        </div>
    </div>`
}

function getChatbotResponseElem() {
    document.getElementById("chatbot-response-card").style.display = "block"
    const optional = document.getElementById("chatbot-response")
    if (!optional) {
        initChatbotResponseCard()
        return document.getElementById("chatbot-response")
    } else {
        return optional
    }
}

function getChatbotSpinner() {
    document.getElementById("chatbot-response-card").style.display = "block"
    const optional = document.getElementById("chatbot-spinner")
    if (!optional) {
        initChatbotResponseCard()
        return document.getElementById("chatbot-spinner")
    } else {
        return optional
    }
}

function showChatbotSpinner() {
    document.getElementById("chatbot-response-card").innerHTML = ""
    getChatbotSpinner().style.display = "block"
}

function hideChatbotSpinner() {
    getChatbotSpinner().style.display = "none"
}

async function streamChatbotResponseBodyReader(reader) {
    const elem = getChatbotResponseElem()

    displayLatency("chatbot-latency", desc = "Chatbot response", latency = "...")
    // read() returns a promise that resolves when a value has been received
    return reader.read().then(function pump({ done, value }) {
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
        updateLatency("chatbot-latency", datum["latency"])

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

    try {
        const trans_results = await fetchTranslation(question)
        displayTranslationResults(trans_results)

        const kg_results = await fetchKgResults(trans_results["domain"], trans_results["sparql"]["postprocessed"])
        displayKgResults(kg_results)
        console.log(kg_results)
        showChatbotSpinner()
        const chatbotResponseReader = await fetchChatbotResponseReader(question, kg_results["data"])
        await streamChatbotResponseBodyReader(chatbotResponseReader)
    } catch (error) {
        console.log(error)
        if ((error instanceof HttpError) && (error.statusCode == 500)) {
            displayError("An internal server error is encountered. Please try again.");
        } else {
            displayError("An unexpected error is encountered. Please report it with the following error message<br/>" + error)
        }
    } finally {
        isProcessing = false;
        document.getElementById('ask-button').className = "mybutton"
        hideChatbotSpinner()
    }
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

async function throwErrorIfNotOk(res) {
    if (!res.ok) {
        console.log(await res.text())
        throw new HttpError(res.status)
    }
    return res
}

function fetchTranslation(question) {
    return fetch("/translate", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ question })
    })
        .then(throwErrorIfNotOk)
        .then(res => res.json())
}

function fetchKgResults(domain, sparql_query) {
    return fetch("/kg", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ domain, sparql_query })
    })
        .then(throwErrorIfNotOk)
        .then(res => res.json())
}

function fetchChatbotResponseReader(question, data) {
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
    })
        .then(throwErrorIfNotOk)
        .then(res => res.body.pipeThrough(new TextDecoderStream()).getReader())
}
