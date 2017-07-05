// http://blog.thomsonreuters.com/index.php/mobile-patent-suits-graphic-of-the-day/

var socket = io();

/*constructor: d3 link graph***********************************************/
var FileLinkMap = function (options) {
    var width = $(document).width(),
        height = options.height || 1200,
        charge = options.charge || -3000,
        distance = options.distance || 100,
        nodeR = options.nodeR || 15,
        textSize = options.textSize || 5;

    var colorList = ["#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059",
        "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
        "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
        "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
        "#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F",
        "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
        "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
        "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",
        "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
        "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
        "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
        "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329"];
    var colorList2 = [


        "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C", "#000000"];
    var colorMap = {};
    var mapSize = 0;

    var bubbleMap = {};
    bubbleMap.nodesArr =[];

    function packNodesArr(links, coords) {
        var nodes = {};
        var nodesArr = [];


        function getDomain(str) {
            if (!str) {
                return null;
            }

            str = str.replace("theworldavatar", "jparksimulator");
            str = str.replace("file:/C:/", "http://www.jparksimulator.com/");
            var arr = str.split("/");


            console.log(arr);
            let domain = "";
            if (arr.length < 3) {
                domain += arr[2];
            }
            for (let i = 2; i < arr.length - 1; i++) {
                domain += "/" + arr[i];
            }
            return domain;
        }

        function getSimpleName(url) {
            if (!url) {
                return undefined;
            }
            var arr = url.split("/");

            //  return (arr[arr.length - 1] === "")? arr[arr.length-3]+'/'+arr[arr.length-2] : arr[arr.length-2]+"/"+arr[arr.length -1];
            return (arr[arr.length - 1] === "") ? arr[arr.length - 2] : arr[arr.length - 1];

            return url;
        }

        /*search for coordinates in coordinate array by url*/
        function getCoord(url) {
            for(let i = 0; i < coords.length; i++){
                let coord = coords[i];
              //   console.log("url in packed coords+ " +coord.url);
                //console.log("coord in packed coords+ " +JSON.stringify(coord.coord));

                if(coord.url == url){
                   return coord.coord;
                }
            }

            return null;
        }
        // Compute the distinct nodes from the links.
        links.forEach(function (link) {

            link.source = nodes[link.source] || (nodes[link.source] = {
                    url: link.source,

                    name: getSimpleName(link.source),
                    domain: getDomain(link.source),
                    count: 0
                });

            if (nodes[link.target]) {
                nodes[link.target].level = parseInt(link.level) + 1;
            }
            link.target = nodes[link.target] || (nodes[link.target] = {
                    url: link.target,
                    name: getSimpleName(link.target),
                    domain: getDomain(link.target),
                    count: 0
                    , level: parseInt(link.level) + 1

                });

            //get count of links on each node
            /**
             if (nodes[link.source] != null) {
                nodes[link.source].count = nodes[link.source].count + 1;
                console.log("++");
            }
             if (nodes[link.target] != null) {
                nodes[link.target].count = nodes[link.target].count + 1;
                console.log("++");

            }
             ***/
        });

        let index = 0;
        console.log("@@@@@@@@@@@@@@@@@@@@@@");
        for (let link of links) {

            console.log(index + "  :" + JSON.stringify(link));
            index++;
        }
        //packs object :nodes into array
        for (var URI in nodes) {
            if (nodes.hasOwnProperty(URI)) {

                if (nodes[URI].name.toLowerCase().indexOf("world") !== -1)//manually add level to world
                {
                    nodes[URI].level = 0;
                }
                //add to node attri: geo coordinates
                nodes[URI].coord = getCoord(URI);
                nodesArr.push(nodes[URI]);
                console.log("packed node: " + JSON.stringify(nodes[URI]))
                //console.log("count" + nodes[attr].count);
            }
        }
        return nodesArr;
    }

    function setBodyS(node) {
        return -300 * (1 || node.count);

        //return charge;
    }

    function setD() {
        return distance;
    }

    /**
     * Allocate color for nodes of each domain
     * @param d  datum
     * @returns one color defined in colormap
     */
    function allocateColor(d) {
        // is this exist in color map?

        if (d.level !== undefined && d.level !== null && !isNaN(d.level)) {

            return colorList2[d.level];
        }

        colorMap[d.domain] = colorMap[d.domain] || (colorList[mapSize++]);

        if (mapSize >= mapSize.length) {
            mapSize = 0;
            console.log("WARNING: DOMAIN NUMBER EXISTS COLOR MAP NUMBER!");
        }

        return colorMap[d.domain];
    }

    function sortOrder(d, i) {

        if (d.level !== undefined && d.level !== null && !isNaN(d.level)) {

            return d.level * 1000 + i;
        }

        return 100000 + i;
    }

    function defineLegend(d) {
        if (d.level !== undefined && d.level !== null && !isNaN(d.level)) {

            return d.level;
        }

        return d.domain;
    }

    var svg = d3.select("#draw-panel").append("svg")
        .attr("width", width)
        .attr("height", height);
    var g = svg.append("g");

    function clear() {
        g.selectAll("line").data([]).exit().remove();
        g.selectAll("circle").data([]).exit().remove();
        g.selectAll("text").data([]).exit().remove();
    }

    bubbleMap.update = function(links, coords) {
         bubbleMap.nodesArr = packNodesArr(links, coords);

        //set force simulation
        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink(links).distance(70))
            .force("charge", d3.forceManyBody().strength(setBodyS))
            .force("center", d3.forceCenter(width / 2, height / 2));

        var path = g.selectAll("line") //linking lines
            .data(links, function (d) {
                return d.source.domain + d.source.name + d.target.domain + d.target.name;
            });

        path.exit().remove();


        path.enter().append("line")
            .attr("class", function (d) {
                return "link " + "licensing";
            })
            .attr("marker-end", function (d) {
                return "url(#" + "licensing" + ")";
            })
        ;
        // path.exit().remove();
        path = g.selectAll("line");

        var circle = g.selectAll("a.cir") //node bubbles
            .data(bubbleMap.nodesArr, function (d) {
                return d.domain + d.name;
            });

        circle.exit().remove();

        circle.enter().append("a")
            .attr('class', 'cir')
            //.append("a")
            .attr("xlink:href", function (d) {
                return d.url;
            })

            .append("circle")
            .attr("id", function (d) {
                return d.name;
            })
            .attr("r", nodeR)
            .attr("class", "nodes")
            .attr("fill", allocateColor)
            .attr("data-legend-pos", sortOrder)
            .attr("data-legend", defineLegend)
            .on("mouseover", handleMouseOver)         //highlight all links when mouse over
            .on("mouseout", handleMouseOut)
            .call(d3.drag()                         //enable user to drag
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended)
            );


        circle = g.selectAll("a.cir");
        var circleDraw = g.selectAll("a.cir").select("circle.nodes");

        var text = g.selectAll("text.nodeTag")  //node tags
            .data(bubbleMap.nodesArr, function (d) {
                return d.domain + d.name;
            });
        text.exit().remove();


        text.enter().append("text")
            .attr("class", "nodeTag")
            .attr("x", textSize)
            .attr("y", "0.31em")
            .text(function (d) {
                return d.name;
            });
        text = g.selectAll("text.nodeTag");

        simulation
            .nodes(bubbleMap.nodesArr)
            .on("tick", ticked);

        simulation.force("link")
            .links(links);


        var preLengend = svg.selectAll("g.legend").remove();

        var legend = svg.append("g")
            .attr("class", "legend")
            .attr("transform", "translate(50,30)")
            .style("font-size", "12px")
            .call(d3.legend);


        function ticked() {

            path
                .attr("x1", function (d, i) {
                    if (d === undefined) {
                        //	console.log("@@@@@@@@@@@@@"+i);
                        return 0;
                    }
                    return d.source.x;
                })
                .attr("y1", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.source.y;
                })
                .attr("x2", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.target.x;
                })
                .attr("y2", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.target.y;
                });

            circle
                .attr("x", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.x;
                })
                .attr("y", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.y;
                });

            circleDraw
                .attr("cx", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.x;
                })
                .attr("cy", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.y;
                });
            text
                .attr("x", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.x;
                })
                .attr("y", function (d) {
                    if (d === undefined) {
                        return 0;
                    }
                    return d.y;
                });

        }


        function dragstarted(d) {
            if (!d3.event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }

        function handleMouseOver(dCircle) {
            //node name : d.name
            svg.selectAll("line")
                .style("stroke", highlightPath);

            function highlightPath(dLink) {
                // console.log("source: "+JSON.stringify(dLink.source)+"  target: "+ JSON.stringify(dLink.target) +" node name: "+dCircle.name);
                if ((dLink.source && dLink.source.name === dCircle.name) || ( dLink.target && dLink.target.name === dCircle.name)) {
                    //     console.log("change color");


                    return "#ffff00";
                }
                return "#008000";
            }
        }

        function handleMouseOut(dCircle) {
            //node name : d.name
            // console.log("change back");

            svg.selectAll("line").style("stroke", "#008000");

        }
        bubbleMap.defaultOpa();
    }

    //TODO: add coordinates to nodes data
    bubbleMap.updateByCoord = function updateByCoord(center, radius) {
        svg.selectAll("a.cir").select('circle.nodes').attr("opacity",function(d) {
                if(d.coord) {
                    console.log("@@@@@@@@@@@in d3 node dta: " + JSON.stringify(d.coord))
                    let dx = d.coord.x - center.x;
                    let dy = d.coord.y - center.y;
                    let eps = Number.EPSILON;
                      console.log(radius * radius - dx * dx - dy * dy);
                    return (radius * radius - dx * dx - dy * dy > eps) ? 1 : 0.25;
                } else {//This node does not have coordinates
                   // console.log(" node: "+d.url+" does not have coordinates");
                    return 0.25;
                }
            });


        //TODO: deal with path

        // deal with label
        svg.selectAll("text.nodeTag").attr("visibility",function(d) {
            if(d.coord) {
                console.log("@@@@@@@@@@@in d3 node dta: " + JSON.stringify(d.coord))
                let dx = d.coord.x - center.x;
                let dy = d.coord.y - center.y;
                let eps = Number.EPSILON;
                console.log(radius * radius - dx * dx - dy * dy);
                return (radius * radius - dx * dx - dy * dy > eps) ? "visible" : "hidden";
            } else {//This node does not have coordinates
                // console.log(" node: "+d.url+" does not have coordinates");
                return "hidden";
            }
        });


    }

    bubbleMap.defaultOpa = function () {
        svg.selectAll("a.cir").select('circle.nodes').attr("opacity",function(d) {
        return 1;
        })
    };

    var data = JSON.parse($("#data").val());//extract link data from web page
    var links = data.connections;
    var coords = data.geoCoords;
    console.log("Extract from initial data: coords: "+ coords);
    bubbleMap.update(links, coords);
    return bubbleMap;

};
/*END constructor: d3 link graph*********************************************/

