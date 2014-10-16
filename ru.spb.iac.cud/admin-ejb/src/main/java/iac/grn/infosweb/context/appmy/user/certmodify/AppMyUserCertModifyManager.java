package iac.grn.infosweb.context.appmy.user.certmodify;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.persistence.NoResultException;

import org.apache.xml.security.utils.Base64;
import org.hibernate.lob.BlobImpl;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;

import ru.spb.iac.cud.core.util.CUDConstants;


import iac.cud.infosweb.dataitems.AppUserCertModifyItem;
import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.UserItem;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.AcUsersCertBssT;
import iac.cud.infosweb.entity.IspBssT;
import iac.cud.infosweb.entity.JournAppUserCertaddBssT;
import iac.grn.infosweb.context.app.access.AppAccessContext;
import iac.grn.infosweb.context.mc.arm.ArmManager;
import iac.grn.infosweb.context.mc.usr.UsrManager;
import iac.grn.infosweb.session.table.BaseDataModel;
import iac.grn.infosweb.session.table.BaseManager;
import iac.grn.serviceitems.BaseTableItem;
import iac.grn.serviceitems.HeaderTableItem;

@Name("appMyUserCertModifyManager")
public class AppMyUserCertModifyManager extends BaseManager{

	
	private String rejectReason;
	private String commentText;
	
	private List<HeaderTableItem> headerItemsListContextCREATE;
	
	public void invokeLocal(String type, int firstRow, int numberOfRows,
	           String sessionId) {
		
		 log.info("hostsManager:invokeLocal");
		 try{
			 String orderQuery=null;
			 log.info("hostsManager:invokeLocal");
			 
			 AppMyUserCertModifyStateHolder appMyUserCertModifyStateHolder = (AppMyUserCertModifyStateHolder)
					  Component.getInstance("appMyUserCertModifyStateHolder",ScopeType.SESSION);
			 HashMap<String, String> filterMap = appMyUserCertModifyStateHolder.getColumnFilterValues();
			 String st=null;
			  
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				 Set<Map.Entry<String, String>> set = appMyUserCertModifyStateHolder.getSortOrders().entrySet();
                 for (Map.Entry<String, String> me : set) {
      		       log.info("me.getKey+:"+me.getKey());
      		       log.info("me.getValue:"+me.getValue());
      		       
      		       if(orderQuery==null){
      		    	 orderQuery="order by "+me.getKey()+" "+me.getValue();
      		       }else{
      		    	 orderQuery=orderQuery+", "+me.getKey()+" "+me.getValue();  
      		       }
      		     }
                 log.info("invokeLocal:list:orderQuery:"+orderQuery);
                 
                 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : set_filter) {
    	            	  log.info("me.getKey+:"+me.getKey());
    	            	  log.info("me.getValue:"+me.getValue());
    	   		      
    	   		     if(me.getKey().equals("t1_crt_date")){  
    	        	   //  st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	        	   //������ ������ �� ������  
    	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	   
    	   		     }else if(me.getKey().equals("t1_iogv_bind_type")&&(me.getValue()!=null && me.getValue().equals("-2"))){
    	    	    	 
    	    	    	 st=(st!=null?st+" and " :"")+" t1_usr_code is null ";
    	    	    	 
    	    	     }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//������ ������ �� ������
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	        	  }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);

             
               List<Object[]> lo=null;
               AppUserCertModifyItem ui = null;
               DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
               

