package uk.ac.cam.cares.jps.wte.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.ontology.OntModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.wte.FCQuerySource;
import uk.ac.cam.cares.jps.wte.WastetoEnergyAgent;
import uk.ac.cam.cares.jps.wte.visualization.WTEVisualization;

public class TestVisualization {
	static String iriofnetwork= null;
	@Before
	public void setUp() {
		iriofnetwork="http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem";
	}
	/** produces xy coordinates of each FC on the network. 
	 * tests if the result is empty or if it contains the coordinates/values
	 */
	@Test
	public void testFCQueryDirect(){
		WTEVisualization a = new WTEVisualization();
		JSONObject jo = new JSONObject();

		try {
		OntModel model = FCQuerySource.readModelGreedy(iriofnetwork);
			String result = a.createMarkers(model, jo);
			assertNotNull(result);
			JSONObject fcMap = new JSONObject(result);
			assertTrue(fcMap.has("result"));
			//result has to be a string and not a JSONobject because of how javascript retrieves the value
			JSONArray coordinates = fcMap.getJSONArray("result");
			System.out.println(coordinates.length());
			assertNotNull(coordinates);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/** produces xy coordinates of each FC on the network. 
	 *  tests if the result is empty or if it contains the coordinates/values
	 *  calls via agentcaller rather than user
	 */
	@Test
	public void testFCQueryAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				iriofnetwork);
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/createMarkers", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * returns only one entry (OnsiteWTF#0) in base case
	 * if it returns more than one, then it's probably modified. restore from base case your WasteNetwork OWL
	 */
	@Test
	public void testOnsiteDirect(){ 
		// OnSiteWasteTreatment-0
		WTEVisualization a = new WTEVisualization();
		JSONObject jo = new JSONObject();
		OntModel model = FCQuerySource.readModelGreedy(iriofnetwork);
		try {
			String result = a.searchOnsite(model, jo);
			assertNotNull(result);
			JSONObject fcMap = new JSONObject(result);
			assertTrue(fcMap.has("result"));
			//result has to be a string and not a JSONobject because of how javascript retrieves the value
			JSONArray coordinates = fcMap.getJSONArray("result");
			assertEquals(coordinates.length(),1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * returns only one entry (OnsiteWTF#0) in base case
	 * if it returns more than one, then it's probably modified. restore from base case. 
	 */
	@Test
	public void testOnsiteQueryAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				iriofnetwork);
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/queryOnsite", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/** returns tech outputs, separated into JSON Objects
	 * {onsite:[tax, installationcost, operationcost, manpowercost], offsite:[]}
	 * 
	 */
	@Test
	public void testreadInputsDirect(){
		WTEVisualization a = new WTEVisualization();
		try {
		OntModel model = FCQuerySource.readModelGreedy(iriofnetwork);
		String g = a.readInputs(model);
		JSONObject jo = new JSONObject(g);
		System.out.println(g);

		assertNotNull(g);
		assertTrue(jo.has("offsite"));
		assertTrue(jo.has("onsite"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testreadInputsAgent(){
		JSONObject jo = new JSONObject().put("wastenetwork",
				iriofnetwork);
		try {
			String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_WTE/WTEVisualization/readInputs", jo.toString());
			System.out.println(resultStart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
