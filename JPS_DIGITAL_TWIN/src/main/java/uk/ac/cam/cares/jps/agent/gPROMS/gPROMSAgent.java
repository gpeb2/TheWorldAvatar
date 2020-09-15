package uk.ac.cam.cares.jps.agent.gPROMS;

import java.io.File;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter; 

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FileUtils;

import org.json.JSONObject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.ac.cam.cares.jps.agent.configuration.gPROMSAgentConfiguration;
import uk.ac.cam.cares.jps.agent.configuration.gPROMSAgentProperty;
//import uk.ac.cam.cares.jps.agent.gPROMS.gPROMSAgent;
//import uk.ac.cam.cares.jps.agent.gPROMS.gPROMSAgentException;
import uk.ac.cam.cares.jps.agent.utils.ZipUtility;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.slurm.job.JobSubmission;
import uk.ac.cam.cares.jps.base.slurm.job.PostProcessing;
import uk.ac.cam.cares.jps.base.slurm.job.SlurmJobException;
import uk.ac.cam.cares.jps.base.slurm.job.Status;
import uk.ac.cam.cares.jps.base.slurm.job.Utils;
import uk.ac.cam.cares.jps.base.util.FileUtil;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;


/**
 * gPROMS Agent developed for setting-up and running gPROMS chemical network on
 * HPC.
 *The input files for gPROMS execution should be placed in user.home//input folder
 * @author Aravind Devanand (aravind@u.nus.edu)
 *
 */
@Controller
@WebServlet(urlPatterns = { gPROMSAgent.JOB_REQUEST_PATH, gPROMSAgent.JOB_STATISTICS_PATH,
		/* gPROMSAgent.JOB_OUTPUT_REQUEST_PATH */ })
public class gPROMSAgent extends JPSAgent {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(gPROMSAgent.class);
	private File workspace;
	static JobSubmission jobSubmission;
	public static ApplicationContext applicationContextgPROMSAgent;
	public static gPROMSAgentProperty gPROMSAgentProperty;

	public static final String BAD_REQUEST_MESSAGE_KEY = "message";
	public static final String UNKNOWN_REQUEST = "The request is unknown to gPROMS Agent";

	public static final String JOB_REQUEST_PATH = "/job/request";
	public static final String JOB_OUTPUT_REQUEST_PATH = "/job/output/request";
	public static final String JOB_STATISTICS_PATH = "/job/statistics";
	public static final String JOB_SHOW_STATISTICS_PATH = "/job/show/statistics";
	
	// Create a temporary folder in the user's home location
	private Path temporaryDirectory = null;

	public JSONObject produceStatistics(String input) throws IOException, gPROMSAgentException {
		System.out.println("Received a request to send statistics.\n");
		logger.info("Received a request to send statistics.\n");
		// Initialises all properties required for this agent to set-up<br>
		// and run jobs. It will also initialise the unique instance of<br>
		// Job Submission class.
		initAgentProperty();
		return jobSubmission.getStatistics(input);
	}