             lo=entityManager.createNativeQuery(
             "select t1.t1_id, t1.t1_created, "+
             "t1.t1_status, t1_org_name,  t1_user_fio, t1_reject_reason, t1_comment, "+
           
             //
             
             "t1_org_name_app, t1_user_id_app,  t1_user_login_app, t1_user_fio_app, t1_user_pos_app, "+
             "t1_dep_name_app, " +
             "t1_cert_app,  t1_usr_code_app, t1_user_tel_app,  t1_user_email_app "+
              "from(  "+
             "select JAS.ID_SRV t1_id, JAS.CREATED t1_created,   "+
             "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
             "JAS.COMMENT_ t1_comment, "+
              "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
              "JAS.REJECT_REASON t1_reject_reason,  "+
      
				//
              
              "AU_APP.ID_SRV  t1_user_id_app, AU_APP.LOGIN  t1_user_login_app, "+
               "CL_ORG_app.FULL_ t1_org_name_app,  decode(AU_app.UP_SIGN_USER, null, AU_app.SURNAME||' '||AU_app.NAME_ ||' '|| AU_app.PATRONYMIC,  CL_USR_app.FIO ) t1_user_fio_app, "+
                 "decode(AU_app.UP_SIGN_USER, null, AU_app.POSITION, CL_USR_app.POSITION) t1_user_pos_app, "+
                 
                 "decode(AU_app.UP_SIGN_USER, null, AU_app.DEPARTMENT, decode(substr(CL_DEP_app.sign_object,4,2), '00', null, CL_DEP_app.FULL_)) t1_dep_name_app, " +
                 "AU_app.CERTIFICATE t1_cert_app, AU_app.UP_SIGN_user t1_usr_code_app, "+   
                 "decode(AU_app.UP_SIGN_USER, null, AU_app.PHONE, CL_USR_app.PHONE ) t1_user_tel_app, "+   
                 "decode(AU_app.UP_SIGN_USER, null, AU_app.E_MAIL, CL_USR_app.EMAIL) t1_user_email_app "+
            "from JOURN_APP_USER_CERTADD_BSS_T jas, "+
               "AC_USERS_KNL_T au_FULL,   "+
                "ISP_BSS_T cl_org_full, "+
                 "ISP_BSS_T cl_usr_full, "+
                 "ISP_BSS_T cl_org_app, "+
                 "ISP_BSS_T cl_usr_app, "+
                 "ISP_BSS_T cl_dep_app, "+
                "AC_USERS_KNL_T au_APP, "+
              "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
                "from ISP_BSS_T cl_org "+
                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
                "group by CL_ORG.SIGN_OBJECT) t03, "+
                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
                            "from ISP_BSS_T cl_usr "+
                            "where CL_USR.FIO is not null "+
                            "group by CL_usr.SIGN_OBJECT) t02,   "+
                
                 "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
                "from ISP_BSS_T cl_org "+
                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
                "group by CL_ORG.SIGN_OBJECT) t03_app, "+
                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
                            "from ISP_BSS_T cl_usr "+
                            "where CL_USR.FIO is not null "+
                            "group by CL_usr.SIGN_OBJECT) t02_app,  "+ 
               "(select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE  "+
                            "from ISP_BSS_T cl_dep "+
                            "where CL_dep.SIGN_OBJECT LIKE '%000' "+
                            "group by CL_DEP.SIGN_OBJECT) t04_app "+
                                      
                "where JAS.UP_USER=AU_FULL.ID_SRV "+
                "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
                "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
                "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
                "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
                "and au_APP.ID_SRV =JAS.UP_USER_APP "+
                
                "and au_APP.UP_SIGN=t03_APP.CL_ORG_CODE "+
                "and CL_ORG_app.ID_SRV=t03_APP.CL_ORG_ID "+
                
                "and AU_APP.UP_SIGN_USER=t02_APP.CL_USR_CODE(+) "+
                "and CL_USR_APP.ID_SRV(+)=t02_APP.CL_USR_ID "+
                
                "and substr(au_APP.UP_SIGN,1,5)||'000'=t04_APP.CL_dep_CODE(+) "+
                "and CL_dep_app.ID_SRV(+)=t04_APP.CL_dep_ID "+
                "and JAS.UP_USER= :idUser "+ 
                
              ") t1 "+
              (st!=null ? " where "+st :" ")+
              (orderQuery!=null ? orderQuery+", t1_id desc " : " order by t1_id desc "))
              .setFirstResult(firstRow)
              .setMaxResults(numberOfRows)
              .setParameter("idUser", getCurrentUser().getBaseId())
              .getResultList();
               auditList = new ArrayList<BaseItem>();
               
               for(Object[] objectArray :lo){
            	   try{
            		   log.info("invokeLocal:list:02");
            		   

                	     ui= new AppUserCertModifyItem(
            	    		 (objectArray[0]!=null?new Long(objectArray[0].toString()):null),
            				 (objectArray[1]!=null?df.format((Date)objectArray[1]) :""),
            				 (objectArray[2]!=null?Integer.parseInt(objectArray[2].toString()):0),	
            				 (objectArray[3]!=null?objectArray[3].toString():""),
            				 (objectArray[4]!=null?objectArray[4].toString():""),
            				 (objectArray[5]!=null?objectArray[5].toString():""),
            				 (objectArray[6]!=null?objectArray[6].toString():""),
            				 
            				 (""),
	            			 (""),
	            			 (""),
	            			 (""),
	            			 (""),
	            			 
	            			 (objectArray[7]!=null?objectArray[7].toString():""),
	            			 (objectArray[8]!=null?new Long(objectArray[8].toString()):null),
	            			 (objectArray[9]!=null?objectArray[9].toString():""),
	            			 (objectArray[10]!=null?objectArray[10].toString():""),
	            			 (objectArray[11]!=null?objectArray[11].toString():""),
	            			 (objectArray[12]!=null?objectArray[12].toString():""),
	            			 (objectArray[13]!=null?objectArray[13].toString():""),
	            			 (objectArray[14]!=null?objectArray[14].toString():""),
	            			 (objectArray[15]!=null?objectArray[15].toString():""),
	            			 (objectArray[16]!=null?objectArray[16].toString():""),
	            			 
	            			 //modeExec
                	    	 1);
                	     
            	     auditList.add(ui);
            	   }catch(Exception e1){
            		   log.error("invokeLocal:for:error:"+e1);
            	   }
               }  
               
