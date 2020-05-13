package uk.ac.cam.cares.jps.dispersion.interpolation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.TimeZone;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.annotate.MetaDataAnnotator;
import uk.ac.cam.cares.jps.base.annotate.MetaDataQuery;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.config.IKeys;
import uk.ac.cam.cares.jps.base.config.KeyValueManager;
import uk.ac.cam.cares.jps.base.config.KeyValueMap;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.KnowledgeBaseClient;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.MatrixConverter;
@WebServlet({"/InterpolationAgent/startSimulation", "/InterpolationAgent/continueSimulation"})
public class InterpolationAgent  extends JPSHttpServlet {
	public String SIM_START_PATH = "/InterpolationAgent/startSimulation";
	public String SIM_PROCESS_PATH = "/InterpolationAgent/endSimulation";
	
	private static final long serialVersionUID = 1L;
	public static final String KEY_WATCH = "watch";
	public static final String KEY_CALLBACK_URL = "callback";
	public static final String dataseturl=KeyValueManager.get(IKeys.DATASET_AIRQUALITY_URL);
    protected void setLogger() {
        logger = LoggerFactory.getLogger(InterpolationAgent.class);
    }
    /**
     *  create logger to log changes attached to WasteToEnergyAgent class. 
     */
    protected Logger logger = LoggerFactory.getLogger(InterpolationAgent.class);
		
	public InterpolationAgent() {
		// TODO Auto-generated constructor stub
	}
	
	/** main function. Reads the values in and copies the templates back. 
	 * 
	 */
	@Override
	protected JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		String path = request.getServletPath();
		//temporarily until we get an idea of how to read input from Front End
		String baseUrl= QueryBroker.getLocalDataPath()+"/JPS_DIS";
		//String coordinates = requestParams.optString("coordinates","[364628.312 131794.703 0]");
		//replace the above method with below (commented out. 
		String stationiri = requestParams.optString("airStationIRI", "http://www.theworldavatar.com/kb/sgp/singapore/AirQualityStation-001.owl#AirQualityStation-001");
		String coordinates = readCoordinate(stationiri);
		String gasType, dispMatrix ="";
		String agentiri = requestParams.optString("agent","http://www.theworldavatar.com/kb/agents/Service__ADMS.owl#Service");
		String location = requestParams.optString("city","http://dbpedia.org/resource/Singapore");
		String[] directorydata = getLastModifiedDirectory(agentiri, location);
		String directoryFolder=directorydata[0];
		String directorytime=ConvertTime(directorydata[1]);
		System.out.println("dirtime= "+directorytime);
		
