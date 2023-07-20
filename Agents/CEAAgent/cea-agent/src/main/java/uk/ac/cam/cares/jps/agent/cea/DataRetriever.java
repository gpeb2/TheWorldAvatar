package uk.ac.cam.cares.jps.agent.cea;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.json.JSONArray;

import java.util.ArrayList;

import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;

public class DataRetriever {
    private OntologyURIHelper ontologyUriHelper;
    public DataRetriever(OntologyURIHelper uriHelper) {
        this.ontologyUriHelper = uriHelper;
    }

    /**
     * Add where for Building Consumption
     * @param builder update builder
     * @param type energy type in ontology
     */
    public void addBuildingConsumptionWhere(WhereBuilder builder, String type){
        builder.addWhere("?building", "ontoubemmp:consumesEnergy", "?grid")
                .addWhere("?grid", "rdf:type", type)
                .addWhere("?grid", "om:hasValue", "?measure")
                .addWhere("?measure", "om:hasUnit", "?unit");
    }

    /**
     * Add where for Device Supply
     * @param builder update builder
     * @param generatorType type of generator
     * @param energyType type of energy supply
     * @param facadeType  type of facade that the generator is theoretically installed on
     */
    public void addSupplyDeviceWhere(WhereBuilder builder, String generatorType, String energyType, String facadeType) {
        builder.addWhere("?building", "obs:hasFacade", "?facade")
                .addWhere("?facade", "rdf:type", facadeType)
                .addWhere("?facade", "ontoubemmp:hasTheoreticalEnergyProduction", "?SolarGenerators")
                .addWhere("?SolarGenerators", "rdf:type", generatorType)
                .addWhere("?SolarGenerators", "ontoubemmp:producesEnergy", "?supply")
                .addWhere("?supply", "rdf:type", energyType)
                .addWhere("?supply", "om:hasValue", "?measure")
                .addWhere("?measure", "om:hasUnit", "?unit");
    }

    /**
     * Add where for Device Area
     * @param builder update builder
     * @param building iri of building
     * @param facadeType type of facade
     */
    public void addSupplyDeviceAreaWhere(WhereBuilder builder, String building, String facadeType) {
        builder.addWhere(NodeFactory.createURI(building), "obs:hasFacade" , "?facade")
                .addWhere("?facade", "rdf:type", facadeType)
                .addWhere("?facade", "ontoubemmp:hasSolarSuitableArea", "?area")
                .addWhere("?area", "om:hasValue", "?measure")
                .addWhere("?measure", "om:hasNumericalValue", "?value")
                .addWhere("?measure", "om:hasUnit", "?unit");
    }