	@RequestMapping(value = gPROMSAgent.JOB_SHOW_STATISTICS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public String showStatistics() throws IOException, gPROMSAgentException {
		System.out.println("Received a request to show statistics.\n");
		logger.info("Received a request to show statistics.\n");
		initAgentProperty();
		return jobSubmission.getStatistics();
	}

	/**
	 * Starts the asynchronous scheduler to monitor quantum jobs.
	 *
	 * @throws gPROMSAgentException
	 */

	public void init() throws ServletException {
		logger.info("---------- gPROMS Simulation Agent has started ----------");
		System.out.println("---------- gPROMS Simulation Agent has started ----------");
		System.out.println(System.getProperty("user.dir"));
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		gPROMSAgent gPROMSAgent = new gPROMSAgent();
		// initialising classes to read properties from the gPROMS-agent.properites
		// file
		initAgentProperty();
		// In the following method call, the parameter getAgentInitialDelay-<br>
		// ToStartJobMonitoring refers to the delay (in seconds) before<br>
		// the job scheduler starts and getAgentPeriodicActionInterval<br>
		// refers to the interval between two consecutive executions of<br>
		// the scheduler.
		executorService.scheduleAtFixedRate(() -> {
			try {
				gPROMSAgent.monitorJobs();
			} catch (SlurmJobException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, gPROMSAgentProperty.getAgentInitialDelayToStartJobMonitoring(),
				gPROMSAgentProperty.getAgentPeriodicActionInterval(), TimeUnit.SECONDS);
		logger.info("---------- gPROMS Simulation jobs are being monitored  ----------");
		System.out.println("---------- gPROMS Simulation jobs are being monitored  ----------");

	}


	/**
	 * Initialises the unique instance of the gPROMSAgentProperty class that<br>
	 * reads all properties of gPROMSAgent from the kinetics-agent property
	 * file.<br>
	 *
	 * Initialises the unique instance of the SlurmJobProperty class and<br>
	 * sets all properties by reading them from the kinetics-agent property file<br>
	 * through the gPROMSAgent class.
	 */
	public void initAgentProperty() {
		// initialising classes to read properties from the kinetics-agent.properites
		// file
		if (applicationContextgPROMSAgent == null) {
			applicationContextgPROMSAgent = new AnnotationConfigApplicationContext(gPROMSAgentConfiguration.class);
		}
		if (gPROMSAgentProperty == null) {
			gPROMSAgentProperty = applicationContextgPROMSAgent.getBean(gPROMSAgentProperty.class);
		}
		if (jobSubmission == null) {
			jobSubmission = new JobSubmission(gPROMSAgentProperty.getAgentClass(), gPROMSAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setHpcServerLoginUserName(gPROMSAgentProperty.getHpcServerLoginUserName());
			jobSubmission.slurmJobProperty
					.setHpcServerLoginUserPassword(gPROMSAgentProperty.getHpcServerLoginUserPassword());
			jobSubmission.slurmJobProperty.setAgentClass(gPROMSAgentProperty.getAgentClass());
			jobSubmission.slurmJobProperty
					.setAgentCompletedJobsSpacePrefix(gPROMSAgentProperty.getAgentCompletedJobsSpacePrefix());
			jobSubmission.slurmJobProperty
					.setAgentFailedJobsSpacePrefix(gPROMSAgentProperty.getAgentFailedJobsSpacePrefix());
			jobSubmission.slurmJobProperty.setHpcAddress(gPROMSAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setInputFileName(gPROMSAgentProperty.getInputFileName());
			jobSubmission.slurmJobProperty.setInputFileExtension(gPROMSAgentProperty.getInputFileExtension());
			jobSubmission.slurmJobProperty.setOutputFileName(gPROMSAgentProperty.getOutputFileName());
			jobSubmission.slurmJobProperty.setOutputFileExtension(gPROMSAgentProperty.getOutputFileExtension());
			jobSubmission.slurmJobProperty.setJsonInputFileName(gPROMSAgentProperty.getJsonInputFileName());
			jobSubmission.slurmJobProperty.setJsonFileExtension(gPROMSAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setJsonFileExtension(gPROMSAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setSlurmScriptFileName(gPROMSAgentProperty.getSlurmScriptFileName());
			jobSubmission.slurmJobProperty.setMaxNumberOfHPCJobs(gPROMSAgentProperty.getMaxNumberOfHPCJobs());
			jobSubmission.slurmJobProperty.setAgentInitialDelayToStartJobMonitoring(
					gPROMSAgentProperty.getAgentInitialDelayToStartJobMonitoring());
			jobSubmission.slurmJobProperty
					.setAgentPeriodicActionInterval(gPROMSAgentProperty.getAgentPeriodicActionInterval());
		}
	}

	/**
	 * Receives and processes HTTP requests that match with the URL patterns<br>
	 * listed in the annotations of this class.
	 *
	 */
	@Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		String path = request.getServletPath();
		System.out.println("A request has been received..............................");
		if (path.equals(gPROMSAgent.JOB_REQUEST_PATH)) {
			try {
				return setUpJob(requestParams.toString());
			} catch (SlurmJobException | IOException | gPROMSAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
			// } else if (path.equals(gPROMSAgent.JOB_OUTPUT_REQUEST_PATH)) {
			// JSONObject result = getSimulationResults(requestParams);
			// return result;
		} else if (path.equals(gPROMSAgent.JOB_STATISTICS_PATH)) {
			try {
				return produceStatistics(requestParams.toString());
			} catch (IOException | gPROMSAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
		} else {
			System.out.println("Unknown request");
			throw new JPSRuntimeException(UNKNOWN_REQUEST);
		}
	}

	/**
	 * Validates input parameters specific to Kinetics Agent to decide whether<br>
	 * the job set up request can be served.
	 */
	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {
		if (requestParams.isEmpty()) {
			throw new BadRequestException();
		}
		return true;
	}

	/**
	 * Checks the status of a job and returns results if it is finished and<br>
	 * post-processing is successfully completed. If the job has terminated<br>
	 * with an error or failed, then error termination message is sent.
	 *
	 * The JSON input for this request has the following format: {"jobId":
	 * "login-skylake.hpc.cam.ac.uk_117804308649998"}
	 *
	 * @param requestParams
	 * @return
	 */
	private JSONObject getSimulationResults(JSONObject requestParams) {
		JSONObject json = new JSONObject();
		String jobId = getJobId(requestParams);
		if (jobId == null) {
			return json.put("message", "jobId is not present in the request parameters.");
		}
		initAgentProperty();
		JSONObject message = checkJobInWorkspace(jobId);
		if (message != null) {
			return message;
		}
		JSONObject result = checkJobInCompletedJobs(jobId);
		if (result != null) {
			return result;
		}
		message = checkJobInFailedJobs(jobId);
		if (message != null) {
			return message;
		}
		return json.put("message", "The job is not available in the system.");
	}

	/**
	 * Checks the presence of the requested job in the workspace.<br>
	 * If the job is available, it returns that the job is currently running.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInWorkspace(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the set-up and running jobs folder.
		workspace = jobSubmission.getWorkspaceDirectory();
		if (workspace.isDirectory()) {
			File[] jobFolders = workspace.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message", "The job is being executed.");
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the completed jobs.<br>
	 * If the job is available, it returns the result.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInCompletedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the completed jobs folder.
		String completedJobsPath = workspace.getParent().concat(File.separator)
				.concat(gPROMSAgentProperty.getAgentCompletedJobsSpacePrefix()).concat(workspace.getName());
		File completedJobsFolder = new File(completedJobsPath);
		if (completedJobsFolder.isDirectory()) {
			File[] jobFolders = completedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					try {
						String inputJsonPath = completedJobsPath.concat(File.separator).concat(jobFolder.getName())
								.concat(File.separator).concat(gPROMSAgentProperty.getReferenceOutputJsonFile());
						InputStream inputStream = new FileInputStream(inputJsonPath);
						return new JSONObject(FileUtil.inputStreamToString(inputStream));
					} catch (FileNotFoundException e) {
						return json.put("message",
								"The job has been completed, but the file that contains results is not found.");
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the failed jobs.<br>
	 * If the job is available, it returns a message saying that<br>
	 * job has failed.
	 *
	 * @param json
	 * @param jobId
	 * @return
	 */
	private JSONObject checkJobInFailedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the failed jobs folder.
		String failedJobsPath = workspace.getParent().concat(File.separator)
				.concat(gPROMSAgentProperty.getAgentFailedJobsSpacePrefix()).concat(workspace.getName());
		File failedJobsFolder = new File(failedJobsPath);
		if (failedJobsFolder.isDirectory()) {
			File[] jobFolders = failedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message",
							"The job terminated with an error. Please check the failed jobs folder.");
				}
			}
		}
		return null;
	}

	/**
	 * Monitors already set up jobs.
	 *
	 * @throws SlurmJobException
	 */
	private void monitorJobs() throws SlurmJobException {
		// Configures all properties required for setting-up and running a Slurm job.
		jobSubmission.monitorJobs();

	}

	/**
	 * Sets up a quantum job by creating the job folder and the following files</br>
	 * under this folder:</br>
	 * - the input file.</br>
	 * - the Slurm script file.</br. - the Status file.</br>
	 * - the JSON input file, which comes from the user request.</br>
	 *
	 * @param jsonString
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	public JSONObject setUpJob(String jsonString) throws IOException, gPROMSAgentException, SlurmJobException {
		String message = setUpJobOnAgentMachine(jsonString);
		JSONObject obj = new JSONObject();
		obj.put("jobId", message);
		return obj;
	}

	/**
	 * Sets up the quantum job for the current input.
	 *
	 * @param jsonInput
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	private String setUpJobOnAgentMachine(String jsonInput)
			throws IOException, gPROMSAgentException, SlurmJobException {
		initAgentProperty();
		long timeStamp = Utils.getTimeStamp();
		String jobFolderName = getNewJobFolderName(gPROMSAgentProperty.getHpcAddress(), timeStamp);
		System.out.println("Jobfolder is"+ jobFolderName);
		//
		
		
		Path temporaryDirectory1 = Paths.get(System.getProperty("user.home"), "." + jobFolderName);
		System.out.println("tempdir is"+ temporaryDirectory1.toString());
		System.out.println("tempdir1 is"+ temporaryDirectory1.toString());
		System.out.println("userdir is"+ System.getProperty("user.dir"));
		System.out.println("scrptdir is"+gPROMSAgentProperty.getAgentScriptsLocation().toString());
		return jobSubmission.setUpJob(jsonInput,
				new File(URLDecoder.decode(getClass().getClassLoader().getResource(gPROMSAgentProperty.getSlurmScriptFileName())
						.getPath(), "utf-8")),
			/**	new File("C:/Users/caresadmin/JParkSimulator-git/JPS_DIGITAL_TWIN/src/main/resources/input.zip"),
			*	timeStamp);
			*/
				getInputFile(jsonInput, jobFolderName), timeStamp);
	}			
	

	/**
	 * Prepares input files, bundle them in a zip file and return the zip file to the calling method.
	 *
	 * @param jsonInput
	 * @param jobFolderName
	 * @return
	 * @throws IOException
	 * @throws gPROMSAgentException
	 */
	private File getInputFile(String jsonInput, String jobFolderName) throws IOException, gPROMSAgentException {
		
		//Preparation of settings.input file
		
		
		
		

//Extracting required variables from owl files
			System.out.println(System.getProperty("user.dir"));
		   String filePath = System.getProperty("user.home")+"\\input\\debutaniser_section.owl";

			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

			try {
			    File file = new File(filePath);
			    FileInputStream reader = new FileInputStream(file);
			    model.read(reader,null);     //load the ontology model
			} catch (Exception e) {
			    e.printStackTrace();
			}
//Sample Sparql query
//			String sparqlQuery =
//					"SELECT ?Temp\n"+
//					"WHERE {\n "+
//					"?x a <http://www.semanticweb.org/caresadmin1/ontologies/2020/5/untitled-ontology-396#Temperature> .\n"+	
//					"?x  <http://www.semanticweb.org/caresadmin1/ontologies/2020/5/untitled-ontology-396#hasValue>  ?Temp .\n"+
//					"}" ;
//			//System.err.println(sparqlQuery); //Prints the query
//			Query query = QueryFactory.create(sparqlQuery);
//
//			QueryExecution qe = QueryExecutionFactory.create(query, model);
//
//			ResultSet results = qe.execSelect();
//			//ResultSetFormatter.out(System.out, results, query);			
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
//			ResultSetFormatter.outputAsCSV(byteArrayOutputStream,results);
//			String s=byteArrayOutputStream.toString();
//			System.out.println(s);
//			//List se=Arrays.asList(s.split("\\s*,\\s*|http"));
//			//System.out.println(se);
//			String[] sa= s.split("\\r?\\n");
//			//System.out.println(Arrays.toString(sa[1]));
//			System.out.println(sa[1]);

//Trial one with the debutaniser file
			
			String TempQuery =
					"PREFIX process:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_function/process.owl#>\r\n" +
					"PREFIX system:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>\r\n" +
					"\r \n"+
					"SELECT ?Temp\n"+
					"WHERE {\n "+
					"?x a  system:ScalarValue  .\n" + 
					"?x system:value ?Temp .\n"+
					"}" ;
			//System.err.println(TempQuery); //Prints the query
			Query queryt = QueryFactory.create(TempQuery);

			QueryExecution qet = QueryExecutionFactory.create(queryt, model);

			ResultSet results = qet.execSelect();
			//ResultSetFormatter.out(System.out, results, query);			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
			ResultSetFormatter.outputAsCSV(byteArrayOutputStream,results);
			String s=byteArrayOutputStream.toString();
			System.out.println(s);
			//List se=Arrays.asList(s.split("\\s*,\\s*|http"));
			//System.out.println(se);
			String[] sa= s.split("\\r?\\n");
			//System.out.println(Arrays.toString(sa[1]));
			System.out.println(sa[1]);			
			
//Once the file is created, data has to be written to it
		  try {
			  String input = System.getProperty("user.home")+"\\input\\Settings.input";
		      FileWriter myWriter = new FileWriter(input);
		      myWriter.write("Feed__T \n");
		      myWriter.write(sa[1]);
		      myWriter.write("\n");
		      myWriter.write("Feed__P\n");
		      myWriter.write(sa[2]);
		      myWriter.write("\nstep1__initial_value \n");
		      myWriter.write("step1__final_value \n");
		      myWriter.write("step2__initial_value \n");
		      myWriter.write("step2__final_value \n");
		      myWriter.write("step3__initial_value \n");
		      myWriter.write("step3__final_value \n");
		      myWriter.write("step4__initial_value \n");
		      myWriter.write("step4__final_value \n");
		      
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	
		
		
		
		
		
		
		
		
		// Compress all files in the temporary directory into a ZIP
		//Path zipFile = Paths.get(System.getProperty("user.home"), temporaryDirectory.getFileName().toString() + ".zip");
		Path zipFile = Paths.get(System.getProperty("user.home")+"\\input.zip");
		// Create a temporary folder in the user's home location
		//Path temporaryDirectory= Paths.get("C:\\Users\\caresadmin\\JParkSimulator-git\\JPS_DIGITAL_TWIN\\src\\main\\resources\\input");

		Path temporaryDirectory= Paths.get(System.getProperty("user.home")+"\\input");
		// Path temporaryDirectory= Paths.get("C:\\Users\\caresadmin\\JParkSimulator-git\\JPS_DIGITAL_TWIN\\src\\main\\resources\\input");
		List<File> zipContents = new ArrayList<>();

		Files.walk(temporaryDirectory)
			.map(Path::toFile)
			.forEach((File f) -> zipContents.add(f));
		zipContents.remove(temporaryDirectory.toFile());

		// Will throw an IOException if something goes wrong
		new ZipUtility().zip(zipContents, zipFile.toString());

		// Return the final ZIP file
		return new File(zipFile.toString());
	}


	/**
	 * Produces a job folder name by following the schema hpcAddress_timestamp.
	 *
	 * @param hpcAddress
	 * @param timeStamp
	 * @return
	 */
	public String getNewJobFolderName(String hpcAddress, long timeStamp) {
		return hpcAddress.concat("_").concat("" + timeStamp);
	}

	/**
	 * Returns the job id.
	 *
	 * @param jsonObject
	 * @return
	 */
	public String getJobId(JSONObject jsonObject) {
		if (jsonObject.has("jobId")) {
			return jsonObject.get("jobId").toString();
		} else {
			return null;
		}

	}
}
