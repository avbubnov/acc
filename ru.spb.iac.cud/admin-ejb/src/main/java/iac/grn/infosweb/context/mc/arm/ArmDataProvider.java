package iac.grn.infosweb.context.mc.arm;

import iac.cud.infosweb.dataitems.BaseItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;


@Name("armDataProvider")
public class ArmDataProvider implements Serializable{
	
	@Logger private Log log;
	
	@In(create=true)
	private ArmManager armManager;

	private List<BaseItem> allItems = null;
	
	/*	private synchronized void initData() {
		log.info("AuditDataProvider:initData");
		allItems=auditManager.getAuditList();
	}

	public List<VAuditReport> getAllItems() {
		log.info("AuditDataProvider:getAllItems");
		if (allItems!=null && allItems.size()>0) {
			return allItems;
		} else {
			initData();
			return allItems;
		}
	}
	public VAuditReport getAuctionItemByPk(VAuditReportPK pk) {
		for (VAuditReport item:getAllItems()) {
			if (item.getPk().equals(pk)) {
				return item;
			}
		}
		throw new RuntimeException("Auction Item pk="+pk.toString()+" not found");
	}
	public boolean hasAuctionItemByPk(VAuditReportPK pk) {
		for (VAuditReport item:getAllItems()) {
			if (item.getPk().equals(pk)) {
				return true;
			}
		}
		return false;
		
	}*/
	
	public List<BaseItem> getItemsByrange(int firstRow, int numberOfRows, String sortField, 
			                                   boolean ascending) {
		List<BaseItem> ret = new ArrayList<BaseItem>();
	/*	for (int counter=0; counter<numberOfRows; counter++) {
			ret.add(getAllItems().get(startPk.intValue()+counter));
		}*/
		log.info("AuditDataProvider:getItemsByrange");
		ret=armManager.getAuditList(firstRow, numberOfRows);
		return ret;
	}
	
	public void update() {
		// nothing need to do
	}
	
	public int getRowCount() {
		int ri=0;
		Long rL=armManager.getAuditCount();
		if(rL!=null){
			ri = Integer.parseInt(rL.toString());
		}
		return ri;
	}
}
