package iac.grn.infosweb.context.mc.audit.func;

import iac.grn.serviceitems.BaseTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AFuncContext {

    private HashMap<String, BaseTableItem> auditItemsMap=new HashMap<String, BaseTableItem>();
	
	public HashMap <String, BaseTableItem> getAuditItemsMap() {
        return this.auditItemsMap;
    }
	/*private TreeMap<String, BaseTableItem> auditItemsMap=new TreeMap<String, BaseTableItem>();
	
	public TreeMap <String, BaseTableItem> getAuditItemsMap() {
        return this.auditItemsMap;
    }*/
	
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
	
	public AFuncContext(){
		
		// ����� field.itemFiltField ��� field.itemSortField
		
		auditItemsMap.put("dateAction",new BaseTableItem("���� � ����� ��������", "...", "dateActionValue", "act_dat", "act_dat_value", 1));
	    auditItemsMap.put("userName",new BaseTableItem("������������", "...", "userName", "usr_fio", "usr_fio", 2));
	    auditItemsMap.put("isName",new BaseTableItem("��", "...", "isName", "arm_name", "arm_id", 3));
		auditItemsMap.put("actName",new BaseTableItem("��������", "...", "actName", "act_name", "act_name", 4));
		
	}
}
