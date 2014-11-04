package iac.grn.infosweb.context.mc.perm;

import iac.grn.serviceitems.BaseTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PermContext {

    private Map<String, BaseTableItem> auditItemsMap=new HashMap<String, BaseTableItem>();
	
	public Map <String, BaseTableItem> getAuditItemsMap() {
        return this.auditItemsMap;
    }

	
	public List <BaseTableItem> getAuditItemsCollection() {
		
		List<BaseTableItem> btiList = new ArrayList<BaseTableItem>(this.auditItemsMap.values());
		Collections.sort(btiList, new Comparator<BaseTableItem>() {

	        public int compare(BaseTableItem o1, BaseTableItem o2) {
	        	
	        	int sort1=o1.getItemSort();
	        	int sort2=o2.getItemSort();
	        	
	        	if(sort1 > sort2){
	                return 1;
	            }else if(sort1 < sort2){
	                return -1; 
	            }else{
	                return 0;
	            }    
	         }
	    });

        return btiList;
    }
	
	public PermContext(){
		
		auditItemsMap.put("idPerm",new BaseTableItem("��", "...", "idPerm", 1));
		auditItemsMap.put("permName",new BaseTableItem("��������", "...", "permName", 2));
		auditItemsMap.put("permDescr",new BaseTableItem("��������", "...", "permDescr", 3));
		
		auditItemsMap.put("orderNum",new BaseTableItem("����������", "...", "orderNum", 4));
		
	}
}
