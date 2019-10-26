package uk.ac.cam.cares.jps.scenarios.test;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.discovery.MediaType;
import uk.ac.cam.cares.jps.base.util.FileUtil;

public abstract class TestKnowledgeBaseHelper extends TestCase {

	private KnowledgeBaseSwitchClient client = null;
	protected String datasetUrl = null;
	private long startTime = -1;
	
	protected void printTime(String s) {
		
		long endTime = System.currentTimeMillis();
		
		if (s == null) {
			startTime = endTime;
			return;
		}

		if (startTime > 0) {
			long diff = endTime - startTime;
			System.out.println(s + " diff = " + diff);
		}
		startTime = endTime;
	}
	
	public void createClient(String datasetUrl, boolean direct) {
		this.datasetUrl = datasetUrl;
		client = new KnowledgeBaseSwitchClient(datasetUrl, direct);
	}
	
	protected KnowledgeBaseSwitchClient client() {
		return client;
	}
	
	public File[] getBuildingFiles(int from, int to) {
		File dir = new File("D:/myTemp/cityGML/cityGML/buildingsthehaguenamedgraphs");
		File[] files = dir.listFiles();
		return Arrays.copyOfRange(files, from, to);
	}
	
	public File getFile(int pos) {
		return getBuildingFiles(pos,pos)[0];
	}
	
	protected String getE303LoadUrl() {
		return datasetUrl + "/some/path/testE-303load.owl";
	}
	
	protected String putE303Load(String datasetUrl, String targetUrl) {
		return putE303Load(datasetUrl, targetUrl, null);
	}
	
	protected String putE303Load(String datasetUrl, String targetUrl, String numbermarker) {
		String filePath = AgentLocator.getCurrentJpsAppDirectory(this) + "/testres" + "/E-303load.owl";
		String body = FileUtil.readFileLocally(filePath);
		if (numbermarker != null) {
			body = body.replace("0.27", numbermarker);
		}
		client().put(targetUrl, body, MediaType.APPLICATION_RDF_XML.type);
		return body;
	}
	
	protected void assertMarkerInE303Load(String content, String numbermarker) {
		String match = numbermarker + "</system:numericalValue>";
		assertTrue(content.contains(match));
	}
}
