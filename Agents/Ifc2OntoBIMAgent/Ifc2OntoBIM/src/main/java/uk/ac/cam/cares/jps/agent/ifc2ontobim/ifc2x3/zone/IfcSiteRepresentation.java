package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone;

import org.apache.jena.rdf.model.*;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.UUID;


/**
 * A class representing the IfcSiteRepresentation concept in OntoBIM.
 *
 * @author qhouyee
 */
public class IfcSiteRepresentation extends IfcAbstractRepresentation {
    private String botSiteIRI;
    private Double latitudeDegree;
    private Double latitudeMinute;
    private Double latitudeSecond;
    private Double latitudeMilSec;
    private Double longitudeDegree;
    private Double longitudeMinute;
    private Double longitudeSecond;
    private Double longitudeMilSec;
    private final String projectIRI;
    private final Double refElevation;

    /**
     * Standard Constructor initialising the necessary and optional inputs.
     *
     * @param iri          The instance IRI to be created.
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param placementIri The local placement IRI for the zone's position.
     * @param projectIri   The project's IFC representation IRI if this site is the root zone.
     * @param latitude     A queue of the latitude's degree, minute, second, and millionth-second values.
     * @param longitude    A queue of the longitude's degree, minute, second, and millionth-second values.
     * @param refElevation An optional field containing the reference elevation values stored in IFC.
     */
    public IfcSiteRepresentation(String name, String uid, String placementIri, String projectIri, Queue<String> latitude, Queue<String> longitude, String refElevation) {
        // Initialise the super class
        super(OntoBimConstant.SITE_REP_CLASS, name, uid, placementIri);
        // Generate new site IRIs
        this.botSiteIRI = this.getPrefix() + OntoBimConstant.SITE_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        // Parse the latitude and longitude
        parseLatLong(latitude, longitude);
        // Parse the optional values
        this.projectIRI = projectIri; // If the argument is null, the field will still be null
        if (refElevation != null) {
            // Remove the ` .` in elevation generated by IfcOwl (if any) to ensure double conversion is successful
            refElevation = refElevation.contains(" .") ? StringUtils.getStringBeforeLastCharacterOccurrence(refElevation, ".") : refElevation;
            this.refElevation = Double.valueOf(refElevation);
        } else {
            this.refElevation = null;
        }
    }

    public String getBotSiteIRI() {return botSiteIRI;}
    protected Double getRefElevation() {return this.refElevation;}

    /**
     * Parses the latitude and longitude values for validation and attaching their values.
     *
     * @param latitude  A queue of the latitude's degree, minute, second, and millionth-second values.
     * @param longitude A queue of the longitude's degree, minute, second, and millionth-second values.
     */
    private void parseLatLong(Queue<String> latitude, Queue<String> longitude) {
        if (latitude != null && longitude != null) {
            if (latitude.size() != 4) {
                throw new IllegalArgumentException("Invalid latitude input. There should be four values!");
            } else if (longitude.size() != 4) {
                throw new IllegalArgumentException("Invalid longitude input. There should be four values!");
            } else {
                latitudeDegree = Double.valueOf(latitude.poll());
                latitudeMinute = Double.valueOf(latitude.poll());
                latitudeSecond = Double.valueOf(latitude.poll());
                latitudeMilSec = Double.valueOf(latitude.poll());
                longitudeDegree = Double.valueOf(longitude.poll());
                longitudeMinute = Double.valueOf(longitude.poll());
                longitudeSecond = Double.valueOf(longitude.poll());
                longitudeMilSec = Double.valueOf(longitude.poll());
            }
        } else {
            latitudeDegree = null;
            latitudeMinute = null;
            latitudeSecond = null;
            latitudeMilSec = null;
            longitudeDegree = null;
            longitudeMinute = null;
            longitudeSecond = null;
            longitudeMilSec = null;
        }
    }

    /**
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    @Override
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        super.addIfcAbstractRepresentationStatements(statementSet, OntoBimConstant.BIM_SITE_REP_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotSiteIRI(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BOT_SITE_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotSiteIRI(), OntoBimConstant.BIM_HAS_IFC_REPRESENTATION, this.getIri());
        if (this.projectIRI != null) {
            StatementHandler.addStatement(statementSet, this.projectIRI, OntoBimConstant.BIM_HAS_ROOT_ZONE, this.getIri());
        }
        if (this.latitudeMilSec != null && this.longitudeMilSec != null) {
            // Generate the instances
            String latInst = this.getPrefix() + "Latitude_" + UUID.randomUUID();
            String longInst = this.getPrefix() + "Longitude_" + UUID.randomUUID();
            // Add the statements we are interested in
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_LAT, latInst);
            StatementHandler.addStatement(statementSet, latInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_COMPOUND_PLANE_ANGLE);
            StatementHandler.addStatement(statementSet, latInst, OntoBimConstant.BIM_HAS_DEGREE, this.latitudeDegree);
            StatementHandler.addStatement(statementSet, latInst, OntoBimConstant.BIM_HAS_MINUTE, this.latitudeMinute);
            StatementHandler.addStatement(statementSet, latInst, OntoBimConstant.BIM_HAS_SEC, this.latitudeSecond);
            StatementHandler.addStatement(statementSet, latInst, OntoBimConstant.BIM_HAS_MILSEC, this.latitudeMilSec);
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_LONG, longInst);
            StatementHandler.addStatement(statementSet, longInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_COMPOUND_PLANE_ANGLE);
            StatementHandler.addStatement(statementSet, longInst, OntoBimConstant.BIM_HAS_DEGREE, this.longitudeDegree);
            StatementHandler.addStatement(statementSet, longInst, OntoBimConstant.BIM_HAS_MINUTE, this.longitudeMinute);
            StatementHandler.addStatement(statementSet, longInst, OntoBimConstant.BIM_HAS_SEC, this.longitudeSecond);
            StatementHandler.addStatement(statementSet, longInst, OntoBimConstant.BIM_HAS_MILSEC, this.longitudeMilSec);
        }
        if (this.refElevation != null) {
            // Generate the instances
            String heightInst = this.getPrefix() + "Height_" + UUID.randomUUID();
            String measureInst = this.getPrefix() + "Measure_" + UUID.randomUUID();
            String lengthInst = this.getPrefix() + "Length_" + UUID.randomUUID();
            // Add the statements we are interested in
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_REF_ELEVATION, heightInst);
            StatementHandler.addStatement(statementSet, heightInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.HEIGHT_CLASS);
            StatementHandler.addStatement(statementSet, heightInst, OntoBimConstant.OM_HAS_VALUE, measureInst);
            StatementHandler.addStatement(statementSet, measureInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.MEASURE_CLASS);
            StatementHandler.addStatement(statementSet, measureInst, OntoBimConstant.OM_HAS_NUMERICAL_VALUE, this.refElevation);
            StatementHandler.addStatement(statementSet, measureInst, OntoBimConstant.OM_HAS_UNIT, lengthInst);
            StatementHandler.addStatement(statementSet, lengthInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.LENGTH_CLASS);
            StatementHandler.addStatement(statementSet, lengthInst, OntoBimConstant.SKOS_NOTATION, METRE_UNIT, false);
        }
    }
}