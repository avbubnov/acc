package iac.grn.ramodule.entity;

import java.io.Serializable;

public class VAuditReportPK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long sessionId;
	
	private Long entryid;
	
	public VAuditReportPK() {
		super();
	}
	public VAuditReportPK(Long sessionId, Long entryid) {
		this.sessionId=sessionId;
		this.entryid=entryid;
	}
	public Long getSessionId(){
		return this.sessionId;
	}
	public void setSessionId(Long sessionId){
		this.sessionId=sessionId;
	}
	public Long getEntryid(){
		return this.entryid;
	}
	public void setEntryid(Long entryid){
		this.entryid=entryid;
	}
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if ( ! (o instanceof VAuditReportPK)) {
			return false;
		}
		VAuditReportPK other = (VAuditReportPK) o;
		return this.sessionId.equals(other.sessionId)
			  && this.entryid.equals(other.entryid);
	}

	@Override
	public int hashCode() {
		return this.sessionId.hashCode()
			^ this.entryid.hashCode();
	}
	@Override
	public String toString() {
		return this.sessionId.toString()+"_"+
			   this.entryid.toString();
	}
}