             log.info("invokeLocal:list:02");
             
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 
                 
                 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : set_filter) {
    	            	  log.info("me.getKey+:"+me.getKey());
    	            	  log.info("me.getValue:"+me.getValue());
    	   		    
    	            	  /*
    	   		     //  if(me.getKey().equals("LCR.CREATED")){  
    	        	//	 st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	        	//   }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//������ ������ �� ������
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	        	 //  }
    	            	 */ 
    	            	  
    	              if(me.getKey().equals("t1_iogv_bind_type")&&(me.getValue()!=null && me.getValue().equals("-2"))){
     	    	    	 st=(st!=null?st+" and " :"")+" t1_usr_code is null ";
    	              }else{
    	            	 st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	              }	 
     	    	 
    	            	  
    	              }
    	    	   }
				 
				/* 
				 auditCount = (Long)entityManager.createQuery(
						 "select count(au) " +
				         "from AcUser au "+
				         (st!=null ? " where "+st :""))
		                .getSingleResult();*/
				 
				
				 auditCount = ((java.math.BigDecimal)entityManager.createNativeQuery(
						       "select count(*) " +
						    		  "from(  "+
						                "select JAS.ID_SRV t1_id, JAS.CREATED t1_created,   "+
						                "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
						                "JAS.COMMENT_ t1_comment, "+
						                 "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
						                 "JAS.REJECT_REASON t1_reject_reason,  "+
						         
			        		      //
										
						                 "AU_APP.ID_SRV  t1_user_id_app, AU_APP.LOGIN  t1_user_login_app, "+
						                  "CL_ORG_app.FULL_ t1_org_name_app,  decode(AU_app.UP_SIGN_USER, null, AU_app.SURNAME||' '||AU_app.NAME_ ||' '|| AU_app.PATRONYMIC,  CL_USR_app.FIO ) t1_user_fio_app, "+
						                    "decode(AU_app.UP_SIGN_USER, null, AU_app.POSITION, CL_USR_app.POSITION) t1_user_pos_app, "+
						                    
						                    "decode(AU_app.UP_SIGN_USER, null, AU_app.DEPARTMENT, decode(substr(CL_DEP_app.sign_object,4,2), '00', null, CL_DEP_app.FULL_)) t1_dep_name_app, "+
						                    "AU_app.CERTIFICATE t1_cert_app, AU_app.UP_SIGN_user t1_usr_code_app, "+   
						                    "decode(AU_app.UP_SIGN_USER, null, AU_app.PHONE, CL_USR_app.PHONE ) t1_user_tel_app, "+   
						                    "decode(AU_app.UP_SIGN_USER, null, AU_app.E_MAIL, CL_USR_app.EMAIL) t1_user_email_app "+
						               "from JOURN_APP_USER_CERTADD_BSS_T jas, "+
						                  "AC_USERS_KNL_T au_FULL,   "+
						                   "ISP_BSS_T cl_org_full, "+
						                    "ISP_BSS_T cl_usr_full, "+
						                    "ISP_BSS_T cl_org_app, "+
						                    "ISP_BSS_T cl_usr_app, "+
						                    "ISP_BSS_T cl_dep_app, "+
						                   "AC_USERS_KNL_T au_APP, "+
						                 "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
						                   "from ISP_BSS_T cl_org "+
						                   "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
						                   "group by CL_ORG.SIGN_OBJECT) t03, "+
						                    "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
						                               "from ISP_BSS_T cl_usr "+
						                               "where CL_USR.FIO is not null "+
						                               "group by CL_usr.SIGN_OBJECT) t02,   "+
						                   
						                    "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
						                   "from ISP_BSS_T cl_org "+
						                   "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
						                   "group by CL_ORG.SIGN_OBJECT) t03_app, "+
						                    "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
						                               "from ISP_BSS_T cl_usr "+
						                               "where CL_USR.FIO is not null "+
						                               "group by CL_usr.SIGN_OBJECT) t02_app,  "+ 
						                  "(select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE  "+
						                               "from ISP_BSS_T cl_dep "+
						                               "where CL_dep.SIGN_OBJECT LIKE '%000' "+
						                               "group by CL_DEP.SIGN_OBJECT) t04_app "+
						                                         
						                   "where JAS.UP_USER=AU_FULL.ID_SRV "+
						                   "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
						                   "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
						                   "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
						                   "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
						                   "and au_APP.ID_SRV =JAS.UP_USER_APP "+
						                   
						                   "and au_APP.UP_SIGN=t03_APP.CL_ORG_CODE "+
						                   "and CL_ORG_app.ID_SRV=t03_APP.CL_ORG_ID "+
						                   
						                   "and AU_APP.UP_SIGN_USER=t02_APP.CL_USR_CODE(+) "+
						                   "and CL_USR_APP.ID_SRV(+)=t02_APP.CL_USR_ID "+
						                   
						                   "and substr(au_APP.UP_SIGN,1,5)||'000'=t04_APP.CL_dep_CODE(+) "+
						                   "and CL_dep_app.ID_SRV(+)=t04_APP.CL_dep_ID "+
						                   "and JAS.UP_USER= :idUser "+ 
						                   
						                 ") t1 "+
		         (st!=null ? " where "+st :" "))
		       .setParameter("idUser", getCurrentUser().getBaseId())
               .getSingleResult()).longValue();
                 
                 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if(type.equals("bean")){
				 
			 }
		}catch(Exception e){
			  log.error("invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("������!");
		}

	}
	
	 private AppUserCertModifyItem getUserItem(Long idUser){
		 if(idUser==null){
			  return null;
		   }
		   
		   try{
	           List<Object[]> lo=null;
	           AppUserCertModifyItem ui = null;
	           DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
	           
	           //!!!
	           //t1_usr_code_app - ������� ������(� ���� ���������� ���������)
	           //t1_SIGN_USER - ���� �� ���������� �� ���������
	           
	           lo=entityManager.createNativeQuery(
	        		    "select t1.t1_id, t1.t1_created, "+
	        		             "t1.t1_status, t1_org_name,  t1_user_fio, t1_reject_reason, t1_comment, "+
	        		           
                                  "t1_CERTIFICATE_USER, "+

	        		             "t1_org_name_app, t1_user_id_app,  t1_user_login_app, t1_user_fio_app, t1_user_pos_app, "+
	        		             "t1_dep_name_app, " +
	        		             "t1_cert_app,  t1_usr_code_app, t1_user_tel_app,  t1_user_email_app, "+
	        		             "t1_MODE_EXEC "+
	        		              "from(  "+
	        		             "select JAS.ID_SRV t1_id, JAS.CREATED t1_created,   "+
	        		             "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
	        		             "JAS.COMMENT_ t1_comment, "+
	        		              "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
	        		              "JAS.REJECT_REASON t1_reject_reason,  "+
	        		      
								  "JAS.CERT_VALUE t1_CERTIFICATE_USER, "+ 
	        		          
	        		              "AU_APP.ID_SRV  t1_user_id_app, AU_APP.LOGIN  t1_user_login_app, "+
	        		               "CL_ORG_app.FULL_ t1_org_name_app,  decode(AU_app.UP_SIGN_USER, null, AU_app.SURNAME||' '||AU_app.NAME_ ||' '|| AU_app.PATRONYMIC,  CL_USR_app.FIO ) t1_user_fio_app, "+
	        		                 "decode(AU_app.UP_SIGN_USER, null, AU_app.POSITION, CL_USR_app.POSITION) t1_user_pos_app, "+
	        		                 
	        		                 "decode(AU_app.UP_SIGN_USER, null, AU_app.DEPARTMENT, decode(substr(CL_DEP_app.sign_object,4,2), '00', null, CL_DEP_app.FULL_)) t1_dep_name_app, "+
	        		                 "AU_app.CERTIFICATE t1_cert_app, AU_app.UP_SIGN_user t1_usr_code_app, "+   
	        		                 "decode(AU_app.UP_SIGN_USER, null, AU_app.PHONE, CL_USR_app.PHONE ) t1_user_tel_app, "+   
	        		                 "decode(AU_app.UP_SIGN_USER, null, AU_app.E_MAIL, CL_USR_app.EMAIL) t1_user_email_app, "+
	        		                 "JAS.MODE_EXEC t1_MODE_EXEC "+  
	        		              "from JOURN_APP_USER_CERTADD_BSS_T jas, "+
	        		               "AC_USERS_KNL_T au_FULL,   "+
	        		                "ISP_BSS_T cl_org_full, "+
	        		                 "ISP_BSS_T cl_usr_full, "+
	        		                 "ISP_BSS_T cl_org_app, "+
	        		                 "ISP_BSS_T cl_usr_app, "+
	        		                 "ISP_BSS_T cl_dep_app, "+
	        		                "AC_USERS_KNL_T au_APP, "+
	        		              "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
	        		                "from ISP_BSS_T cl_org "+
	        		                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
	        		                "group by CL_ORG.SIGN_OBJECT) t03, "+
	        		                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
	        		                            "from ISP_BSS_T cl_usr "+
	        		                            "where CL_USR.FIO is not null "+
	        		                            "group by CL_usr.SIGN_OBJECT) t02,   "+
	        		                
	        		                 "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE  "+
	        		                "from ISP_BSS_T cl_org "+
	        		                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
	        		                "group by CL_ORG.SIGN_OBJECT) t03_app, "+
	        		                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE  "+
	        		                            "from ISP_BSS_T cl_usr "+
	        		                            "where CL_USR.FIO is not null "+
	        		                            "group by CL_usr.SIGN_OBJECT) t02_app,  "+ 
	        		               "(select max(CL_dep.ID_SRV) CL_DEP_ID,  CL_DEP.SIGN_OBJECT  CL_DEP_CODE  "+
	        		                            "from ISP_BSS_T cl_dep "+
	        		                            "where CL_dep.SIGN_OBJECT LIKE '%000' "+
	        		                            "group by CL_DEP.SIGN_OBJECT) t04_app "+
	        		                                      
	        		                "where JAS.UP_USER=AU_FULL.ID_SRV "+
	        		                "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
	        		                "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
	        		                "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
	        		                "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
	        		                "and au_APP.ID_SRV =JAS.UP_USER_APP "+
	        		                
	        		                "and au_APP.UP_SIGN=t03_APP.CL_ORG_CODE "+
	        		                "and CL_ORG_app.ID_SRV=t03_APP.CL_ORG_ID "+
	        		                
	        		                "and AU_APP.UP_SIGN_USER=t02_APP.CL_USR_CODE(+) "+
	        		                "and CL_USR_APP.ID_SRV(+)=t02_APP.CL_USR_ID "+
	        		                
	        		                "and substr(au_APP.UP_SIGN,1,5)||'000'=t04_APP.CL_dep_CODE(+) "+
	        		                "and CL_dep_app.ID_SRV(+)=t04_APP.CL_dep_ID "+
	        		                "and JAS.ID_SRV=? "+
	        		             ") t1 ")
	         .setParameter(1, idUser)
	         .getResultList();
	           
	           for(Object[] objectArray :lo){
	        	   try{
	        		   log.info("AppMyUserCertModifyManager:getUserItem:login:"+objectArray[1].toString());
	        		   
	        		  
	        		   String serial = null;
        	           String certDate = null;
        			   String userFio = null;
        			   String depName = null;
        			   String orgName = null;
	        		   
	        		   if(objectArray[7]!=null){
	        			   //cert_value
	        			  
	        			  java.sql.Blob sb = (java.sql.Blob) objectArray[7];
	        			   
	        			  log.info("AppMyUserCertModifyManager:getUserItem:02:"+sb.length());
		        		  
	        			  byte[] cert_byte = Base64.decode(sb.getBytes(1L, (int)sb.length()));
	        			  
	        			  log.info("AppMyUserCertModifyManager:getUserItem:03:"+cert_byte.length);
	        			  
	        			  CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
	        	          X509Certificate user_cert = (X509Certificate)
	        	        		   user_cf.generateCertificate(new ByteArrayInputStream(cert_byte));
	        	        //  X509Certificate user_cert = (X509Certificate)
	        	        //		   user_cf.generateCertificate(sb.getBinaryStream());
	        		   
	        	        
	        	           serial = dec_to_hex(user_cert.getSerialNumber());
	        	           
	        	           certDate = df.format(user_cert.getNotAfter());
	        	           
	        	           String subject = user_cert.getSubjectDN().getName();
	        	    	   
	        	    	   log.info("AppMyUserCertModifyManager:getUserItem::"+subject);
	        	    	   
	        	    	   LdapName ldapDN = new LdapName(subject);
	        			   
	        			   for(Rdn rdn: ldapDN.getRdns()) {
	        				  //  System.out.println(rdn.getType() + " -> " + rdn.getValue());
	        				    
	        				    if("CN".equals(rdn.getType())){
	        				    	userFio = (String)rdn.getValue();
	        				    }else if("OU".equals(rdn.getType())){
	        				    	depName = (String)rdn.getValue();
	        				    }else if("O".equals(rdn.getType())){
	        				    	orgName = (String)rdn.getValue();
	        				    }
	        				    
	        				}
	        		   }
	        		   
	        		   ui= new AppUserCertModifyItem(
	            	    		 (objectArray[0]!=null?new Long(objectArray[0].toString()):null),
	            				 (objectArray[1]!=null?df.format((Date)objectArray[1]) :""),
	            				 (objectArray[2]!=null?Integer.parseInt(objectArray[2].toString()):0),	
	            				 (objectArray[3]!=null?objectArray[3].toString():""),
	            				 (objectArray[4]!=null?objectArray[4].toString():""),
	            				 (objectArray[5]!=null?objectArray[5].toString():""),
	            				 (objectArray[6]!=null?objectArray[6].toString():""),
	            				 
	            				 (serial!=null?serial:""),
		            			 (certDate!=null?certDate:""),
		            			 (orgName!=null?orgName:""),
		            			 (depName!=null?depName:""),
		            			 (userFio!=null?userFio:""),
		            			 
		            			 (objectArray[8]!=null?objectArray[8].toString():""),
		            			 (objectArray[9]!=null?new Long(objectArray[9].toString()):null),
		            			 (objectArray[10]!=null?objectArray[10].toString():""),
		            			 (objectArray[11]!=null?objectArray[11].toString():""),
		            			 (objectArray[12]!=null?objectArray[12].toString():""),
		            			 (objectArray[13]!=null?objectArray[13].toString():""),
		            			 (objectArray[14]!=null?objectArray[14].toString():""),
		            			 (objectArray[15]!=null?objectArray[15].toString():""),
		            			 (objectArray[16]!=null?objectArray[16].toString():""),
		            			 (objectArray[17]!=null?objectArray[17].toString():""),
		            			 (objectArray[18]!=null?Integer.parseInt(objectArray[18].toString()):1));
	        		   
	        		   
	        	     return ui;
	        	   }catch(Exception e1){
	        		   log.error("getUserItem:for:error:"+e1);
	        		   e1.printStackTrace(System.out);
	        	   }
	           }  
		   }catch(Exception e){
			   log.error("getUserItem:error:"+e);
			 
		   }
		   return null;
	 }
	 
	 
	 public void createArm(){
		   log.info("AppMyUserCertModifyManager:createArm:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserCertModifyManager:createArm:02:"+sessionId);
	     
	       Long idApp=null;
	       Long idUser=null;
	       int modeExec=0;
	       //0-��������
	       //1-����������
	       
		   try{
			   
			   idApp =  new Long(sessionId); 
			   
			   Object[] app=(Object[]) entityManager.createNativeQuery(
			    			  "select JAS.UP_USER_APP, " +
			    			  "JAS.CERT_VALUE, jas.MODE_EXEC "+
	                          "from JOURN_APP_USER_CERTADD_BSS_T jas "+
	                          "where JAS.ID_SRV=? ")
			    			.setParameter(1, idApp)
			    			.getSingleResult();  
				 
			   idUser=new Long(app[0].toString());
			 
			   modeExec = Integer.parseInt(app[2].toString());
			   
			   DateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

	           String serial = null;
	           String certDate = null;
			   String userFio = null;
			   String depName = null;
			   String orgName = null;
    		   
    		   if(app[1]!=null){
    			   //cert_value
    			  
    			  java.sql.Blob sb = (java.sql.Blob) app[1];
    			   
    			  log.info("AppMyUserCertModifyManager:createArm:02:"+modeExec);
        		  
    			  byte[] cert_byte = Base64.decode(sb.getBytes(1L, (int)sb.length()));
    			  
    			  log.info("AppMyUserCertModifyManager:createArm:03");
    			  
    			  CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
    	          X509Certificate user_cert = (X509Certificate)
    	        		   user_cf.generateCertificate(new ByteArrayInputStream(cert_byte));
    	        
    	           serial = dec_to_hex(user_cert.getSerialNumber());
    	           
    	           /* if(certNumExistCrt(serial)){
	        	   log.info("UsrManager:saveUserCertificate:01_1:return;");
	        	   return false;
	           }*/
    	           
    	           certDate = df.format(user_cert.getNotAfter());
    	           
    	           String subject = user_cert.getSubjectDN().getName();
    	    	   
    	    	   log.info("AppMyUserCertModifyManager:createArm:03:"+subject);
    	    	   
    			   LdapName ldapDN = new LdapName(subject);
    			   
    			   for(Rdn rdn: ldapDN.getRdns()) {
    				  //  System.out.println(rdn.getType() + " -> " + rdn.getValue());
    				    
    				    if("CN".equals(rdn.getType())){
    				    	userFio = (String)rdn.getValue();
    				    }else if("OU".equals(rdn.getType())){
    				    	depName = (String)rdn.getValue();
    				    }else if("O".equals(rdn.getType())){
    				    	orgName = (String)rdn.getValue();
    				    }
    				    
    				}
    			   
    			   if(modeExec==1){
    			   //����������
    				   
	    			   AcUsersCertBssT userCert = new AcUsersCertBssT();
	    	    	   
	    	    	   userCert.setCertNum(serial);
	    	    	   userCert.setCertDate(df.format(user_cert.getNotAfter()));
	    	    	   
	    	    	   //!!!
	    	    	   //��������� ������ �� user_cert.getEncoded(),
	    	    	   //� x509Cert.getBytes
	    	    	   userCert.setCertValue(sb.getBytes(1L, (int)sb.length()));
	    	    	   
	    	    	   userCert.setUserFio(userFio);
				     
	    	    	   userCert.setDepName(depName);
				  
	    	    	   userCert.setOrgName(orgName);
					    
			           userCert.setUpUserRaw(idUser);
	   	   
	   	               userCert.setCreator(getCurrentUser().getBaseId());
	   	               userCert.setCreated(new Date());
	   	   
	   	               entityManager.persist(userCert);
	     
	                   entityManager.flush();
    	    	   
    			   }else{
    				   //��������
    				   
    				   entityManager.createNativeQuery(
							    "delete from AC_USERS_CERT_BSS_T auc " + 
							    "where AUC.UP_USER = ? " +
							    "and upper(AUC.CERT_NUM) = upper(?) ")
				                .setParameter(1, idUser)
				                .setParameter(2, serial)
		                        .executeUpdate();
    				   
    			   }
                 
    		       entityManager.createNativeQuery(
    	 	     		   "update JOURN_APP_USER_CERTADD_BSS_T t1 " +
    	 	     		   "set t1.STATUS=1, t1.UP_USER_EXEC=? " +
    	 	     		   "where t1.ID_SRV=? ")
    	 	     		 .setParameter(1, getCurrentUser().getBaseId())
    	 	     		 .setParameter(2, new Long(sessionId))
    	         	 	 .executeUpdate();
                   
    		   }
			  
		     AppUserCertModifyItem ui = getUserItem(new Long(sessionId));
		     
		     Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserCertModifyManager:createArm:error:"+e);
		   }
	 }
	 
	 public void reject(){
		   log.info("AppMyUserCertModifyManager:reject:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserCertModifyManager:reject:sessionId:"+sessionId);
	     
		   try{
			   
		     entityManager.createNativeQuery(
	 	     		   "update JOURN_APP_USER_CERTADD_BSS_T t1 " +
	 	     		   "set t1.STATUS=2,  t1.REJECT_REASON=?, t1.UP_USER_EXEC=? " +
	 	     		   "where t1.ID_SRV=? ")
	 	     		 .setParameter(1, this.rejectReason)
	 	     		 .setParameter(2, getCurrentUser().getBaseId())
	 	     		 .setParameter(3, new Long(sessionId))
	 	     	 	 .executeUpdate();
		     
             AppUserCertModifyItem ui = getUserItem(new Long(sessionId)); 
		     
		     Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserCertModifyManager:reject:error:"+e);
		   }
	 }
	 
	 public void comment(){
		   log.info("AppMyUserCertModifyManager:comment:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserCertModifyManager:comment:sessionId:"+sessionId);
	     
		   try{
			   
		     entityManager.createNativeQuery(
	 	     		   "update JOURN_APP_USER_CERTADD_BSS_T t1 " +
	 	     		   "set t1.COMMENT_=? " +
	 	     		   "where t1.ID_SRV=? ")
	 	     		 .setParameter(1, this.commentText)
	 	     		 .setParameter(2, new Long(sessionId))
	 	     	 	 .executeUpdate();
		     
           AppUserCertModifyItem ui = getUserItem(new Long(sessionId)); 
		     
		   Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserCertModifyManager:reject:error:"+e);
		   }
	 }
	 
	 //����������� ��������������� �� BaseManager
	 //�� � ������ �� ������ ������ �� ����������� � ���� �� ����� �� ����
	 //� BaseManager.forView() ���� ��� �� ������
	 //� �������� ����� ����� ������� ���������� ����� ��� ���������� ������
	 //� ��������� ��� ��������� � ����
	 public void forView() {
		   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
		  log.info("AppMyUserCertModifyManager:forView:sessionId:"+sessionId);
		   if(sessionId!=null ){
			   
			  AppUserCertModifyItem au = getUserItem(new Long(sessionId)); 
			   
			 /* 
			  //��� �������
			  //��� ���� ����� ��� JournAppUserCertaddBssT
        	 BaseItem au = super.searchBean(sessionId);
        	 
        	try{
        		
        		JournAppUserCertaddBssT jac = entityManager.find(
        				JournAppUserCertaddBssT.class, 
                        new Long(sessionId));
        		
        		if(jac.getCertValue() !=null){
        		
		        		DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
		        		
		     		    String serial = null;
		 	            String certDate = null;
		 			    String userFio = null;
		 			    String depName = null;
		 			    String orgName = null;
		     		   
		     		   
		     			   //cert_value
		     			  
		     			  log.info("AppUserCertModifyManager:forView:02");
			        	
		     			  byte[] cert_byte = Base64.decode(jac.getCertValue());
		     			 
		     			  CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
		     	          X509Certificate user_cert = (X509Certificate)
		     	        		   user_cf.generateCertificate(new ByteArrayInputStream(cert_byte));
		     		   
		     	        
		     	           serial = dec_to_hex(user_cert.getSerialNumber());
		     	           
		     	           certDate = df.format(user_cert.getNotAfter());
		     	           
		     	           String subject = user_cert.getSubjectDN().getName();
		     	    	   
		     	    	   log.info("AppUserCertModifyManager:forView:03:"+subject);
		     	    	   
		     			   LdapName ldapDN = new LdapName(subject);
		     			   
		     			   for(Rdn rdn: ldapDN.getRdns()) {
		     				  //  System.out.println(rdn.getType() + " -> " + rdn.getValue());
		     				    
		     				    if("CN".equals(rdn.getType())){
		     				    	userFio = (String)rdn.getValue();
		     				    }else if("OU".equals(rdn.getType())){
		     				    	depName = (String)rdn.getValue();
		     				    }else if("O".equals(rdn.getType())){
		     				    	orgName = (String)rdn.getValue();
		     				    }
		     				    
		     				}
		     			
		        		((AppUserCertModifyItem)au).setCertNum(serial);
		        		((AppUserCertModifyItem)au).setCertDate(certDate);
		        		((AppUserCertModifyItem)au).setCertOrgName(orgName);
		        		((AppUserCertModifyItem)au).setCertDepName(depName);
		        		((AppUserCertModifyItem)au).setCertUserFio(userFio);
        		
        	  }
        	}catch(Exception e){
        		log.info("AppUserCertModifyManager:forView:04:error"+e);
        	}*/
			 
        	 
        	 
		     Contexts.getEventContext().set("contextBeanView", au);
		  }
	   }
	 
	 public void forViewCrt() {
		   try{
			 String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
		     log.info("AppMyUserCertModifyManager:forViewCrt:01:"+sessionId);
		     
		     AppUserCertModifyItem ui = getUserItem(new Long(sessionId));
	    		
	    	 Contexts.getEventContext().set("contextBeanView", ui);
		     
	    		    	 
		   }catch(Exception e){
			 log.error("AppMyUserCertModifyManager:forViewCrt:Error:"+e);
		   }
	   } 
	 
	 public void forViewUpdDel() {
		 try{
		     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
		     log.info("AppMyUserCertModifyManager:forViewUpdDel:sessionId:"+sessionId);
		     if(sessionId!=null){
		    	
		     	 
		    	 AppUserCertModifyItem ui = getUserItem(new Long(sessionId));
		        	 
		   	 Contexts.getEventContext().set("appMyUserCertModifyBean", ui);
		     }
		   }catch(Exception e){
			   log.error("AppMyUserCertModifyManager:forViewUpdDel:Error:"+e);
		   }
	 }
	 
	 public void forViewComment() {
		   
		   try{
			     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
					        .getRequestParameterMap()
					        .get("sessionId");
			     log.info("AppMyUserCertModifyManager:forViewComment:sessionId:"+sessionId);
			     if(sessionId!=null){
			    	
			     	 
			    	 AppUserCertModifyItem ui = getUserItem(new Long(sessionId));
			    	 
			    	 this.commentText=ui.getComment();

	     }
			}catch(Exception e){
				   log.error("AppMyUserCertModifyManager:forViewComment:Error:"+e);
			}
	   } 
	 
	 public String getRejectReason(){
		 return this.rejectReason;
	 }
	 public void setRejectReason(String rejectReason){
		 this.rejectReason=rejectReason;
	 }
	 
	 public String getCommentText(){
		 return this.commentText;
	 }
	 public void setCommentText(String commentText){
		 this.commentText=commentText;
	 }
	 
	 public List <BaseTableItem> getAuditItemsListSelect() {
		   log.info("getAuditItemsListSelect:01");
		   AppMyUserCertModifyContext ac= new AppMyUserCertModifyContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			   
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("idApp"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("created"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("orgName"));
			  // auditItemsListSelect.add(ac.getAuditItemsMap().get("usrFio"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("statusValue"));
		   }
	       return this.auditItemsListSelect;
  }
  

  
  public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("AppMyUserCertModifyManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   AppMyUserCertModifyContext ac= new AppMyUserCertModifyContext();
		  // auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
		   
	   }
	   return this.auditItemsListContext;
  }
  
  public List<HeaderTableItem> getHeaderItemsListContext() {
	  
	  if(headerItemsListContext==null){
		   AppMyUserCertModifyContext ac= new AppMyUserCertModifyContext();
		//   headerItemsListContext = new ArrayList<BaseTableItem>();
		   headerItemsListContext=ac.getHeaderItemsList();
		   
		/*   
		   AppUserItem ui = (AppUserItem)
					  Component.getInstance("contextBeanView",ScopeType.EVENT); 
		   
		   log.info("AppUserManager:getHeaderItemsListContext:01");
		   
		   if(ui!=null){
			   log.info("AppUserManager:getHeaderItemsListContext:ui.getStatus():"+ui.getStatus());
			   if(ui.getStatus()!=2){
				   log.info("AppUserManager:getHeaderItemsListContext:03:"+headerItemsListContext.get(2).getItems().g);
				   headerItemsListContext.get(2).getItems().remove("rejectReason");
			   }
		   }*/
		   
	   }
	  
	   return this.headerItemsListContext;
  }
  
  
  public List<HeaderTableItem> getHeaderItemsListContext(String ids) {
	  
	 	AppMyUserCertModifyContext ac= new AppMyUserCertModifyContext();
		
	 	if(ids!=null) {
	 		
	 	
	 		headerItemsListContext=new ArrayList<HeaderTableItem>();
	 				
	 	    //List<String> idsList = Arrays.asList(ids);
	 	
	 	     List<String> idsList =  Arrays.asList(ids.split(","));
	 	   
	    	for(HeaderTableItem hti :ac.getHeaderItemsList()){
			
			 
			
			  if(idsList.contains(hti.getItemField())){
				  headerItemsListContext.add(hti);
			  }
			  
		   }

	 	}
	   return this.headerItemsListContext;
 }
  
  public List<HeaderTableItem> getHeaderItemsListContextCREATE(String ids) {
	  
	 	AppMyUserCertModifyContext ac= new AppMyUserCertModifyContext();
		
	 	if(ids!=null) {
	 		
	 	
	 		headerItemsListContextCREATE=new ArrayList<HeaderTableItem>();
	 				
	 	    //List<String> idsList = Arrays.asList(ids);
	 	
	 	     List<String> idsList =  Arrays.asList(ids.split(","));
	 	   
	    	for(HeaderTableItem hti :ac.getHeaderItemsList()){
			
			 
			
			  if(idsList.contains(hti.getItemField())){
				  headerItemsListContextCREATE.add(hti);
			  }
			  
		   }

	 	}
	   return this.headerItemsListContextCREATE;
}
  private static String dec_to_hex(BigInteger bi) {
		
		String result = null;
		
		try
		{
		 result = bi.toString(16);
	     System.out.println("num_convert:num:"+result);
		}
		catch (NumberFormatException e)
		{
		     System.out.println("Error! tried to parse an invalid number format");
		}
		 return result;
		}
}
