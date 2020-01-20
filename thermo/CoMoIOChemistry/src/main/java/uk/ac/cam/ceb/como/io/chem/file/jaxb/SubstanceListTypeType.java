//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for substanceListTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="substanceListTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="solution"/>
 *     &lt;enumeration value="mixture"/>
 *     &lt;enumeration value="other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "substanceListTypeType")
@XmlEnum
public enum SubstanceListTypeType {

    @XmlEnumValue("solution")
    SOLUTION("solution"),
    @XmlEnumValue("mixture")
    MIXTURE("mixture"),
    @XmlEnumValue("other")
    OTHER("other");
    private final java.lang.String value;

    SubstanceListTypeType(java.lang.String v) {
        value = v;
    }

    public java.lang.String value() {
        return value;
    }

    public static SubstanceListTypeType fromValue(java.lang.String v) {
        for (SubstanceListTypeType c: SubstanceListTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
