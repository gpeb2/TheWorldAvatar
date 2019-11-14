package uk.ac.cam.cares.jps.ess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.CommandHelper;
import uk.ac.cam.cares.jps.base.util.MatrixConverter;


@WebServlet(urlPatterns = { "/ESSAgent" })

public class JPS_ESS extends JPSHttpServlet {

	private static final long serialVersionUID = -4199209974912271432L;
	private Logger logger = LoggerFactory.getLogger(JPS_ESS.class);
	//public static final String AGENT_TAG = "GAMS_NuclearAgent";
	private String modelname="NESS.gms";

	public static void runGAMSOld() throws IOException, InterruptedException {
		System.out.println("Start");
		System.out.println("separator= " + File.separator);
		String executablelocation = "C:/GAMS/win64/28.2/gams.exe";
		String folderlocation ="D:/Users/LONG01/Documents/gamsdir/projdir/";
//		String folderlocation = "C:/Users/GKAR01/Documents/gamsdir/projdir/";
		String[] cmdArray = new String[5];

		cmdArray[0] = executablelocation;
		cmdArray[1] = folderlocation + "NESS.gms";
		cmdArray[2] = "WDIR=" + folderlocation;
		cmdArray[3] = "SCRDIR=" + folderlocation;
		cmdArray[4] = "LO=2";

		String cmdArrayinstring = cmdArray[0] + " " + cmdArray[1] + "," + cmdArray[2] + "," + cmdArray[3] + " "
				+ cmdArray[4];

		System.out.println(cmdArrayinstring);
//        Process p = Runtime.getRuntime().exec(cmdArray);
//		   p.waitFor();
		// String startbatCommand ="C:/JPS_DATA/workingdir/JPS_POWSYS/gamsexecute.bat";

		ArrayList<String> groupcommand = new ArrayList<String>();

		groupcommand.add("D:/Users/LONG01/Documents/gamsdir/projdir/gamsexecute.bat");

		// CommandHelper.executeSingleCommand(folderlocation,startbatCommand);
		CommandHelper.executeCommands(folderlocation, groupcommand);
		System.out.println("Done");
	}


	public void runGAMS(String baseUrl) throws IOException, InterruptedException { // need gdx files to be in directory location 		
		
		modifyTemplate(baseUrl,modelname);

		
		logger.info("Start");
		//logger.info("separator= "+File.separator);
        String executablelocation ="C:/GAMS/win64/28.2/gams.exe"; //depends where is in claudius
        String folderlocation =baseUrl.replace("//", "/");
//        String folderlocation =baseUrl.replace("/","\\\\")+"\\";
        //String folderlocation ="C:/JPS_DATA/workingdir/JPS_POWSYS/parallelworld/";
        String[] cmdArray = new String[5];
        
        cmdArray[0] = executablelocation;
        cmdArray[1] = folderlocation+"/" + modelname;
        cmdArray[2] = "WDIR="+folderlocation;
        cmdArray[3] = "SCRDIR="+folderlocation;
        cmdArray[4] = "LO=2";

        
        String cmdArrayinstring=cmdArray[0]+" "+cmdArray[1]+","+cmdArray[2]+","+cmdArray[3]+" "+cmdArray[4];
        
        System.out.println(cmdArrayinstring);
        Process p = Runtime.getRuntime().exec(cmdArray);
		   p.waitFor();
         
		   System.out.println("Done");
	}
	
