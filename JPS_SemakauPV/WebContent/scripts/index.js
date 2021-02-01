$(document).ready(function(){
	
	//-----------------------------------------------------------------------------------//
	
	const toggleDisplay = elemId => {
        let x = document.getElementById(elemId);
        if (x.style.display !== 'block') {
            x.style.display = 'block';
        } else {
            x.style.display = 'none';
        }
    };

    $("#readme-button").click(function() {
        toggleDisplay("readme-text");
    });

    document.addEventListener("click", function(evt) {
        var readmeButtonElement = document.getElementById('readme-button'),
            readmeTextElement = document.getElementById('readme-text'),
            targetElement = evt.target;  // clicked element

        if (targetElement == readmeButtonElement || targetElement == readmeTextElement) {
            return; //readme-button or readme-text is clicked. do nothing.
        }

        if(readmeTextElement.style.display === 'block') {
            readmeTextElement.style.display = 'none';
        }
    });
    
    //-----------------------------------------------------------------------------------//

	$.getJSON('/JPS_SemakauPV/SemakauVisualization',
		{
			pvgenerator:"http://www.theworldavatar.com/kb/sgp/semakauisland/semakauelectricalnetwork/PV-002.owl#PV-002",
			ebus:"http://www.theworldavatar.com/kb/sgp/semakauisland/semakauelectricalnetwork/EBus-006.owl#EBus-006",
			irradiationsensor:"http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationSensor-001.owl#SGSolarIrradiationSensor-001"
		},
		function(data){
			console.log(data);
			let propvallst = data.propVal;
			let propTime = data.proptime;
			makeChart(propvallst, propTime , 'graph1', "W/m^2", 'rgba(211, 181, 146, 1)');
			
			let VoltAnglevaluelst= data.VoltAng;
			let VoltMagvaluelst = data.VoltMag;
			makeChart(VoltAnglevaluelst, propTime , 'graph3', 'degree');
       		makeChart(VoltMagvaluelst, propTime , 'graph4', 'V');
			let reactivepowervaluelst= data.reactivePower;
			let activepowervaluelst = data.activePower;
			makeChart(activepowervaluelst, propTime , 'graph7', 'MW');
        	makeChart(reactivepowervaluelst, propTime , 'graph8', 'Mvar');
		}).fail( function(){
			alert("Connection to server failed");
			let propvallst = ["638.0","392.2","524.5","524.5","377.3","810.9","1034.6","917.7","826.4","500.5","183.2","33.8","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","392.1","277.2","279.3","145.5","31.3","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","0.0","79.8","247.5","373.3"];
			let propTime = ["07:59:08","08:59:05","09:59:04","10:59:04","11:59:04","12:59:04","13:59:04","14:59:04","15:59:04","16:59:04","17:59:04","18:59:04","19:59:04","20:59:04","21:59:04","22:59:04","23:59:04","02:41:22","02:59:08","03:59:04","04:59:04","05:59:04","06:59:04","07:59:08","08:59:04","09:59:04","10:59:04","11:59:04","12:59:04","13:59:04","14:59:04","15:59:04","16:59:04","17:59:04","18:59:04","19:59:04","20:59:05","21:59:04","22:59:04","23:59:04","02:42:52","02:59:06","03:59:05","04:59:04","05:59:05","06:59:04","07:59:08","08:59:04","09:59:04"];
			makeChart(propvallst, propTime , 'graph1', "W/m^2", 'rgba(211, 181, 146, 1)');
			let VoltAnglevaluelst= ["-0.06164762539492122","-0.04367676905703308","-0.028896874580032948","0.06764641084290104","-0.017895399377640807","-0.06614701513182188","-0.09164800740921145","-0.08859055859689616","-0.0941046475029495","-0.09487902318467127","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09015266126519697","-0.07083332313672437","-0.05365019378541527","0.009100939043763221","-0.04526557433504844","-0.03984494456299602","0.04854069359214479","-0.0481494561842192","-0.08481213932228816","-0.09327686660179862","-0.09383762140580405","-0.09183492567721324","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.09824355200870383","-0.08753580551317167","-0.06563966554724554","-0.06941808482185352","-0.045439141298192987","0.009701747762340462"];
			let VoltMagvaluelst = ["1.0000009925560616","1.0000015692165045","1.0000020434833026","1.0000051414265878","1.000002396505743","1.0000008481767384","1.000000029884431","1.0000001279938229","0.9999999510541772","0.9999999262055104","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","1.0000000778680638","1.0000006977994609","1.0000012491828116","1.000003262781683","1.0000015182338948","1.0000016921745634","1.0000045283499952","1.0000014256940317","1.0000002492381803","0.9999999776165454","0.9999999596226831","1.000000023886477","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","0.9999998182423369","1.0000001618394208","1.0000008644568994","1.000000743212542","1.000001512664366","1.0000032820608211"];
			
			makeChart(VoltAnglevaluelst, propTime , 'graph3', 'degree');
       		makeChart(VoltMagvaluelst, propTime , 'graph4', 'V');
			let reactivepowervaluelst=["0.04826546635821238","0.1009146836120638","0.07257663868657746","0.07257663868657746","0.10410619434063328","0.011231090185751455","-0.036684409812973336","-0.011644973291511922","0.007911062246635507","0.07771732710843438","0.1456815119524012","0.1776822973784606","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.10093610314715484","0.12554714896679492","0.12509733872988243","0.15375667668173482","0.17821778575573735","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.18492210023924244","0.16782931123656816","0.13190875088884285","0.1049629757442761"];
			let activepowervaluelst = ["0.1468563006648167","0.0902777755957394","0.12073074087905479","0.12073074087905479","0.08684807655778479","0.18665462037369973","0.23814614217171676","0.21123796649809948","0.1902224280977465","0.11520639343536947","0.04216991660697968","0.007780853770038482","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","0.09025475748139072","0.0638069440947472","0.06429032449606968","0.03349208749752394","0.007205400911321269","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","7.31120181618472E-7","0.018369186370435386","0.0569705641331866","0.08592735198383723"];
			makeChart(activepowervaluelst, propTime , 'graph7', 'MW');
        	makeChart(reactivepowervaluelst, propTime , 'graph8', 'Mvar');

		})
	function makeChart(dataset, time, id, unit){
		makeChart(dataset, time, id, unit,"rgba(39, 211, 122, 1)");
	}
	function makeChart(dataset, time, id, unit, color){
		var v = dataset;
		console.log(time);
		v.forEach(function(obj){obj = parseFloat(obj)});
		vGraph = new Chart(id, {
			type: 'line', 
			data: {
				labels: time, 
				datasets:[
					{	
						label:'',
						pointRadius: 2, 
						pointHoverRadius:5, 
						backgroundColor:color,
			            data: v, 
			            fill: false,
					}
					]
				}, 
			options:{
				title:{ 
			        text: unit,
			        display: true
				}
			}, 
			responsive:true,
			maintainAspectRatio:true
	
		})
	}

});