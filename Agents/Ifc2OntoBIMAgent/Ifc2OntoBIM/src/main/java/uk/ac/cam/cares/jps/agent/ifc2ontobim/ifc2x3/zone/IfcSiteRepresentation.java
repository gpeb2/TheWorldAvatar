package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone;

import org.apache.jena.rdf.model.*;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.QueryHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ttlparser.StringUtils;

import java.util.LinkedHashSet;
import java.util.UUID;


/**
 * A class representing the IfcSiteRepresentation concept in OntoBIM.
 *
 * @author qhouyee
 */
public class IfcSiteRepresentation extends IfcAbstractRepresentation {
    private String botSiteIRI;
    private final Double refElevation;

    /**
     * Standard Constructor initialising the necessary and optional inputs.
     *
     * @param iri          The instance IRI to be created.
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param refElevation An optional field containing the reference elevation values stored in IFC.
     */
    public IfcSiteRepresentation(String iri, String name, String uid, String refElevation) {
        // Initialise the super class
        super(iri, OntoBimConstant.SITE_REP_CLASS,  name, uid);
        // Generate new site IRIs
        this.botSiteIRI = this.getPrefix() + OntoBimConstant.SITE_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
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
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    @Override
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        super.addIfcAbstractRepresentationStatements(statementSet, OntoBimConstant.BIM_SITE_REP_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotSiteIRI(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BOT_SITE_CLASS);
        StatementHandler.addStatement(statementSet, this.getBotSiteIRI(), OntoBimConstant.BIM_HAS_IFC_REPRESENTATION, this.getIri());

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