package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone;

import org.apache.jena.rdf.model.Statement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ttlparser.StringUtils;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * A class representing the IfcStoreyRepresentation concept in OntoBIM.
 *
 * @author qhouyee
 */
public class IfcStoreyRepresentation extends IfcAbstractRepresentation {
    private final String buildingIRI;
    private final String botStoreyIRI;
    private final Double refElevation;

    /**
     * Standard Constructor initialising the necessary and optional inputs.
     *
     * @param iri          The instance IRI to be created.
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param buildingIri  The IRI of bot:Building that is linked to this building instance.
     * @param refElevation An optional field containing the reference elevation values stored in IFC.
     */
    public IfcStoreyRepresentation(String iri, String name, String uid, String buildingIri, String refElevation) {
        // Initialise the super class
        super(iri, OntoBimConstant.STOREY_REP_CLASS,  name, uid);
        this.buildingIRI = buildingIri;
        // Generate a new bot Storey IRI
        this.botStoreyIRI = this.getPrefix() + OntoBimConstant.STOREY_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        if (refElevation != null) {
            // Remove the ` .` in elevation generated by IfcOwl (if any) to ensure double conversion is successful
            refElevation = refElevation.contains(" .") ? StringUtils.getStringBeforeLastCharacterOccurrence(refElevation, ".") : refElevation;
            this.refElevation = Double.valueOf(refElevation);
        } else {
            this.refElevation = null;
        }
    }

    public String getBotStoreyIRI() {return botStoreyIRI;}
    protected Double getRefElevation() {return this.refElevation;}

    /**
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    @Override
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        super.addIfcAbstractRepresentationStatements(statementSet, OntoBimConstant.BIM_STOREY_REP_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotStoreyIRI(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BOT_STOREY_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotStoreyIRI(), OntoBimConstant.BIM_HAS_IFC_REPRESENTATION, this.getIri());
        StatementHandler.addStatement(statementSet, this.buildingIRI, OntoBimConstant.BOT_HAS_STOREY, this.getBotStoreyIRI());

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
            StatementHandler.addStatementWithDoubleLiteral(statementSet, measureInst, OntoBimConstant.OM_HAS_NUMERICAL_VALUE, this.refElevation);
            StatementHandler.addStatement(statementSet, measureInst, OntoBimConstant.OM_HAS_UNIT, lengthInst);
            StatementHandler.addStatement(statementSet, lengthInst, OntoBimConstant.RDF_TYPE, OntoBimConstant.LENGTH_CLASS);
            StatementHandler.addStatement(statementSet, lengthInst, OntoBimConstant.SKOS_NOTATION, METRE_UNIT, false);
        }
    }
}
