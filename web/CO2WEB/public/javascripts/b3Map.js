var b3map = new PopupMap({useCluster:true,  editable: true});

var socket = io();

socket.emit("join", JSON.stringify([{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/E-301.owl", withData:true}
    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/R-301.owl", withData:true}
,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/T-601002.owl", withData:true}
,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/R-302.owl", withData:true}
,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/P-302.owl", withData:true}

    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601008.owl", withData:true}
    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601001.owl", withData:true}
    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601002.owl", withData:true}
    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601003.owl", withData:true}
    ,{uri:"http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601004.owl", withData:true}


]));

let b3dm  = [
        ["V_molarF_3-1", 0],
        ["V_Temperature_3-1",0],
        ["V_molarF_3-4",0],
        ["V_Temperature_3-4",0],
        ["ValueOfOutletPressureOfP-302",0],
        ["V_molarF_Utility_FW-301",0]
    ]


let b2dm  =[
        ["V_molarF_601001",0],
        ["V_Temperature_601001",0],
		["V_Temperature_601002",0]
]     

var dataMaps = [
    { url:"http://www.theworldavatar.com/Service_Node_BiodieselPlant3/DoSimulation"
        ,dataMap:   new Map(b3dm)}

    ,{ url:"http://www.theworldavatar.com/Service_Node_BiodieselPlant3/DoSimulation2"
        ,dataMap:  new Map(b2dm) }
];
/**
        "V_molarF_601001":0,
        "V_Temperature_601001":0
        **/
//need to keep a copy of initial, data
socket.on('initial', function (idata) {
//extract data we need by name
    console.log(idata);
    updateDataMap(idata);

    console.log(dataMaps);

});
socket.on('update', function (udata) {
    //need to corrspond the updated data with kept copy - by name

    console.log("get update event");
    console.log(udata);
    //Blinking any modified object
    let uri1 = udata.uri.replace("C:\\TOMCAT\\webapps\\ROOT\\", "http://www.theworldavatar.com/");
	 uri1 = uri1.replace(/\\/g, "/");
	
	let uri2 = udata.uri.replace("C:\\TOMCAT\\webapps\\ROOT\\", "http://www.jparksimulator.com/");
	uri2 = uri2.replace(/\\/g, "/");

                    [uri1, uri2].forEach((uri)=>{
                        console.log(uri);
                        //console.log(b3map.getMarker(uri));
                        let mmarker = b3map.getMarker(uri);

                        if(mmarker){

                            mmarker.blinkAnimation()
                        }

                    })

    let modifs = updateDataMap(udata.data);
    console.log(modifs)   
   if(Object.keys(modifs).length > 0){
       Object.keys(modifs).forEach(url=>{
		   console.log(modifs[url])
           SendSimulationQuery(url, Array.from(modifs[url].values()));
       });
   }
//TODO: check update event
    //on update event, ajax to simulation service
    //when ajax result backs, ajax to endpoint for update
});


/***
 * Updated saved copy of data with new data
 * @param newData newData
 * @returns {boolean} if the data has any change
 */
function updateDataMap(newData) {
    let modifiedFlag = {};
    for(let dataP of newData){

        for(let sim of dataMaps){
            let dataMap = sim.dataMap;
            if(dataP&& dataP.name  && dataMap.has(dataP.name) && dataMap.get(dataP.name)!==dataP.value){
                dataMap.set(dataP.name, dataP.value);
                modifiedFlag[sim.url] = dataMap;
            }
        }

    }
    return modifiedFlag
}

//"http://www.theworldavatar.com/Service_Node_BiodieselPlant3/DoSimulation"
function SendSimulationQuery(murl, variables) {


    var queryString = "?Input=";
    for (var i = 0; i < variables.length; i++) {
        if (i == 0) {
            queryString = queryString + variables[i];

        } else {
            queryString = queryString + "+" + variables[i];
        }
    }

	console.log(queryString);
        $.ajax({
            url:   murl + queryString,

            success: function(response) {

                console.log("response from simulation:")
                console.log(response);
                /**
                 *SAMPLE:
                 ValueOfHeatDutyOfR-301$1.1612685888155425#
                 V_Angle_LoadPoint_R-301$-0.21495073093196138#
                 V_ActualVoltage_LoadPoint_R-301$3.3821878615224006#
                 ValueOfHeatDutyOfR-302$0.43186899431558023#
                 V_Angle_LoadPoint_R-302$-0.48014259831225964#
                 V_ActualVoltage_LoadPoint_R-302$2.966610277813966#
                 */
                //TODO: process this result and update

                let processed =  processResult(response);
                let divided = dispatchArray(processed);
                console.log(divided)
                async.each(divided, outputUpdate, function (err) {
                                       displayMessageModal("Update to server failed.");

                } )


            },
            error: function(xhr) {
                //Do Something to handle error
            }
        });
}
//TODO: A single case, if extended, this should be more general

function dispatchArray(arrs){

let result = [];

  arrs.forEach((array)=>{
var i,j,c,temparray,chunk = 4;
for (i=0,j=array.length,c = 0; i<j; i+=chunk,c++) {
    temparray = array.slice(i,i+chunk);
    result[c] = result.length>c?result[c]:[];
    result[c].push(temparray);
    // do whatever
}

  })

return result;
}

//Output attr map
var outputMap = {
    "ValueOfHeatDutyOfR-301":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/R-301.owl", name:"ValueOfHeatDutyOfR-301"}
    ,"V_Angle_LoadPoint_R-301":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/R-301load.owl", name:"V_theta_R-301load"}
    ,"V_ActualVoltage_LoadPoint_R-301":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/R-301load.owl", name:"V_ActualV_R-301load"}
        ,"ValueOfHeatDutyOfR-302":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant3/R-302.owl", name:"ValueOfHeatDutyOfR-302"}
    ,"V_Angle_LoadPoint_R-302":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/R-302load.owl", name:"V_theta_R-302load"}
    ,"V_ActualVoltage_LoadPoint_R-302":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/R-302load.owl", name:"V_ActualV_R-302load"}
    ,"V_molarF_601039":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/T-601002.owl", name:"V_molarF_601039"}
    ,"ValueOfHeatDutyOfE-601001":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601001.owl", name:"ValueOfHeatDutyOfE-601001"}
    ,"ValueOfHeatDutyOfE-601002":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601002.owl", name:"ValueOfHeatDutyOfE-601002"}
    ,"ValueOfHeatDutyOfE-601003":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601003.owl", name:"ValueOfHeatDutyOfE-601003"}
    ,"ValueOfHeatDutyOfE-601004":{uri : "http://www.jparksimulator.com/kb/sgp/jurongisland/biodieselplant2/E-601004.owl", name:"ValueOfHeatDutyOfE-601004"}
};


/***
 * Process result str of simulation into update query strs
 * @param resultStr
 * @returns {[*,*]}
 */
function processResult(resultStr) {
    let lines = resultStr.split('#');
                let attrObjs = [] 

    lines.forEach((line)=>{
        let nvpair =line.split('$');
        if (nvpair.length < 2){
            return;
        }

        let name = nvpair[0].trim(), value = nvpair[1];
        if(name in outputMap){
            outputMap[name]["value"] = value;
            outputMap[name]["p"] = "http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue";
            outputMap[name]["datatype"] = "float";
         attrObjs.push(outputMap[name])
        }
    })

    //Object.values(outputMap).filter((attr)=>{return attr.value!==null||attr.value!==undefined});
    console.log("updates: ");
    console.log(attrObjs);
    let uris =[];
    attrObjs.forEach((item)=>{uris.push(item.uri, item.uri)});
    console.log(uris);
    return [uris,  constructUpdate(null, attrObjs)];
}


/***
 * construct update query strings for all modification
 * @param uri
 * @param modifications
 */

var constructUpdate = function (uri, modifications) {
    let spoStrs = [];
    modifications.forEach((item) => {
        let muri = uri||item.uri;
        spoStrs = spoStrs.concat(constructSingleUpdate(muri, item));
    });
    console.log(spoStrs);
    return spoStrs;
};

/**
 * construct a single update query
 * @param uri
 * @param attrObj
 * @returns {[*,*]}
 */
var constructSingleUpdate = function (uri, attrObj) {
    var spoDel = "DELETE WHERE {<" + uri + "#" + attrObj.name + "> <" + attrObj.p + "> " + "?o.}";
//TODO: add " for string

    var spoIns = "INSERT DATA {<" + uri + "#" + attrObj.name + "> <" + attrObj.p + "> " + "\""+attrObj.value + "\"^^<http://www.w3.org/2001/XMLSchema#"+attrObj.datatype+">.}";
    return [spoDel, spoIns];
};



//TODO: merge it with the one in PopUpmap, or think of how to seperately it from Popup map
/***
 * Send output update queries
 * @param uris        array of uris to change, corresponding to updateQs
 * @param updateQs    array of SPARQL update strings
 * @param successCB   callback when success
 * @param errorCB      callback when err
 */
function  outputUpdate(input,cb) {

    let uris = input[0]
    let updateQs = input[1]
        console.log(uris)
    console.log(updateQs)
    //TODO: construct uris && updateQs
    var myUrl = 'http://www.theworldavatar.com/Service_Node_BiodieselPlant3/SPARQLEndPoint?uri=' + encodeURIComponent(JSON.stringify(uris)) + '&update=' + encodeURIComponent(JSON.stringify(updateQs)) + '&mode=update';

    $.ajax({
        url: myUrl,
        method: "GET",
        contentType: "application/json; charset=utf-8",
        success: function (data) {//SUCESS updating
            //Update display
            console.log(data);
            cb(null, data);

        },
        error: function (err) {
            displayMsg(errMsgBox, "Can not update to server", "danger")
            console.log("can not update to server")
        cb(err);
        }
    });
}

function displayMessageModal(msg) {
    $('#err-msg-modal-body').empty();
    $('#err-msg-modal-body').append("<p>"+msg+"</p>");

    $('#err-msg-modal').modal('show');

}
