package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone;

import org.apache.jena.rdf.model.Statement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * A class representing the IfcBuildingRepresentation concept in OntoBIM.
 *
 * @author qhouyee
 */
public class IfcBuildingRepresentation extends IfcAbstractRepresentation {
    private String siteIRI;
    private String botBuildingIRI;
    private final String projectIRI;
    private final Double refElevation;
    private final Double terrainElevation;

    /**
     * Standard Constructor initialising the necessary and optional inputs.
     *
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param placementIri The local placement IRI for the zone's position.
     * @param projectIri   The project's IFC representation IRI if this building instance is the root zone.
     * @param siteIri      The IRI of bot:Site that is linked to this building instance.
     * @param refElevation An optional field containing the reference elevation values stored in IFC.
     * @param terElevation An optional field containing the terrain elevation values stored in IFC.
     */
    public IfcBuildingRepresentation(String name, String uid, String placementIri, String projectIri, String siteIri, String refElevation, String terElevation) {
        // Initialise the super class
        super(OntoBimConstant.BUILDING_REP_CLASS,  name, uid, placementIri);
        this.siteIRI = siteIri;
        // Generate a new bot Building IRI
        this.botBuildingIRI = this.getPrefix() + OntoBimConstant.BUILDING_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        this.projectIRI = projectIri; // If the argument is null, the field will still be null
        if (refElevation != null) {
            // Remove the ` .` in elevation generated by IfcOwl (if any) to ensure double conversion is successful
            refElevation = refElevation.contains(" .") ? StringUtils.getStringBeforeLastCharacterOccurrence(refElevation, ".") : refElevation;
            this.refElevation = Double.valueOf(refElevation);
        } else {
            this.refElevation = null;
        }

        if (terElevation != null) {
            terElevation = terElevation.contains(" .") ? StringUtils.getStringBeforeLastCharacterOccurrence(terElevation, ".") : terElevation;
            this.terrainElevation = Double.valueOf(terElevation);
        } else {
            this.terrainElevation = null;
        }
    }

    public String getBotBuildingIRI() {return botBuildingIRI;}
    protected Double getRefElevation() {return this.refElevation;}
    protected Double getTerElevation() {return this.terrainElevation;}

    /**
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    @Override
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        super.addIfcAbstractRepresentationStatements(statementSet, OntoBimConstant.BIM_BUILDING_REP_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotBuildingIRI(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BOT_BUILDING_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotBuildingIRI(), OntoBimConstant.BIM_HAS_IFC_REPRESENTATION, this.getIri());
        StatementHandler.addStatement(statementSet, this.siteIRI, OntoBimConstant.BOT_HAS_BUILDING, this.getBotBuildingIRI());

        if (this.projectIRI != null) {
            StatementHandler.addStatement(statementSet, this.projectIRI, OntoBimConstant.BIM_HAS_ROOT_ZONE, this.getIri());
        }
        // Add the statements for Reference Elevation if it exists
        if (this.refElevation != null) {
            String refElevHeightInst = this.getPrefix() + "Height_" + UUID.randomUUID();
            String refElevMeasureInst = this.getPrefix() + "Measure_" + UUID.randomUUID();
            String refElevLengthInst = this.getPrefix() + "Length_" + UUID.randomUUID();
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_REF_ELEVATION, refElevHeightInst);
            StatementHandler.addStatement(statementSet, refElevHeightInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.HEIGHT_CLASS);
            StatementHandler.addStatement(statementSet, refElevHeightInst, OntoBimConstant.OM_HAS_VALUE, refElevMeasureInst);
            StatementHandler.addStatement(statementSet, refElevMeasureInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.MEASURE_CLASS);
            StatementHandler.addStatement(statementSet, refElevMeasureInst, OntoBimConstant.OM_HAS_NUMERICAL_VALUE, this.refElevation);
            StatementHandler.addStatement(statementSet, refElevMeasureInst, OntoBimConstant.OM_HAS_UNIT, refElevLengthInst);
            StatementHandler.addStatement(statementSet, refElevLengthInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.LENGTH_CLASS);
            StatementHandler.addStatement(statementSet, refElevLengthInst, OntoBimConstant.SKOS_NOTATION, METRE_UNIT, false);
        }
        // Add the statements for Terrain Elevation if it exists
        if (this.terrainElevation != null) {
            String terElevHeightInst = this.getPrefix() + "Height_" + UUID.randomUUID();
            String terElevMeasureInst = this.getPrefix() + "Measure_" + UUID.randomUUID();
            String terElevLengthInst = this.getPrefix() + "Length_" + UUID.randomUUID();
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_TER_ELEVATION, terElevHeightInst);
            StatementHandler.addStatement(statementSet, terElevHeightInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.HEIGHT_CLASS);
            StatementHandler.addStatement(statementSet, terElevHeightInst, OntoBimConstant.OM_HAS_VALUE, terElevMeasureInst);
            StatementHandler.addStatement(statementSet, terElevMeasureInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.MEASURE_CLASS);
            StatementHandler.addStatement(statementSet, terElevMeasureInst, OntoBimConstant.OM_HAS_NUMERICAL_VALUE, this.terrainElevation);
            StatementHandler.addStatement(statementSet, terElevMeasureInst, OntoBimConstant.OM_HAS_UNIT, terElevLengthInst);
            StatementHandler.addStatement(statementSet, terElevLengthInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.LENGTH_CLASS);
            StatementHandler.addStatement(statementSet, terElevLengthInst, OntoBimConstant.SKOS_NOTATION, METRE_UNIT, false);
        }
    }
}
