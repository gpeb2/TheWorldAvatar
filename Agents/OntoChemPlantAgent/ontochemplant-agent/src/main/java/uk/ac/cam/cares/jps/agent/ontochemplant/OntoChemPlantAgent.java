package uk.ac.cam.cares.jps.agent.ontochemplant;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.ac.cam.cares.ogm.models.ModelContext;
import uk.ac.cam.cares.jps.agent.model.ontochemplant.Building;
import uk.ac.cam.cares.jps.agent.model.ontochemplant.CityObject;
import uk.ac.cam.cares.jps.agent.model.ontochemplant.OntoChemPlantModel;
import uk.ac.cam.cares.jps.agent.model.ontochemplant.PlantItem;

/**
 * This agent queries the knowledge graph for information on various structures on Jurong Island, and interacts with the 
 * City Information Agent (CIA) to display this information on the Jurong Island web visualisation 
 * found at http://www.theworldavatar.com:83/citieskgweb/index.html?city=jurongisland?context=chemplant
 * 
 * In order to query the KG, a CityObject IRI as a JSON Object is passed to this agent as an input. This IRI is sent by the CIA 
 * when a user interacts with a structure on the web visualisation. The agent then creates a model using this IRI based on the
 * Modeling Engine Framework, and pulls relevant information regarding the structure. The output information is sent back to 
 * the CIA as a JSON Object.
 * 
 * @author Srishti Ganguly
 */

public class OntoChemPlantAgent {
	
	private static final String NAMESPACE = "jriEPSG24500";
	private static final String TWA_NAMESPACE = "jibusinessunits";
	private static final String GRAPH = "http://www.theworldavatar.com:83/citieskg/namespace/jriEPSG24500/sparql/";
	private static final String OBJECT_CLASS_ID = "objectClassId";
	private static final String CITY_OBJECT = "cityobject";
	private static final String BUILDING = "building";
	private static final String CITY_FURNITURE = "cityfurniture";

	public static JSONObject createOntoChemPlantModel(JSONObject input) throws SQLException {
		
		JSONArray result = new JSONArray();
        JSONObject final_results = new JSONObject();
        String cityObjectIri = (input.getJSONArray(OntoChemPlantAgentLauncher.IRI)).getString(0);

        ModelContext city_object_context = new ModelContext(NAMESPACE, GRAPH);
        CityObject cityObject = city_object_context.createHollowModel(CityObject.class, cityObjectIri);
        city_object_context.pullPartial(cityObject, OBJECT_CLASS_ID);

        // New model context for jibusinessunits namespace
        ModelContext context = new ModelContext(TWA_NAMESPACE);
        
        /* 
         * Decide whether the selected object is a building or plant item based on its object class ID in 
         * CityGML, and pull information from KG. City furniture object ID = 21, Building object ID = 26
         */
        if (cityObject.getObjectClassId().intValue() == 21) {
        	
          String cityfurnitureIri = cityObjectIri.replace(CITY_OBJECT, CITY_FURNITURE);
          OntoChemPlantModel ocp_model = context.createHollowModel(OntoChemPlantModel.class, cityfurnitureIri);
          context.pullAll(ocp_model);

          PlantItem plantitem = context.createHollowModel(PlantItem.class, ocp_model.getOntoCityGMLRepresentationOf().toString());
          context.recursivePullAll(plantitem, 1);

          ArrayList<PlantItem> PlantItemList = new ArrayList<>();
          PlantItemList.add(plantitem);
          result.put(PlantItemList);
          final_results.put("chemplant", PlantItemList);

        } else {
        	
          String buildingIri = cityObjectIri.replace(CITY_OBJECT, BUILDING);
          OntoChemPlantModel ocp_model = context.createHollowModel(OntoChemPlantModel.class, buildingIri);
          context.pullAll(ocp_model);

          Building building = context.createHollowModel(Building.class, ocp_model.getOntoCityGMLRepresentationOf().toString());
          context.recursivePullAll(building, 3);
          
          ArrayList<Building> BuildingList = new ArrayList<>();
          BuildingList.add(building);
          result.put(BuildingList);
          final_results.put("chemplant", BuildingList);

        }
		return final_results;
	}
}
