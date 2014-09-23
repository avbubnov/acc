package iac.grn.infosweb.context.proc.archafunc;

import java.util.Date;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("procArchAFuncSettingsBean")
public class ProcArchAFuncSettingsBean {

	private Long paramActualData;
	
	public Long getParamActualData(){
		return this.paramActualData;
	}
	public void setParamActualData(Long paramActualData){
		this.paramActualData=paramActualData;
	}
	
	
}
