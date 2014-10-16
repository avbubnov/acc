package iac.grn.infosweb.context.mc.clorg;

import iac.grn.serviceitems.BaseTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClOrgContext {

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
	
	public ClOrgContext(){
		
		auditItemsMap.put("signObject",new BaseTableItem("��� ����", "...", "signObject", 1));
		auditItemsMap.put("full",new BaseTableItem("������������", "...", "full", 2));
		auditItemsMap.put("fio",new BaseTableItem("���", "...", "fio", 3));
	    auditItemsMap.put("postalCode",new BaseTableItem("�������� ���", "...", "postalCode", 4));
		auditItemsMap.put("place",new BaseTableItem("��������� �����", "...", "place", 5));
		auditItemsMap.put("prefix",new BaseTableItem("������� ������", "...", "prefix", 6));
		auditItemsMap.put("house",new BaseTableItem("����� ����", "...", "house", 7));
		auditItemsMap.put("phone",new BaseTableItem("�������", "...", "phone", 8));
		auditItemsMap.put("fax",new BaseTableItem("����", "...", "fax", 9));
		auditItemsMap.put("email",new BaseTableItem("E-mail", "...", "email", 10));
		
	/*	auditItemsMap.put("shortName",new BaseTableItem("������� ������������", "...", "shortName", 2));
		auditItemsMap.put("contactEmployeeFio",new BaseTableItem("��� ������������", "...", "contactEmployeeFio", 3));
		auditItemsMap.put("contactEmployeePosition",new BaseTableItem("���������","...", "contactEmployeePosition", 4));
		auditItemsMap.put("contactEmployeePhone",new BaseTableItem("�������", "...", "contactEmployeePhone", 5));
		auditItemsMap.put("isExternalValue",new BaseTableItem("��� �����������","...", "isExternalValue", "isExternal", 6));
		auditItemsMap.put("letValue",new BaseTableItem("��� �����������","...", "letValue", "acLegalEntityType2.name", 7));*/

	}
}
