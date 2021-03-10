package org.cam.ceb.como.nist.webbook.info;
/**
 * A model created to encode different pressures of species.
 * 
 * @author msff2
 *
 */
public class NISTPressure {
	private String value;
	private String units;
	private String method;
	private String reference;
	private String comment;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
