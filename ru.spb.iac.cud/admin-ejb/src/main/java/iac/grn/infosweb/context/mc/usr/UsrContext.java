package iac.grn.infosweb.context.mc.usr;

import iac.grn.serviceitems.BaseTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class UsrContext {

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
	
	public UsrContext(){
		
		
		
		auditItemsMap.put("statusValue",new BaseTableItem("������","...", "statusValue", "t1_status", 0));
		auditItemsMap.put("fio",new BaseTableItem("���","...", "fio", "t1_fio", 10));
		
		auditItemsMap.put("login",new BaseTableItem("�����","...", "login", "t1_login", 11));
		auditItemsMap.put("cert",new BaseTableItem("����� �����������","...", "cert", 12));
		
	/*	auditItemsMap.put("surname",new BaseTableItem("�������","...", "surname"));
		auditItemsMap.put("name1",new BaseTableItem("���","...", "name1"));
		auditItemsMap.put("name2",new BaseTableItem("��������","...", "name2"));*/
		auditItemsMap.put("phone",new BaseTableItem("�������","...", "phone", 20));
		auditItemsMap.put("email",new BaseTableItem("E-mail","...", "email", 30));
		auditItemsMap.put("orgName",new BaseTableItem("�����������","...", "orgName", "t1_org_name", 40));
		auditItemsMap.put("orgCode",new BaseTableItem("��� ���� �����������","...", "orgCode", 41));
		
		auditItemsMap.put("department",new BaseTableItem("�������������","...", "department", 42));
		auditItemsMap.put("position",new BaseTableItem("���������","...", "position", 43));
		
		auditItemsMap.put("usrCode",new BaseTableItem("��� ���� ������������","...", "usrCode", 44));
		
		//auditItemsMap.put("orgName",new BaseTableItem("�����������","...", "orgName", "acOrganization2.fullName", 40));
		
		//auditItemsMap.put("protectLogin",new BaseTableItem("�����","...", "protectLogin", 50));
		//auditItemsMap.put("protectPassword",new BaseTableItem("������","...", "protectPassword", 60));
		
		
		
		auditItemsMap.put("start",new BaseTableItem("���� ������ ��������","...", "start", 70));
		auditItemsMap.put("finish",new BaseTableItem("���� ��������� ��������","...", "finish", 80));
		/*auditItemsMap.put("crtUserName",new BaseTableItem("������","...", "crtUserName", 10));
		auditItemsMap.put("updUserName",new BaseTableItem("������������","...", "updUserName", 11));
		auditItemsMap.put("created",new BaseTableItem("���� ��������","...", "created", 12));
		auditItemsMap.put("modified",new BaseTableItem("���� ��������������","...", "modified", 13));*/
		auditItemsMap.put("crtDate",new BaseTableItem("���� ��������","...", "crtDate", "t1_crt_date", 90));
		auditItemsMap.put("crtUserLogin",new BaseTableItem("������","...", "crtUserLogin", 91));
		auditItemsMap.put("updDate",new BaseTableItem("���� ��������������","...", "updDate", "t1_crt_date",92));
		auditItemsMap.put("updUserLogin",new BaseTableItem("������������","...", "updUserLogin", 93));
	}
}
