package uk.ac.cam.cares.jps.men;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.discovery.AgentRequest;
import uk.ac.cam.cares.jps.base.discovery.AgentResponse;
import uk.ac.cam.cares.jps.base.discovery.Parameter;



@WebServlet(urlPatterns = {"/MENTableAgent"})

public class MenTableAgent extends HttpServlet {
	

	
	private static final long serialVersionUID = -4199209974912271432L;
	Logger logger = LoggerFactory.getLogger(MenTableAgent.class);
	
	private String getContextPathForJPSMen() {
		return "/JPS_MEN/MENAgent";
	}
	
	private String getAnnualCostFactor(int x, String[] annualcostfactor) {
		String ACF[]= new String[annualcostfactor.length];
		for (int a=0;a<annualcostfactor.length;a++)
		{
			ACF[a]=String.valueOf(1/Double.valueOf(annualcostfactor[a])); 
			if (1/Double.valueOf(annualcostfactor[a])>=1)
			{
			ACF[a]=String.format("%.1f",1/Double.valueOf(annualcostfactor[a]));
			}
		}
		
		return ACF[x]; 
	}
	
	private String getTransportationFile() {
		
	//	String baseDir = AgentLocator.getProperty("absdir.jps_men");
		
		//return baseDir + "/testres/transportation/Jr_Transportation_simplified.owl"; // location of the owl file that contains information for the transportation system
		return "http://www.jparksimulator.com/kb/sgp/jurongisland/MaterialTransportMode.owl";
	}
	
	public String getChemicalPlants() {

		//return "http://www.jparksimulator.com/JurongIsland.owl";
		return "http://www.theworldavatar.com/kb/sgp/jurongisland/JurongIsland.owl";
	}
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		logger.info("MEN_Table Agent start");
		request.setCharacterEncoding("UTF-8");
		
		String jsonString = request.getParameter("agentrequest");
		
		logger.info("jsonstring= "+jsonString);
		
		
		
		// read form fields	
		JSONArray queryresult = null;
		String carbontax="0.0";
		String interestfactor = "1.0";
		String intmarketpricefactor="0.0";
		String intmarketlowestprice="0.0";
		JSONArray annualfactor= null;
		JSONObject carbontaxobj;
		try {
			queryresult = new JSONObject(jsonString).getJSONArray("inputParameters");
			carbontaxobj = queryresult.getJSONObject(0);
			carbontax = "" + carbontaxobj.getDouble("value");
			logger.info("carbontaxobj= "+carbontaxobj);
			logger.info("value= "+carbontax);
			intmarketpricefactor= "" + queryresult.getJSONObject(2).getDouble("value");
			intmarketlowestprice = "" + queryresult.getJSONObject(3).getBoolean("value");
			annualfactor=queryresult.getJSONObject(4).getJSONArray("value");
			
		} catch (JSONException e1) {
			logger.error(e1.getMessage(), e1);
		}

		int sizeofparameterjson= queryresult.length();
		logger.info("valueof parameter size= "+sizeofparameterjson);
		
		String[] annualcostfactor= new String[annualfactor.length()];
		for (int i=0; i<annualfactor.length(); i++) {
			annualcostfactor[i] = annualfactor.getString(i);
		}	
	
		String resultjson;
		JSONObject json = new JSONObject();

		Double[][] totaltransportcost= new Double[annualcostfactor.length][annualcostfactor.length];

		JSONArray tmpc = new JSONArray();
		JSONArray ttc = new JSONArray();
		JSONArray tic = new JSONArray();
		JSONArray tcec = new JSONArray();
		JSONArray tce = new JSONArray();
		JSONArray ttyc = new JSONArray();
		
		for (int time = 0; time < annualcostfactor.length; time++) {
			
			AgentRequest agrequest = new AgentRequest();
			Parameter param = new Parameter("transportationModes", getTransportationFile());
			agrequest.getInputParameters().add(param);

			Parameter param2 = new Parameter("eco-industrialpark", getChemicalPlants());
			agrequest.getInputParameters().add(param2);

			// additional Parameters --> five parameters
			Parameter param3 = new Parameter("CarbonTax", carbontax);
			agrequest.getInputParameters().add(param3);

			Parameter param4 = new Parameter("InterestFactor", interestfactor);
			agrequest.getInputParameters().add(param4);

			Parameter param5 = new Parameter("AnnualCostFactor", getAnnualCostFactor(time,annualcostfactor));
			agrequest.getInputParameters().add(param5);

			Parameter param6 = new Parameter("InternationalMarketPriceFactor", intmarketpricefactor);
			agrequest.getInputParameters().add(param6);

			Parameter param7 = new Parameter("InternationalMarketLowestPriceApplied", intmarketlowestprice);
			agrequest.getInputParameters().add(param7);

			AgentResponse resp = AgentCaller.callAgent(getContextPathForJPSMen(), agrequest);
			
			Double totalMaterialPurchaseCost= Double.valueOf((String) resp.getOutputParameters().get(1).getValue())/1000000000;
			Double totalTransportationCost= Double.valueOf((String) resp.getOutputParameters().get(3).getValue())/1000;
			Double totalInstallationCost= Double.valueOf((String) resp.getOutputParameters().get(6).getValue())/1000000;
			Double totalCO2Emission= Double.valueOf((String) resp.getOutputParameters().get(4).getValue());
			Double totalCO2EmissionCost= Double.valueOf((String) resp.getOutputParameters().get(5).getValue())/1000;
			
			tmpc.put(String.format("%.2f",totalMaterialPurchaseCost));
			ttc.put(String.format("%.2f", totalTransportationCost));
			tce.put(String.format("%.2f", totalCO2Emission));
			tcec.put(String.format("%.2f", totalCO2EmissionCost));
			tic.put(String.format("%.2f", totalInstallationCost));			 
			
		
		}
		for (int count=0;count<annualcostfactor.length;count++)
		{
			for (int count2=0;count2<annualcostfactor.length;count2++)
			{
				try {
					totaltransportcost[count][count2]=(Double.valueOf(annualcostfactor[count2])* ttc.getDouble(count)*1000+tic.getDouble(count)*1000000)/1000000;
				} catch (NumberFormatException | JSONException e) {
					logger.error(e.getMessage());

				}
				ttyc.put(String.format("%.2f", totaltransportcost[count][count2]));
			}
				
		}

		try {
			json.put("totalMaterialPurchaseCost", tmpc);
			json.put("totalTransportationCost", ttc);
			json.put("totalCO2Emission", tce);
			json.put("totalCO2EmissionCost", tcec);
			json.put("totalInstallationCost", tic);
			json.put("totaltransportyearcost",ttyc);
		} catch (JSONException e) {
			logger.error(e.getMessage());

		}
		
		
		resultjson = json.toString();
		logger.info("response of the agent=" + resultjson);

		
		response.setContentType("application/json");
	
		response.getWriter().write(resultjson);

		logger.info("MEN_Table Agent stop");
	}
	
	 public void doPost(HttpServletRequest request, HttpServletResponse response)
			    throws IOException, ServletException
			    {
			        doGet(request, response);
			    }
	
	 
}