    /**
     * Retrieves iris from KG for the data type requested
     * @param building uri of building in energyprofile graph
     * @param value type of data from CEAConstants.TIME_SERIES or CEAConstants.SCALARS
     * @param route route to pass to access agent
     * @return list of iris
     */
    public ArrayList<String> getDataIRI(String building, String value, String route) {
        ArrayList<String> result = new ArrayList<>();

        SelectBuilder sb = new SelectBuilder();
        WhereBuilder wb = new WhereBuilder();

        if(building.equals("")) {
            return result;
        }

        wb.addPrefix("ocgml", ontologyUriHelper.getOntologyUri(OntologyURIHelper.ocgml))
                .addPrefix("rdf", ontologyUriHelper.getOntologyUri(OntologyURIHelper.rdf))
                .addPrefix("om", ontologyUriHelper.getOntologyUri(OntologyURIHelper.unitOntology))
                .addPrefix("ontoubemmp", ontologyUriHelper.getOntologyUri(OntologyURIHelper.ontoUBEMMP))
                .addPrefix("obs", ontologyUriHelper.getOntologyUri(OntologyURIHelper.ontobuiltstructure));

        switch(value) {
            case CEAConstants.KEY_ROOF_SOLAR_SUITABLE_AREA:
                addSupplyDeviceAreaWhere(wb, building, "obs:RoofFacade");
                break;
            case CEAConstants.KEY_SOUTH_WALL_SOLAR_SUITABLE_AREA:
                addSupplyDeviceAreaWhere(wb, building, "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_NORTH_WALL_SOLAR_SUITABLE_AREA:
                addSupplyDeviceAreaWhere(wb, building, "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_EAST_WALL_SOLAR_SUITABLE_AREA:
                addSupplyDeviceAreaWhere(wb, building, "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_WEST_WALL_SOLAR_SUITABLE_AREA:
                addSupplyDeviceAreaWhere(wb, building, "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_GRID_CONSUMPTION:
                addBuildingConsumptionWhere(wb,"ontoubemmp:GridConsumption");
                break;
            case CEAConstants.KEY_ELECTRICITY_CONSUMPTION:
                addBuildingConsumptionWhere(wb,"ontoubemmp:ElectricityConsumption");
                break;
            case CEAConstants.KEY_HEATING_CONSUMPTION:
                addBuildingConsumptionWhere(wb,"ontoubemmp:HeatingConsumption");
                break;
            case CEAConstants.KEY_COOLING_CONSUMPTION:
                addBuildingConsumptionWhere(wb,"ontoubemmp:CoolingConsumption");
                break;
            case CEAConstants.KEY_PV_ROOF_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVPanel", "ontoubemmp:ElectricitySupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_PV_WALL_SOUTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVPanel", "ontoubemmp:ElectricitySupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_PV_WALL_NORTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVPanel", "ontoubemmp:ElectricitySupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_PV_WALL_EAST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVPanel", "ontoubemmp:ElectricitySupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_PV_WALL_WEST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVPanel", "ontoubemmp:ElectricitySupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_ROOF_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:ElectricitySupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_SOUTH_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:ElectricitySupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_NORTH_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:ElectricitySupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_EAST_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:ElectricitySupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_WEST_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:ElectricitySupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_ROOF_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:HeatSupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_SOUTH_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:HeatSupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_NORTH_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:HeatSupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_EAST_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:HeatSupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_PVT_PLATE_WALL_WEST_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTPlateCollector", "ontoubemmp:HeatSupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_ROOF_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:ElectricitySupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_SOUTH_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:ElectricitySupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_NORTH_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:ElectricitySupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_EAST_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:ElectricitySupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_WEST_E_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:ElectricitySupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_ROOF_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:HeatSupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_SOUTH_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:HeatSupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_NORTH_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:HeatSupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_EAST_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:HeatSupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_PVT_TUBE_WALL_WEST_Q_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:PVTTubeCollector", "ontoubemmp:HeatSupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_PLATE_ROOF_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalPlateCollector", "ontoubemmp:HeatSupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_THERMAL_PLATE_WALL_SOUTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalPlateCollector", "ontoubemmp:HeatSupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_PLATE_WALL_NORTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalPlateCollector", "ontoubemmp:HeatSupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_PLATE_WALL_EAST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalPlateCollector", "ontoubemmp:HeatSupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_PLATE_WALL_WEST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalPlateCollector", "ontoubemmp:HeatSupply", "obs:WestWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_TUBE_ROOF_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalTubeCollector", "ontoubemmp:HeatSupply", "obs:RoofFacade");
                break;
            case CEAConstants.KEY_THERMAL_TUBE_WALL_SOUTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalTubeCollector", "ontoubemmp:HeatSupply", "obs:SouthWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_TUBE_WALL_NORTH_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalTubeCollector", "ontoubemmp:HeatSupply", "obs:NorthWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_TUBE_WALL_EAST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalTubeCollector", "ontoubemmp:HeatSupply", "obs:EastWallFacade");
                break;
            case CEAConstants.KEY_THERMAL_TUBE_WALL_WEST_SUPPLY:
                addSupplyDeviceWhere(wb,"ontoubemmp:ThermalTubeCollector", "ontoubemmp:HeatSupply", "obs:WestWallFacade");
                break;
            default:
                return result;
        }

        sb.addVar("?measure")
                .addVar("?unit");

        sb.addWhere(wb);

        sb.setVar( Var.alloc( "building" ), NodeFactory.createURI(building));

        JSONArray queryResultArray = new JSONArray(AccessAgentCaller.queryStore(route, sb.build().toString()));

        if(!queryResultArray.isEmpty()){
            result.add(queryResultArray.getJSONObject(0).get("measure").toString());
            result.add(queryResultArray.getJSONObject(0).get("unit").toString());
        }
        return result;
    }

    /**
     * Gets numerical value of specified measurement
     * @param measureUri Uri of the measurement with numerical value in KG
     * @param route route to pass to access agent
     * @param graph graph name
     * @return list of iris
     */
    public String getNumericalValue(String measureUri, String route, String graph){
        String result = "";

        WhereBuilder wb = new WhereBuilder().addPrefix("om", ontologyUriHelper.getOntologyUri(OntologyURIHelper.unitOntology))
                .addWhere("?measure", "om:hasNumericalValue", "?value");

        SelectBuilder sb = new SelectBuilder().addVar("?value");

        if (!graph.isEmpty()){
            sb.addGraph(NodeFactory.createURI(graph), wb);
        }
        else{
            sb.addWhere(wb);
        }

        sb.setVar( Var.alloc( "measure" ), NodeFactory.createURI(measureUri));

        JSONArray queryResultArray = new JSONArray(AccessAgentCaller.queryStore(route, sb.build().toString()));

        if(!queryResultArray.isEmpty()){
            result = queryResultArray.getJSONObject(0).get("value").toString();
        }
        return result;
    }

    /**
     * Returns readable unit from ontology iri
     * @param ontologyUnit unit iri in ontology
     * @return unit as a String
     */
    public String getUnit(String ontologyUnit) {
        switch(ontologyUnit) {
            case("http://www.ontology-of-units-of-measure.org/resource/om-2/kilowattHour"):
                return "kWh";
            case("http://www.ontology-of-units-of-measure.org/resource/om-2/squareMetre"):
                return "m^2";
            default:
                return "";
        }
    }
}