		String options = requestParams.optString("options","1");//How do we select the options? 
		String[] arrayFile = finder(directoryFolder);
		String fGas = arrayFile[0];
		File lstName = new File(directoryFolder, fGas);//fGas is the name of the test.levels.gst
		if (lstName.getName().endsWith(".dat")) {
			ArrayList<String> gsType = determineGas(directoryFolder, lstName);
			gasType = gsType.get(0);
			String fileName = gsType.get(1);		
			dispMatrix = copyOverFile(baseUrl,fileName);//to be swapped with copyOverFile
		}
		else {
			ArrayList<String> gsType = determineGasGst(directoryFolder, lstName);
			gasType = gsType.get(0);
			String fileName = gsType.get(1);
			dispMatrix = rearrangeGst(baseUrl, fileName, gasType);
			
		}
		copyTemplate(baseUrl, "virtual_sensor.m");
		//modify matlab to read 
		if (SIM_START_PATH.equals(path)) {
			try {
				createBat(baseUrl, coordinates,gasType, options, dispMatrix);
				runModel(baseUrl);
				logger.info("finish Simulation");
				notifyWatcher(requestParams, baseUrl+"/exp.csv",
	                    request.getRequestURL().toString().replace(SIM_START_PATH, SIM_PROCESS_PATH));
	           
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }else if (SIM_PROCESS_PATH.equals(path)) {
			 try {
				 List<String[]> read =  readResult(baseUrl,"exp.csv");
				 String arg = read.get(0)[0];
				 logger.info(arg);

				 //update here
				 double concpm10=0.0;
				 double concpm25=0.0;
				 double concpm1=0.0;
				 double concHC=0.0;
				for (int x = 0; x < read.size(); x++) {
					String component = read.get(x)[0];
					String classname = "Outside" + component + "Concentration";
					String value = read.get(x)[1];
					if (component.contains("PM2.5")) {
						concpm25 = concpm25 + Double.valueOf(value);
					} else if (component.contains("PM10")) {
						concpm10 = concpm10 + Double.valueOf(value);
					} else if (component.contains("PM1")) {
						concpm1 = concpm1 + Double.valueOf(value);
					} else if (component.contentEquals("O3") || component.contentEquals("NO2")
							|| component.contentEquals("NO") || component.contentEquals("NOx")
							|| component.contentEquals("SO2") || component.contentEquals("CO2")
							|| component.contentEquals("CO")) {
						updateRepoNewMethod(stationiri, classname, value, value, directorytime);
					} else if (component.contentEquals("C2H6") || component.contentEquals("C2H4")
							|| component.contentEquals("nC4H10") || component.contentEquals("HC")
							|| component.contentEquals("nC3H6") || component.contentEquals("oXylene")
							|| component.contentEquals("isoprene")) {
						concHC = concHC + Double.valueOf(value);
					}
				}
				 updateRepoNewMethod(stationiri, "OutsideHCConcentration",""+concHC,""+concHC,directorytime);
				 updateRepoNewMethod(stationiri, "OutsidePM1Concentration",""+concpm1,""+concpm1,directorytime);
				 updateRepoNewMethod(stationiri, "OutsidePM25Concentration",""+(concpm1+concpm25),""+(concpm1+concpm25),directorytime);
				 updateRepoNewMethod(stationiri, "OutsidePM10Concentration",""+(concpm1+concpm25+concpm10),""+(concpm1+concpm25+concpm10),directorytime);
				 
			 }catch (Exception e) {
				e.printStackTrace();
			}			 
		 }
		return requestParams;
	}
	
