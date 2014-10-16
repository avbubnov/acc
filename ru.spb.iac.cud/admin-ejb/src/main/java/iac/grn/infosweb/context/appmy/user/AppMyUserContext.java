package iac.grn.infosweb.context.appmy.user;

import iac.grn.serviceitems.BaseTableItem;
import iac.grn.serviceitems.HeaderTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppMyUserContext {

	//private HashMap<String, HeaderTableItem> headerItemsMap=new HashMap<String, HeaderTableItem>();
	
	private List<HeaderTableItem> headerItemsList=new ArrayList<HeaderTableItem>();
	
    private HashMap<String, BaseTableItem> auditItemsMap=new HashMap<String, BaseTableItem>();
	
	public HashMap <String, BaseTableItem> getAuditItemsMap() {
        return this.auditItemsMap;
    }
	
	public List<HeaderTableItem> getHeaderItemsList() {
        return this.headerItemsList;
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
	
	public AppMyUserContext(){
		
		
		HeaderTableItem h1=null, h2=null, h3=null, h4=null, h5=null;
		
		headerItemsList.add(h4=new HeaderTableItem("��������� ����������", "h4")); 
		headerItemsList.add(h1=new HeaderTableItem("����������", "h1"));
		headerItemsList.add(h2=new HeaderTableItem("�����������", "h2"));
		headerItemsList.add(h3=new HeaderTableItem("������� ������", "h3")); 
		headerItemsList.add(h5=new HeaderTableItem("�������������", "h5"));
		
		auditItemsMap.put("statusValue",new BaseTableItem("������", "...", "statusValue", "t1_status", 0, h3));
		auditItemsMap.put("idApp",new BaseTableItem("�����", "...", "idApp", "t1_id", 2, h3));
		auditItemsMap.put("created",new BaseTableItem("����", "...", "created", "t1_created", 3, h3));
		
		auditItemsMap.put("rejectReason",new BaseTableItem("������� ������", "...", "rejectReason", "t1_reject_reason", 1, h5));
		auditItemsMap.put("comment",new BaseTableItem("�����������", "...", "comment", "t1_comment", 2, h5));
		
		auditItemsMap.put("orgName", new BaseTableItem("�����������", "...", "orgName", "t1_org_name", 20, h2));
		auditItemsMap.put("usrFio", new BaseTableItem("������������", "...", "usrFio", "t1_user_fio", 21, h2));
		
		auditItemsMap.put("surnameUser",new BaseTableItem("�������", "...", "surnameUser", "t1_SURNAME_USER", 1, h1));
		auditItemsMap.put("nameUser",new BaseTableItem("���", "...", "nameUser", "t1_NAME_USER", 2, h1));
		auditItemsMap.put("patronymicUser",new BaseTableItem("��������", "...", "patronymicUser", "t1_PATRONYMIC_USER", 3, h1));
		auditItemsMap.put("iogvCodeUser",new BaseTableItem("��� ���� ������������", "...", "iogvCodeUser", "t1_SIGN_USER", 4, h1));
		auditItemsMap.put("positionUser",new BaseTableItem("���������", "...", "positionUser", "t1_POSITION_USER", 5, h1));
		auditItemsMap.put("emailUser",new BaseTableItem("Email", "...", "emailUser", "t1_EMAIL_USER", 6, h1));
		auditItemsMap.put("phoneUser",new BaseTableItem("�������", "...", "phoneUser", "t1_PHONE_USER", 7, h1));
		auditItemsMap.put("certificateUser",new BaseTableItem("����������", "...", "certificateUser", "t1_CERTIFICATE_USER", 8, h1));
		auditItemsMap.put("nameDepartament",new BaseTableItem("�������������", "...", "nameDepartament", "t1_NAME_DEPARTAMENT", 9, h1));
		auditItemsMap.put("nameOrg",new BaseTableItem("�����������", "...", "nameOrg", "t1_NAME_ORG", 10, h1));
		auditItemsMap.put("iogvCodeOrg",new BaseTableItem("��� ���� �����������", "...", "iogvCodeOrg", "t1_SIGN_ORG", 11, h1));
		auditItemsMap.put("commentApp",new BaseTableItem("����������� ������", "...", "commentApp", "t1_comment_app", 12, h1));
		

		//auditItemsMap.put("idUser",new BaseTableItem("�� ������������", "...", "idArm", "t1_user_id", 33, h4));
		auditItemsMap.put("login",new BaseTableItem("����� ������������", "...", "login", "t1_user_login", 34, h4));
			
	
		}
}
