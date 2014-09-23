package iac.grn.infosweb.context.proc.archasys;

import java.util.Date;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("procArchASysSettingsBean")
public class ProcArchASysSettingsBean {

	private Long paramActualData;
	
	public Long getParamActualData(){
		return this.paramActualData;
	}
	public void setParamActualData(Long paramActualData){
		this.paramActualData=paramActualData;
	}
	
	
}
