package iac.grn.infosweb.context.mc.arm;

import iac.cud.infosweb.dataitems.BaseItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;


@Name("armUsrDataProvider")
public class ArmUsrDataProvider implements Serializable{
	
	@Logger private Log log;
	
	@In(create=true)
	private ArmUsrManager armUsrManager;

		
	
	public List<BaseItem> getItemsByrange(int firstRow, int numberOfRows, String sortField, 
			                                   boolean ascending) {
		List<BaseItem> ret = new ArrayList<BaseItem>();
	
		log.info("ugroupUsrDataProvider:getItemsByrange");
		ret=armUsrManager.getAuditList(firstRow, numberOfRows);
		return ret;
	}
	
	public void update() {
		// nothing need to do
	}
	
	public int getRowCount() {
		int ri=0;
		Long rL=armUsrManager.getAuditCount();
		if(rL!=null){
			ri = Integer.parseInt(rL.toString());
		}
		return ri;
	}
}
