//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}id"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}dictRef"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}size"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}min"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}convention"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}units"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}builtin"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}title"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}max"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}unitsRef"/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "integerArray")
public class IntegerArray {

    @XmlValue
    protected java.lang.String value;
    @XmlAttribute(name = "id")
    protected java.lang.String id;
    @XmlAttribute(name = "dictRef")
    protected java.lang.String dictRef;
    @XmlAttribute(name = "size")
    protected BigInteger size;
    @XmlAttribute(name = "min")
    protected java.lang.String min;
    @XmlAttribute(name = "convention")
    protected java.lang.String convention;
    @XmlAttribute(name = "units")
    protected java.lang.String units;
    @XmlAttribute(name = "builtin")
    protected java.lang.String builtin;
    @XmlAttribute(name = "title")
    protected java.lang.String title;
    @XmlAttribute(name = "max")
    protected java.lang.String max;
    @XmlAttribute(name = "unitsRef")
    protected java.lang.String unitsRef;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the dictRef property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getDictRef() {
        return dictRef;
    }

    /**
     * Sets the value of the dictRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setDictRef(java.lang.String value) {
        this.dictRef = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSize(BigInteger value) {
        this.size = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setMin(java.lang.String value) {
        this.min = value;
    }

    /**
     * Gets the value of the convention property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getConvention() {
        return convention;
    }

    /**
     * Sets the value of the convention property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setConvention(java.lang.String value) {
        this.convention = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setUnits(java.lang.String value) {
        this.units = value;
    }

    /**
     * Gets the value of the builtin property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getBuiltin() {
        return builtin;
    }

    /**
     * Sets the value of the builtin property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setBuiltin(java.lang.String value) {
        this.builtin = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTitle(java.lang.String value) {
        this.title = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setMax(java.lang.String value) {
        this.max = value;
    }

    /**
     * Gets the value of the unitsRef property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getUnitsRef() {
        return unitsRef;
    }

    /**
     * Sets the value of the unitsRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setUnitsRef(java.lang.String value) {
        this.unitsRef = value;
    }

}
