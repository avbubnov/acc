package iac.grn.infosweb.context.proc.bindunbind;

import java.util.Date;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("procBindUnBindSettingsBean")
public class ProcBindUnBindSettingsBean {

	private Long paramActualData;
	
	public Long getParamActualData(){
		return this.paramActualData;
	}
	public void setParamActualData(Long paramActualData){
		this.paramActualData=paramActualData;
	}
	
	
}
