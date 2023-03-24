package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element;

import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.JunitTestUtils;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element.buildingstructure.Column;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElementTest {
    private static final String testBaseUri1 = "http://www.example.org/";
    private static final String testIri1 = testBaseUri1 + "Element_1242";
    private static final String testName1 = "Table";
    private static final String testUID1 = "vas81j1918";
    private static final String testPlacementIri1 = testBaseUri1 + "LocalPlacement_515";
    private static final String elementClassName = "Element";
    private static final String elementClass = "https://w3id.org/bot#" + elementClassName;
    private static final String testBaseUri2 = "http://www.example.org/test#";
    private static final String testIri2 = testBaseUri2 + "Fridge_122";
    private static final String testName2 = "Lab Fridge";
    private static final String testUID2 = "as18rfhavinv134";
    private static final String testPlacementIri2 = testBaseUri2 + "LocalPlacement_8172";
    private static final String fridgeClassName = "Fridge";
    private static final String fridgeClass = "https://www.theworldavatar.com/kg/ontodevice/" + fridgeClassName;
    private static final String testHostZoneIRI = testBaseUri1 + "Room_7152";
    private static final String testGeomModelIRI = testBaseUri1 + "ModelRepresentation3D_715";
    private static final String testClassName = "IfcModelRepresentation";
    private static final String testClass = JunitTestUtils.bimUri + testClassName;

    @Test
    void testConstructor() {
        Element sample = new Element(testIri1, testName1, testUID1, testPlacementIri1, testHostZoneIRI, testGeomModelIRI);
        // Test that the sample fields are correct
        assertEquals(testBaseUri1, sample.getPrefix());
        assertNotEquals(testIri1, sample.getIfcRepIri());
        assertTrue(sample.getIfcRepIri().contains(testBaseUri1 + testClassName + "_"));
        assertEquals(testName1, sample.getName());
        assertEquals(testUID1, sample.getUid());
        assertEquals(testPlacementIri1, sample.getPlacementIri());

        Element sample2 = new Element(testIri2, testName2, testUID2, testPlacementIri2, testHostZoneIRI, testGeomModelIRI);
        // Test that the sample fields are correct
        assertEquals(testBaseUri2, sample2.getPrefix());
        assertNotEquals(testIri2, sample2.getIfcRepIri());
        assertTrue(sample2.getIfcRepIri().contains(testBaseUri2 + testClassName + "_"));
        assertEquals(testName2, sample2.getName());
        assertEquals(testUID2, sample2.getUid());
        assertEquals(testPlacementIri2, sample2.getPlacementIri());
    }

    @Test
    void testConstructStatementsForGenericElement() {
        // Set up
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        Element sample = new Element(testIri1, testName1, testUID1, testPlacementIri1, testHostZoneIRI, testGeomModelIRI);
        // Execute method
        sample.constructStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedCommonStatements(elementClassName, elementClass, testName1), result);
    }

    @Test
    void testConstructStatementsForCreatedElement() {
        // Set up
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        Element sample = new Element(testIri1, testName2, testUID1, testPlacementIri1, testHostZoneIRI, testGeomModelIRI);
        // Execute method
        sample.constructStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedCommonStatements(fridgeClassName, fridgeClass, testName2), result);
    }

    private List<String> genExpectedCommonStatements(String className, String classType, String elementName) {
        List<String> expected = new ArrayList<>();
        expected.add(testHostZoneIRI + ", https://w3id.org/bot#containsElement, " + testBaseUri1 + className + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        expected.add(testBaseUri1 + className + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " + classType);
        expected.add(testBaseUri1 + className + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasIfcRepresentation, " + testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " + testClass);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/2000/01/rdf-schema#label, \"" + elementName);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasIfcId, \"" + testUID1);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasLocalPosition, " + testPlacementIri1);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasGeometricRepresentation, " + testGeomModelIRI);
        return expected;
    }
}