/*DOM LOGIC**************************************************************/
$(window).load(function () {// when web dom ready
    let url = window.location.href;     // Returns full URL

    /*init d3 map*/
    var map = FileLinkMap({});

    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    /*choice panel*************************************************/
        /*button : show Import **/
    $("#checkShowImport").change(function () {
        if ($('#checkShowImport').prop('checked')) {
            disableThenEnableAfterTimeout();

            //TODO:ajax, only change data, but with d3...how? should use angular instead?
            $.ajax({
                url: '/visualize/includeImport',
                type: 'GET',

                statusCode: {
                    200: function (data) {
                        let links = data.connections;
                        let coords = data.geoCoords;
                        console.log('ajax successful!\n');


                        console.log(JSON.stringify(links));
                        if (LinkRightFormat(links)) {

                            for (let link of links) {
                                if (link === undefined || link === null) {
                                    console.log("!!!!!!!!!!");

                                }

                            }
                            map.update(links, coords);


                        }

                    }
                },
                error: function (err) {
                    console.log(err);


                }
            });

        } else {
            window.location.href = '/visualize';
        }


    });
        /*button : show Service Only **/
    $("#checkShowServiceOnly").change(function () {
        if ($('#checkShowServiceOnly').prop('checked')) {
            disableThenEnableAfterTimeout();
            //TODO:ajax, only change data, but with d3...how? should use angular instead?
            $.ajax({
                url: '/visualize/showServiceOnly',
                type: 'GET',

                statusCode: {
                    200: function (data) {

                        let links = data.connections;
                        let coords = data.geoCoords;

                        console.log('ajax successful!\n');


                        //     console.log(JSON.stringify(links));
                        if (LinkRightFormat(links)) {
                            clearSelectBar();
                            map.update(links, coords);


                        }

                    }
                },
                error: function (err) {
                    console.log(err);


                }
            });

        } else {
            window.location.href = '/visualize';
        }
    })
        /*button : show Default **/
    $("#checkdefault").change(function () {
        if ($('#checkdefault').prop('checked')) {

            window.location.href = '/visualize'; //return to default

        }
    });
        /*check box back to default when unload*/
    $(window).unload(function () {
        console.log("unload");

        if ($("#checkShowImport").prop('checked')) {
            $("#checkShowImport").prop('checked', false);
        }
        if ($("#checkShowServiceOnly").prop('checked')) {
            $("#checkShowServiceOnly").prop('checked', false);
        }
    });
        /*utility functions*********/
    /**
     * utility function, check if link is of correct format
     * @param links
     * @returns {boolean}
     * @constructor
     */
    function LinkRightFormat(links) {
        if (!links || links.length < 1) {
            return false;
        }
        for (let link of links) {
            if (!link.hasOwnProperty("target") || !link.hasOwnProperty("source")) {
                return false;
            }

        }
        return true;
    }
    /**
     * Utility function: disable option buttons
     * @param disable
     * @returns {boolean}
     * @constructor
     */
    function DisableOptionButton(disable) {
        //  console.log(disable);

        if ($("#checkShowImport").prop("disabled") === !disable && $("#checkShowServiceOnly").prop("disabled") === !disable) {
            $("#checkShowImport").prop("disabled", disable);
            $("#checkShowServiceOnly").prop("disabled", disable);
            console.log("now :" + (disable === true ? "disable" : "enable"));
            return true;
        }
        return false;
    }
    /**
     * utility function : disable all buttons then enable all after timeout
     */
    function disableThenEnableAfterTimeout() {
        if (DisableOptionButton(true)) {

            setTimeout(function () {
                DisableOptionButton(false);
            }, 5000);
        }

    }
    /*END  choice panel***************************************************/

    /*GEO Search Panel***************************************************************/

    function updateSelectBar() {
        //initiate select with data
        clearSelectBar();
        map.nodesArr.forEach(function (item) {    // for each in data list
            if(item.coord) {        //only do this if has a coord'
                $("#device-select").append("<option value='" + item.coord.x + "/" + item.coord.y + "'>" + item.name + "</option>");
            }
        });
    }

    function clearSelectBar() {
        $("#device-select").html("");
    }

    updateSelectBar();


   //TODO: check cross senario:
   //TODO: disable geo-search panel when show agents only is checked?

    //when choose from select, update coord to x,y input
    $('#device-select').on('change', function() {
       // alert( this.value );
        let coordStr = $("select#device-select option:selected").val();
        let coordArr = coordStr.split("/");
        $("#search-center-x").val(coordArr[0]);
        $("#search-center-y").val(coordArr[1]);
    });


    //when user clicked submit
    $("#search-submit").click(function () {
        //retreive search center and radius
        let x = parseFloat($("#search-center-x").val());
        let y = parseFloat($("#search-center-y").val());

        // check validity, display err msg
        let radius = parseFloat($("#search-radius").val());
        if(x===null || y===null || radius===null || isNaN(x) || isNaN(y) || isNaN(radius) ){
            displayMsg("Input parameters are not number", "danger");
            return;
        }
        //Todo: or not to do. x,y,radius must be numbers .range checking also? min radius : 0.0000001
        /**
        //truncate radius to 7 digits, show warning
        if(decimalPlaces(x) > 7){
            x= x.toFixed(7);
            $("#search-center-x").val(x);
            displayMsg("Numbers beyond 7 decimal places will be truncated");
        }
        if(decimalPlaces(y) > 7){
            y= x.toFixed(7);
            $("#search-center-y").val(y);
            displayMsg("Numbers beyond 7 decimal places will be truncated");
        }
        if(decimalPlaces(y) > 7){
            radius = radius.toFixed(7);
            $("#search-center-y").val(radius);
            displayMsg("Numbers beyond 7 decimal places will be truncated");
        }
        **/

        console.log("CX:" + x +" Y:" + y+ "  R:" + radius);
        //pack x, y into object
        map.updateByCoord({x, y}, radius);
        displayMsg("Search for device near center point :("+x+" ,"+y+") with radius : "+radius, "success");

        function decimalPlaces(num) {
            var match = (''+num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
            if (!match) { return 0; }
            return Math.max(
                0,
                // Number of digits right of decimal point.
                (match[1] ? match[1].length : 0)
                // Adjust for scientific notation.
                - (match[2] ? +match[2] : 0));
        }

    });

    /*END GEO Search Panel***************************************************************/

    /*Err Msg Bar************************************************************************/
    var template = function (msg, type){

        return "<p class='alert alert-"+type+"'>"+msg+"</p>";
    };

    /**
     *
     * @param msg
     * @param type  [success/info/warning/danger]
     */
    function displayMsg(msg,type) {

        $("#err-msg-panel").html("");
        $("#err-msg-panel").append(template(msg, type));

    }


    /*End Err Msg Bar*******************************************************************/

    /*socket**************************************************************/
    //blink any updated data
    let blinkTimer;
    let preTime = Date.now();
    socket.on('update', function (data) {
        //check if time less than 1s, if true, do not do anything
        if (Date.now() - preTime > 1000) {
            preTime = Date.now();
            console.log("!!!!!!!!!dataUpdate");
            let parsedData = JSON.parse(data);
            let name = parsedData.name;
            console.log(name);
            //search name among nodes to grab the correct one
            // console.log($('circle'));
            // console.log($('#FH-01.owl'));
            // console.log($("circle[id='FH-01.owl']"));

            let node = $("circle[id='FH-01.owl']");

            let oriColor = node.css("fill");

            let orSize = node.attr("r");
            //console.log($('circle#FH-01.owl'));
            console.log(orSize);

            node.animate({'r': 20}, 500)
                .css({'fill': '#FFFC94', 'transition': 'fill 0.5s'})
                .css({'stroke': '#FFF700', 'transition': 'stroke 0.5s'});
            ;
            blinkTimer = setTimeout(function () {
                node.animate({'r': orSize}, 500)
                    .css({'stroke': '#000000', 'transition': 'stroke 0.5s'})
                    .css({'fill': oriColor, 'transition': 'fill 0.5s'});

            }, 500);//on 0.5s, back to normal

        }
    });
    /*END socket **********************************************************/

});
/*END DOM LOGIC**************************************************************/



//module.exports = FileLinkMap;