package iac.grn.infosweb.context.app.user.modify;

import iac.grn.serviceitems.BaseTableItem;
import iac.grn.serviceitems.HeaderTableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppUserModifyContext {

	
	private List<HeaderTableItem> headerItemsList=new ArrayList<HeaderTableItem>();
	
    private Map<String, BaseTableItem> auditItemsMap=new HashMap<String, BaseTableItem>();
	
	public Map <String, BaseTableItem> getAuditItemsMap() {
        return this.auditItemsMap;
    }
	
	public List<HeaderTableItem> getHeaderItemsList() {
        return this.headerItemsList;
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
	
	public AppUserModifyContext(){
		
		
		HeaderTableItem h1=null, h2=null, h3=null, h4=null, h5=null;
		
		
		headerItemsList.add(h1=new HeaderTableItem("����������", "h1"));
		headerItemsList.add(h4=new HeaderTableItem("�������� ������", "h4")); 
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
		
		auditItemsMap.put("surname",new BaseTableItem("�������", "...", "surname", "t1_SURNAME_USER", 1, h1));
		auditItemsMap.put("name",new BaseTableItem("���", "...", "name", "t1_NAME_USER", 2, h1));
		auditItemsMap.put("patronymic",new BaseTableItem("��������", "...", "patronymic", "t1_PATRONYMIC_USER", 3, h1));
		auditItemsMap.put("iogvCode",new BaseTableItem("��� ���� ������������", "...", "iogvCode", "t1_SIGN_USER", 4, h1));
		auditItemsMap.put("position",new BaseTableItem("���������", "...", "position", "t1_POSITION_USER", 5, h1));
		auditItemsMap.put("email",new BaseTableItem("Email", "...", "email", "t1_EMAIL_USER", 6, h1));
		auditItemsMap.put("phone",new BaseTableItem("�������", "...", "phone", "t1_PHONE_USER", 7, h1));
		auditItemsMap.put("certificate",new BaseTableItem("����������", "...", "certificate", "t1_CERTIFICATE_USER", 8, h1));
		auditItemsMap.put("departament",new BaseTableItem("�������������", "...", "departament", "t1_NAME_DEPARTAMENT", 9, h1));
	
		
		
		auditItemsMap.put("loginUser",new BaseTableItem("����� ", "...", "loginUser", "t1_user_login_app", 2, h4));
		auditItemsMap.put("fioUser",new BaseTableItem("���", "...", "fioUser", "t1_user_fio_app", 3, h4));
		auditItemsMap.put("positionUser",new BaseTableItem("���������", "...", "positionUser", "t1_user_pos_app", 5, h4));
		auditItemsMap.put("nameDep",new BaseTableItem("�������������", "...", "nameDep", "t1_dep_name_app", 6, h4));
		auditItemsMap.put("nameOrg",new BaseTableItem("�����������", "...", "nameOrg", "t1_org_name_app", 7, h4));
		auditItemsMap.put("certUser",new BaseTableItem("����������", "...", "certUser", "t1_cert_app", 8, h4));
		auditItemsMap.put("emailUser",new BaseTableItem("Email", "...", "emailUser", "t1_user_email_app", 9, h4));
		auditItemsMap.put("phoneUser",new BaseTableItem("�������", "...", "phoneUser", "t1_user_tel_app", 10, h4));
		auditItemsMap.put("iogvCodeUser",new BaseTableItem("��� ����", "...", "iogvCodeUser", "t1_usr_code_app", 11, h4));
		}
}