	public static String ConvertTime(String current) {
		DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		   utcFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		   String result=current;
		try {
			  Date date = utcFormat.parse(current);
			   DateFormat pstFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			   pstFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

			   System.out.println(pstFormat.format(date));
			   result=pstFormat.format(date)+"+08:00";
			   return result;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}

	
	/** find an array of path names that follow dat or match
	 * 
	 * @param dirName
	 * @return
	 */
	public String[] finder(String dirName) {
        File dir = new File(dirName);
        return dir.list(new FilenameFilter(){
        		//checks if file ends with dat or gst file
                 public boolean accept(File dir, String filename){ 
                	 return (filename.endsWith(".dat")||filename.endsWith("levels.gst"));
                }
        });

    }
	/** determine the Gas from a Gst file rather than from a .dat file
	 * 
	 * @param dirName
	 * @return
	 */
	public ArrayList<String> determineGasGst(String dirName, File lstName){
		//unlike the method below which looks for header in .dat
		String fullString = "";
		try {
			BufferedReader bff = new BufferedReader(new FileReader(lstName));
			String text = bff.readLine(); //get first line of file
			String[] gasTypes = text.split(",Conc"); //Have to insert \\ because of parenthesis
			List<String> newLst = Arrays.asList(gasTypes);
			List<String> gasTypeArr = new ArrayList<String>();
			gasTypeArr.addAll(newLst); //because asList returns a fixed-size list
			gasTypeArr.remove(0);
			List<String> gasTypeArr2 = gasTypeArr.subList(0,(gasTypeArr.size()/4));
			for (int i = 0; i < gasTypeArr2.size(); i ++) {
				String j= gasTypeArr2.get(i).split("\\|")[2];
				gasTypeArr2.set(i, j);
			}
			String[] gasTypes2 = new String[gasTypeArr2.size()];
			gasTypes2 = gasTypeArr2.toArray(gasTypes2);	
			
			fullString = "['" +String.join(" ", gasTypes2) +"']";
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> lst_a = new ArrayList<String>();
		lst_a.add(fullString);
		lst_a.add(lstName.getAbsolutePath());
		return lst_a;
	}
	/** determine the types of gas available. 
	 * 
	 * @param dirName
	 * @return {ArrayList} [gases, name of relevantFile]
	 */
	public ArrayList<String> determineGas(String dirName, File lstName) {
		//Since there are two files with gst, then we'll just choose the first in the array of [1] or [2]
		String fullString = "";
		try {
			BufferedReader bff = new BufferedReader(new FileReader(lstName));
			String text = bff.readLine(); //get first line of file
			String[] gasTypes = text.split("Z\\(m\\)"); //Have to insert \\ because of parenthesis
			String gasType = gasTypes[gasTypes.length-1].trim();
			//get last element (the gases are separated by six spaces
			
			String[] gasTypes2 = gasType.split("\\s+");
			fullString = "['" +String.join(" ", gasTypes2) +"']";
		}catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> lst_a = new ArrayList<String>();
		lst_a.add(fullString);
		lst_a.add(lstName.getAbsolutePath());
		return lst_a;
	}
	/** notifies the watcher to return with the callback. 
	 * 
	 * @param agentArgs JSONObject
	 * @param filePath String
	 * @param callbackIRI String
	 */
    private void notifyWatcher(JSONObject agentArgs, String filePath, String callbackIRI) {
        agentArgs.put(KEY_WATCH, filePath);
        agentArgs.put(KEY_CALLBACK_URL, callbackIRI);
        execute(KeyValueMap.getInstance().get("url.jps_aws"), agentArgs.toString(), HttpPost.METHOD_NAME);
    }
	/** copies over the files in the working directory over to scenario folder. 
	 * 
	 * @param newdir
	 * @param filename
	 */
	public void copyTemplate(String newdir, String filename) { //in this case for main .m file
		File file = new File(AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/"+filename);
		
		String destinationUrl = newdir + "/"+filename;
		new QueryBroker().putLocal(destinationUrl, file);
	}
	/** copies over the files in the working directory over to scenario folder. 
	 * 
	 * @param newdir location of directory of scenario folder
	 * @param filename name of actual file without directory
	 */
	public String copyOverFile(String newdir, String filename) { //in this case for source folder
		Path path = Paths.get(filename);
		File file = new File(filename);
		Path fileName = path.getFileName();
		String destinationUrl = newdir + "/"+fileName.toString();
		new QueryBroker().putLocal(destinationUrl, file);
		return fileName.toString();
	}
	/** copies over the files in the working directory over to scenario folder. 
	 * current run of 4 minutes to make file
	 * @param newdir location of directory of scenario folder
	 * @param filename name of actual file without directory
	 */
	public String rearrangeGst(String newdir, String filename, String gasType) { //in this case for source folder
		Path path = Paths.get(filename);
		File file = new File(filename);
		String writePath = newdir + "/"+"test.dat";
		File myObj = new File(writePath);
		logger.info(writePath);
		Path fileName = path.getFileName();
		String destinationUrl = newdir + "/"+fileName.toString();
		new QueryBroker().putLocal(destinationUrl, file);
		gasType = gasType.substring(2, gasType.length()-2);
		int noOfGas = (gasType.split(" ")).length;
		int j = 0;
		try {
			BufferedReader bff = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(myObj));
			writer.write("Year  Month  Day  Hour  X(m)  Y(m)  Z(m)  ");
			writer.write(gasType +"\r\n");
			bff.readLine();
			String thisLine = null;
			 while ((thisLine = bff.readLine()) != null) {
				 	thisLine.trim();
				 	j++;
		            String[] lineArr = thisLine.split(",");
		            List<String> arr = Arrays.asList(lineArr);
		            List<String> introArr = arr.subList(0,6);
		            for (int i = 0; i< 4; i++) {
			            StringJoiner sb = new StringJoiner(" ");
			            List<String> arr1 = new ArrayList<String>();
			            arr1.addAll(introArr);
			            arr1.add(String.valueOf(i*10));
			            arr1.addAll(arr.subList(7+i*noOfGas,(i+1)*noOfGas+7));
			            for (String s : arr1){
			                sb.add(s);
			            }
			            writer.write(sb.toString().trim()+"\r\n");
		            }
		            
		         }
			 bff.close();
			 writer.close();
			 System.out.println(j);
			 return "test.dat";
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}catch (IOException ex) {
			ex.printStackTrace();
		}
		return "";
	}
	/** create the batch file in the mentioned folder. 
	 * 
	 * @param baseUrl
	 * @throws Exception
	 */
	public void createBat(String baseUrl, String coordinates, String gasType, String options, String dispMatrix) throws Exception {
		String loc = " virtual_sensor(" + coordinates +"," +gasType
				+"," +options+",'"+baseUrl+"/','" + dispMatrix+"'";
		String bat = "setlocal" + "\n" + "cd /d %~dp0" + "\n" 
		+ "matlab -nosplash -noFigureWindows -r \"try; cd('"+baseUrl
		+"');"+ loc + "); catch; end; quit\"";
		new QueryBroker().putLocal(baseUrl + "/runm.bat", bat);
	}
	/** runs the batch file. 
	 * 
	 * @param baseUrl
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void runModel(String baseUrl) throws IOException, InterruptedException {
		String startbatCommand =baseUrl+"/runm.bat";
		String result= executeSingleCommand(baseUrl,startbatCommand);
		logger.info("final after calling: "+result);
	}
	/** executes the process. Called by runModel. 
	 * 
	 * @param targetFolder
	 * @param command
	 * @return
	 * @throws InterruptedException
	 */
	private String executeSingleCommand(String targetFolder , String command) throws InterruptedException 
	{  
	 
		logger.info("In folder: " + targetFolder + " Executed: " + command);
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		try {
			pr = rt.exec(command, null, new File(targetFolder)); // IMPORTANT: By specifying targetFolder, all the cmds will be executed within such folder.
			pr.waitFor();
		} catch (IOException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}
		
				 
		BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		String resultString = "";
		try {
			
			while((line = bfr.readLine()) != null) {
				resultString += line;

			}
		} catch (IOException e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		}
		return resultString; 
	}
	 /** reads the result from the txt file produced and returns as List<String[]>
		 * 
		 * @param baseUrl String
		 * @param filename name of the file. 
		 * @return
		 * @throws IOException
		 */
		private List<String[]> readResult(String baseUrl,String filename) throws IOException {

	        String outputFile = baseUrl + "/"+filename;
	        String csv = new QueryBroker().readFileLocal(outputFile);
	        List<String[]> simulationResult = MatrixConverter.fromCsvToArray(csv);
			
			return simulationResult;
		}
	/**
     * Gets the latest file created using rdf4j
     * @return last created file
     */
    public String[] getLastModifiedDirectory(String agentiri, String location ) {
//    	agentiri = "http://www.theworldavatar.com/kb/agents/Service__ComposedADMS.owl#Service";
		List<String> lst = new ArrayList<String>();
		lst.add(location);
    	System.out.println(lst);
    	long millis = System.currentTimeMillis();
		String toSimulationTime = MetaDataAnnotator.getTimeInXsdTimeStampFormat(millis);
		String fromSimulationTime = MetaDataAnnotator.getTimeInXsdTimeStampFormat(millis-3600*1000); //the last 1hr
    	String resultfromfuseki = MetaDataQuery.queryResources(null,null,null,agentiri,  fromSimulationTime, toSimulationTime,null,lst);
		 String[] keys = JenaResultSetFormatter.getKeys(resultfromfuseki);
		 List<String[]> listmap = JenaResultSetFormatter.convertToListofStringArrays(resultfromfuseki, keys);
		 String dir=listmap.get(0)[0];
		 String simulationtime=listmap.get(0)[4];
		 String[]resp= {dir,simulationtime};
    	return resp;
    }
    /** read the coordinates of the station based on the iri
     * 
     * @param stationiri
     * @return
     */
    public String readCoordinate(String stationiri) {
		String sparqlQuery = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>" + 
				"PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#>" + 
				"PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#>" + 
				"PREFIX j6:<http://www.w3.org/2006/time#>" + 
				"PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#>" + 
				"SELECT Distinct  ?xval ?yval ?zval" + 
 
				"{graph <"+stationiri+">" + 
				"{ " + 
				"?entity   j7:hasGISCoordinateSystem ?coordsys ." + 
				"?coordsys   j7:hasProjectedCoordinate_x ?xent ." + 
				"?xent j2:hasValue ?vxent ." + 
				"?vxent   j2:numericalValue ?xval ." + 
				"?coordsys   j7:hasProjectedCoordinate_y ?yent ." + 
				"?yent j2:hasValue ?vyent ." + 
				"?vyent   j2:numericalValue ?yval . " +
				"?coordsys   j7:hasProjectedCoordinate_z ?zent ." + 
				"?zent j2:hasValue ?vzent ." + 
				"?vzent   j2:numericalValue ?zval . " + 
				"}" + 
//				"}" + 
				"ORDER BY DESC(?proptimeendval)LIMIT 1";
			String result2 = new QueryBroker().queryFile(stationiri, sparqlQuery);
			String[] keys2 = JenaResultSetFormatter.getKeys(result2);
			List<String[]> resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result2, keys2);
			String xVal = resultListfromquery.get(0)[0];
			String yVal = resultListfromquery.get(0)[1];
			String zVal = resultListfromquery.get(0)[2];
			StringJoiner sb = new StringJoiner(" ");
			sb.add(xVal);
			sb.add(yVal);
			sb.add(zVal);
			return "[" + sb.toString() + "]";
		
    }
    public void updateRepoNewMethod(String context,String propnameclass, String scaledvalue,String prescaledvalue, String newtimestamp) {
		String sensorinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "SELECT ?vprop ?proptimeval "
//				+ "WHERE " //it's replaced when named graph is used
				+ "{graph "+"<"+context+">"
				+ "{ "
				+ " ?prop a j4:"+propnameclass+" ."
				+ " ?prop   j2:hasValue ?vprop ." 
				+ " ?vprop   j6:hasTime ?proptime ."
				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ."
//				+ " ?proptime   j6:hasBeginning ?proptimestart ."
//				+ " ?proptime   j6:hasEnd ?proptimeend ."
//				+ " ?proptimestart   j6:inXSDDateTimeStamp ?proptimestartval ." 
//				+ " ?proptimeend   j6:inXSDDateTimeStamp ?proptimeendval ."
				+ "}" 
				+ "}" 
				+ "ORDER BY ASC(?proptimeval)LIMIT1";
		
		List<String[]> keyvaluemapold =queryEndPointDataset(sensorinfo);
		
		String sparqlupdate = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "WITH <" + context + ">"
				+ "DELETE { "
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue ?oldpropertydata ."
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue ?oldpropertydata2 ."
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp ?olddatatime ."
				//+ "<" + keyvaluemapold.get(0)[2]+ "> j6:inXSDDateTimeStamp ?olddataend ."
				+ "} "
				+ "INSERT {"
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue \""+scaledvalue+"\"^^xsd:double ."
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue \""+prescaledvalue+"\"^^xsd:double ."
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp \""+newtimestamp+"\" ." 
				//+ "<" + keyvaluemapold.get(0)[2]+ "> j6:inXSDDateTimeStamp \""+newtimestampend+"\" ." 
				+ "} "
				+ "WHERE { "
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue ?oldpropertydata ."	
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue ?oldpropertydata2 ."	
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp ?olddatatime ."
				//+ "<" + keyvaluemapold.get(0)[2]+ "> j6:inXSDDateTimeStamp ?olddataend ."
				+ "}";
		
			
			KnowledgeBaseClient.update(dataseturl, null, sparqlupdate); //update the dataset	
	}
	private List<String[]> queryEndPointDataset(String querycontext) {
		String resultfromrdf4j = KnowledgeBaseClient.query(dataseturl, null, querycontext);
		String[] keys = JenaResultSetFormatter.getKeys(resultfromrdf4j);
		List<String[]> listmap = JenaResultSetFormatter.convertToListofStringArrays(resultfromrdf4j, keys);
		return listmap;
	}

}
