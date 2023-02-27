package uk.ac.cam.cares.jps.agent.ifc2ontobim.jenaquerybuilder.base;

import com.hp.hpl.jena.shared.uuid.JenaUUID;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.NamespaceMapper;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.QueryHandler;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.util.UUID;

/**
 * Provides query statements specific to the spatial zones such as Site, Building, Building Storey, and Spaces.
 *
 * @author qhouyee
 */
public class IfcSpatialZonesConstructBuilder extends IfcConstructBuilderTemplate {
    public static final String LAT_ANGLE_VAR = "?latcompoundangle";
    public static final String LAT_DEGREE_VAR = "?latdegree";
    public static final String LAT_MIN_VAR = "?latminute";
    public static final String LAT_SEC_VAR = "?latsecond";
    public static final String LAT_MILSEC_VAR = "?latmilsecond";
    public static final String LONG_ANGLE_VAR = "?longcompoundangle";
    public static final String LONG_DEGREE_VAR = "?longdegree";
    public static final String LONG_MIN_VAR = "?longminute";
    public static final String LONG_SEC_VAR = "?longsecond";
    public static final String LONG_MILSEC_VAR = "?longmilsecond";
    public static final String IFC_SITE_REPRESENTATION_CLASS = "IfcSiteRepresentation";

    /**
     * Create the SPARQL query syntax for Construct queries.
     *
     * @param builder  Construct Builder object to add Construct query statements.
     * @param ifcClass The IfcOwl ontology's class name for the spatial structure element.
     * @param bimClass The OntoBIM ontology's class name for the spatial structure element.
     * @return The SPARQL query string for spatial zones
     */
    public String createSparqlQuery(ConstructBuilder builder, String ifcClass, String bimClass) {
        // Calls methods specific to each input to generate additional SPARQL query statements
        this.switchFunctionDependingOnInput(builder, ifcClass, bimClass);
        return builder.buildString();
    }

    /**
     * A utility method to call other functions that generate additional SPARQL query statements based on the inputs.
     */
    @Override
    protected void switchFunctionDependingOnInput(ConstructBuilder builder, String ifcClass, String bimClass) {
        switch (ifcClass) {
            case "ifc:IfcSite":
                addReferenceSystemParamQueryComponents(builder);
                break;
        }
    }

    /**
     * Add the statements for querying the reference system parameters like Latitude, longitude, elevation into the builder.
     *
     * @param builder Construct Builder object to add Construct query statements.
     */
    private void addReferenceSystemParamQueryComponents(ConstructBuilder builder) {
        builder.addConstruct(ELEMENT_VAR, BIM_PREFIX + "hasRefLatitude", LAT_ANGLE_VAR)
                .addConstruct(LAT_ANGLE_VAR, QueryHandler.RDF_TYPE, "bim:CompoundPlaneAngle")
                .addConstruct(LAT_ANGLE_VAR, BIM_PREFIX + "hasDegree", LAT_DEGREE_VAR)
                .addConstruct(LAT_ANGLE_VAR, BIM_PREFIX + "hasMinute", LAT_MIN_VAR)
                .addConstruct(LAT_ANGLE_VAR, BIM_PREFIX + "hasSecond", LAT_SEC_VAR)
                .addConstruct(LAT_ANGLE_VAR, BIM_PREFIX + "hasMillionthSecond", LAT_MILSEC_VAR)
                .addConstruct(ELEMENT_VAR, BIM_PREFIX + "hasRefLongitude", LONG_ANGLE_VAR)
                .addConstruct(LONG_ANGLE_VAR, QueryHandler.RDF_TYPE, "bim:CompoundPlaneAngle")
                .addConstruct(LONG_ANGLE_VAR, BIM_PREFIX + "hasDegree", LONG_DEGREE_VAR)
                .addConstruct(LONG_ANGLE_VAR, BIM_PREFIX + "hasMinute", LONG_MIN_VAR)
                .addConstruct(LONG_ANGLE_VAR, BIM_PREFIX + "hasSecond", LONG_SEC_VAR)
                .addConstruct(LONG_ANGLE_VAR, BIM_PREFIX + "hasMillionthSecond", LONG_MILSEC_VAR);

        builder.addWhere(ELEMENT_VAR, "ifc:refLatitude_IfcSite", LAT_ANGLE_VAR)
                .addWhere(LAT_ANGLE_VAR, QueryHandler.RDF_TYPE, "ifc:IfcCompoundPlaneAngleMeasure")
                .addWhere(LAT_ANGLE_VAR, "list:hasContents/express:hasInteger", LAT_DEGREE_VAR)
                .addWhere(LAT_ANGLE_VAR, "list:hasNext/list:hasContents/express:hasInteger", LAT_MIN_VAR)
                .addWhere(LAT_ANGLE_VAR, "list:hasNext/list:hasNext/list:hasContents/express:hasInteger", LAT_SEC_VAR)
                .addOptional(LAT_ANGLE_VAR, "list:hasNext/list:hasNext/list:hasNext/list:hasContents/express:hasInteger", LAT_MILSEC_VAR)
                .addWhere(ELEMENT_VAR, "ifc:refLongitude_IfcSite", LONG_ANGLE_VAR)
                .addWhere(LONG_ANGLE_VAR, QueryHandler.RDF_TYPE, "ifc:IfcCompoundPlaneAngleMeasure")
                .addWhere(LONG_ANGLE_VAR, "list:hasContents/express:hasInteger", LONG_DEGREE_VAR)
                .addWhere(LONG_ANGLE_VAR, "list:hasNext/list:hasContents/express:hasInteger", LONG_MIN_VAR)
                .addWhere(LONG_ANGLE_VAR, "list:hasNext/list:hasNext/list:hasContents/express:hasInteger", LONG_SEC_VAR)
                .addOptional(LONG_ANGLE_VAR, "list:hasNext/list:hasNext/list:hasNext/list:hasContents/express:hasInteger", LONG_MILSEC_VAR);
    }
}