	public void modifyTemplatever2(String newdir2, String filename) throws IOException { 
		String newdir=newdir2.replace("/","\\\\");
		System.out.println("newdir2="+newdir2);
		System.out.println("newdir="+newdir);
		String destinationUrl = newdir2.replace("/","\\") + "\\"+filename;
		System.out.println("dest="+destinationUrl);
		File file = new File(AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/"+filename);
		System.out.println("FILE: FILE: "+ file);
        String fileContext = FileUtils.readFileToString(file);
        fileContext = fileContext.replaceAll("Ptlow.gdx",newdir+"\\\\"+"Ptlow.gdx");
        fileContext = fileContext.replaceAll("Pthigh.gdx",newdir+"\\\\"+"Pthigh.gdx");
        fileContext = fileContext.replaceAll("Dtlow.gdx",newdir+"\\\\"+"Dtlow.gdx");
        fileContext = fileContext.replaceAll("Dthigh.gdx",newdir+"\\\\"+"Dthigh.gdx");
        fileContext = fileContext.replaceAll("EnvironmentalScore.gdx",newdir+"\\\\"+"EnvironmentalScore.gdx");
        fileContext = fileContext.replaceAll("EconomicalScore.gdx",newdir+"\\\\"+"EconomicalScore.gdx");
        fileContext = fileContext.replaceAll("Maturity.gdx",newdir+"\\\\"+"Maturity.gdx");
        fileContext = fileContext.replaceAll("Pa_high.gdx",newdir+"\\\\"+"Pa_high.gdx");
        
        fileContext = fileContext.replaceAll("Ptlow.csv",newdir+"\\\\"+"Ptlow.csv output="+newdir+"\\\\"+"Ptlow.gdx");
        fileContext = fileContext.replaceAll("Pthigh.csv",newdir+"\\\\"+"Pthigh.csv output="+newdir+"\\\\"+"Pthigh.gdx");
        fileContext = fileContext.replaceAll("Dtlow.csv",newdir+"\\\\"+"Dtlow.csv output="+newdir+"\\\\"+"Dtlow.gdx");
        fileContext = fileContext.replaceAll("Dthigh.csv",newdir+"\\\\"+"Dthigh.csv output="+newdir+"\\\\"+"Dthigh.gdx");
        fileContext = fileContext.replaceAll("EnvironmentalScore.csv",newdir+"\\\\"+"EnvironmentalScore.csv output="+newdir+"\\\\"+"EnvironmentalScore.gdx");
        fileContext = fileContext.replaceAll("EconomicalScore.csv",newdir+"\\\\"+"EconomicalScore.csv output="+newdir+"\\\\"+"EconomicalScore.gdx");
        fileContext = fileContext.replaceAll("Maturity.csv",newdir+"\\\\"+"Maturity.csv output="+newdir+"\\\\"+"Maturity.gdx");
        fileContext = fileContext.replaceAll("Pa_high.csv",newdir+"\\\\"+"Pa_high.csv output="+newdir+"\\\\"+"Pa_high.gdx");
        
        //fileContext = fileContext.replaceAll("%gams.scrdir%soleps.gdx",newdir+"\\\\"+"soleps.gdx");
        //System.out.println(fileContext);
//        File fileout = new File(destinationUrl);
//        FileWriter fileWriter = new FileWriter(fileout);
//		fileWriter.write(fileContext);
//		fileWriter.close();
		new QueryBroker().put(destinationUrl, fileContext);
	}
	
	public void modifyTemplate(String newdir, String filename) throws IOException {
		newdir = newdir.replace("//", "/");
		String destinationUrl = newdir + "/"+filename;
		File file = new File(AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/"+filename);
		
        String fileContext = FileUtils.readFileToString(file);
		System.out.println("FILE: FILE: "+ file);
        
        fileContext = fileContext.replaceAll("Ptlow.gdx",newdir+"/Ptlow.gdx");
        fileContext = fileContext.replaceAll("Pthigh.gdx",newdir+"/Pthigh.gdx");
        fileContext = fileContext.replaceAll("Dtlow.gdx",newdir+"/Dtlow.gdx");
        fileContext = fileContext.replaceAll("Dthigh.gdx",newdir+"/Dthigh.gdx");
        fileContext = fileContext.replaceAll("EnvironmentalScore.gdx",newdir+"/EnvironmentalScore.gdx");
        fileContext = fileContext.replaceAll("EconomicalScore.gdx",newdir+"/EconomicalScore.gdx");
        fileContext = fileContext.replaceAll("Maturity.gdx",newdir+"/Maturity.gdx");
        fileContext = fileContext.replaceAll("Pa_high.gdx",newdir+"/Pa_high.gdx");
        
        fileContext = fileContext.replaceAll("Ptlow.csv",newdir+"/Ptlow.csv output="+newdir+"/Ptlow.gdx");
        fileContext = fileContext.replaceAll("Pthigh.csv",newdir+"/Pthigh.csv output="+newdir+"/Pthigh.gdx");
        fileContext = fileContext.replaceAll("Dtlow.csv",newdir+"/Dtlow.csv output="+newdir+"/Dtlow.gdx");
        fileContext = fileContext.replaceAll("Dthigh.csv",newdir+"/Dthigh.csv output="+newdir+"/Dthigh.gdx");
        fileContext = fileContext.replaceAll("EnvironmentalScore.csv",newdir+"/EnvironmentalScore.csv output="+newdir+"/EnvironmentalScore.gdx");
        fileContext = fileContext.replaceAll("EconomicalScore.csv",newdir+"/EconomicalScore.csv output="+newdir+"/EconomicalScore.gdx");
        fileContext = fileContext.replaceAll("Maturity.csv",newdir+"/Maturity.csv output="+newdir+"/Maturity.gdx");
        fileContext = fileContext.replaceAll("Pa_high.csv",newdir+"/Pa_high.csv output="+newdir+"/Pa_high.gdx");
        System.out.println("NEWDIR: "+ newdir);
        //FileUtils.write(file, fileContext);
 
		
		new QueryBroker().put(destinationUrl, fileContext);
	}
	
	public void copyTemplate(String newdir, String filename) {
		File file = new File(AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/"+filename);
		
		String destinationUrl = newdir + "/"+filename;
		new QueryBroker().put(destinationUrl, file);
	}
	
	public static OntModel readModelGreedy(String iriofnetwork) {
		String electricalnodeInfo = "PREFIX j1:<http://www.jparksimulator.com/ontology/ontoland/OntoLand.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "SELECT ?component "
				+ "WHERE {?entity  a  j2:CompositeSystem  ." + "?entity   j2:hasSubsystem ?component ." + "}";

		QueryBroker broker = new QueryBroker();
		return broker.readModelGreedy(iriofnetwork, electricalnodeInfo);
	}
	
	public void prepareCSV(String PVNetworkiri, String baseUrl) {
		OntModel model = readModelGreedy(PVNetworkiri);
		//System.out.println("model= "+model);

		String batteryquery = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysBehavior.owl#> "
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
				+ "SELECT ?Pa_high ?Da_low ?Pa_low ?Da_high " 
				+ "WHERE {?entity  a  j1:PhotovoltaicGenerator  ."

				+ "?entity   j6:hasMaximumActivePowerGenerated ?Pmax ." 
				+ "?Pmax     j2:hasValue ?vPmax ."
				+ "?vPmax  j5:upperLimit ?Pa_high ."

				+ "?entity   j6:hasMinimumActivePowerGenerated ?Pmin ." 
				+ "?Pmin     j2:hasValue ?vPmin ."
				+ "?vPmin  j5:lowerLimit ?Pa_low ."

				+ "?entity   j6:hasStateOfCharge ?dt ." 
				+ "?dt     j2:hasValue ?vdt ."
				+ "?vdt  j5:upperLimit ?Da_high ." 
				+ "?vdt  j5:lowerLimit ?Da_low ."
				+ "}";
		//?Pa_low ?Pa_high ?Da_high ?Da_low 

		ResultSet resultPV = JenaHelper.query(model, batteryquery);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultPV);
		String[] keyspv = JenaResultSetFormatter.getKeys(result);
		List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keyspv);

		List<String[]> resultListforcsv = new ArrayList<String[]>();
		String[] header = { "Parameters", "Value" };
		resultListforcsv.add(header);
		for (int x = 0; x < resultList.get(0).length; x++) {
			String[] line = { keyspv[x], resultList.get(0)[x] };
			resultListforcsv.add(line);
		}
		String s = MatrixConverter.fromArraytoCsv(resultListforcsv);
		new QueryBroker().put(baseUrl + "/Pa_high.csv", s);
	}
	
	protected void doGetJPS(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String baseUrl = QueryBroker.getLocalDataPath();// + "/GAMS_ESS";
		System.out.println("baseURL: " + baseUrl);
		JSONObject jofornuc = AgentCaller.readJsonParameter(request);
		String PVNetworkiri=jofornuc.getString("PVNetwork");
		System.out.println("PVNETWORK: " + PVNetworkiri);
		System.out.println("parameter got= "+jofornuc.toString());
		prepareCSV(PVNetworkiri,baseUrl);
		
		copyTemplate(baseUrl, "Ptlow.csv");
		copyTemplate(baseUrl, "Pthigh.csv");
		copyTemplate(baseUrl, "Dtlow.csv");
		copyTemplate(baseUrl, "Dthigh.csv");
		copyTemplate(baseUrl, "EnvironmentalScore.csv");
		copyTemplate(baseUrl, "EconomicalScore.csv");
		copyTemplate(baseUrl, "Maturity.csv");
		
		try {
			runGAMS(baseUrl);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			
			
		}
	}
}
