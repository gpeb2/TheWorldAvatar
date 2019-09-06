package uk.ac.cam.cares.jps.scenarios.test;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.scenario.ScenarioLog;
import uk.ac.cam.cares.jps.scenario.ScenarioManagementAgent;

public class TestScenarioManagement extends TestCase {
	
//	public void testCreateScenarioDescription() {
//		
//		String scenarioName = "test1234567";
//		String agent = "http://www.theworldavatar.com/kb/agents/Service__OpenWeatherMap.owl";
//		
//		new ScenarioAgent().createScenarioDescription(scenarioName, agent);
//	}
	
	public void testCreateScenarioAgent() {
	
		String scenarioName = "testscenarioabc";
		ScenarioLog log = new ScenarioLog(scenarioName);
		JSONObject message = new JSONObject().put("agent", "http://www.theworldavatar.com/kb/agents/Service__OpenWeatherMap.owl#Service");
		message.put("operation", "mock");
		log.logMessage(scenarioName, message);
		
		JSONObject jo = new ScenarioManagementAgent().createScenarioAgent(scenarioName, log);
		
		System.out.println(jo);
		
		JSONArray joarray = jo.getJSONArray("service");
		// operations: mock, call, read, query, delete and one from OpenWeatherMap
		assertEquals(7, joarray.length());
	}
	
	
	public void testTmp() {
		List<String> list = new ScenarioManagementAgent().getScenarioIRIsOLD();
		for (String current : list) {
			System.out.println(current);
		}
	}
	
	public void testTmp3() {
		
		//String json = new ScenarioManagementAgent().listScenariosAndAgentsAsJson();
		//System.out.println(json);
//		String dir = "C:/Users/Andreas/my/JPSWorkspace/JParkSimulator-git/JPS_COMPOSITION/testres/admsservicesWithWasteProduct";
//		KeyValueServer.set(ServiceDiscovery.KEY_DIR_KB_AGENTS, dir);
//		System.out.println("dir=" + KeyValueServer.get(ServiceDiscovery.KEY_DIR_KB_AGENTS));
//		
//		ArrayList<Service> services = ServiceDiscovery.getInstance().getServices();
//		for (Service current : services) {
//			System.out.println(current.getOperations().get(0).getHttpUrl());
//		}
	}
	
	public void testTmp4() {
		
		String result =  new ScenarioManagementAgent().listScenariosAndAgentsAsJson();
		
		//String result = new ScenarioManagementAgent().listAgentsAsJson();
		//String result = new ScenarioManagementAgent().listScenariosAsJson();
		System.out.println(result);
	}
	
//	public void testTmp5() {
//		
//		String scenarioName = "aaaa";
//		String agent = "http://www.theworldavatar.com/kb/agents/Service__ADMS.owl#Service";
//		ScenarioHelper.getScenarioBucket(scenarioName);
//		new ScenarioAgent().createScenarioDescription(scenarioName, agent);
//	}
	

	

}
