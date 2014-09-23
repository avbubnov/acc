package iac.grn.infosweb.context.appmy.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;


import iac.cud.infosweb.dataitems.AppUserItem;
import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.UserItem;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.IspBssT;
import iac.cud.infosweb.util.BaseUtil;
import iac.grn.infosweb.context.mc.arm.ArmManager;
import iac.grn.infosweb.context.mc.clorg.ClOrgManager;
import iac.grn.infosweb.context.mc.clusr.ClUsrManager;
import iac.grn.infosweb.context.mc.usr.UsrManager;
import iac.grn.infosweb.session.table.BaseDataModel;
import iac.grn.infosweb.session.table.BaseManager;
import iac.grn.serviceitems.BaseTableItem;
import iac.grn.serviceitems.HeaderTableItem;

@Name("appMyUserManager")
public class AppMyUserManager extends BaseManager{

	
	private String rejectReason;
	private String commentText;
	
	public void invokeLocal(String type, int firstRow, int numberOfRows,
	           String sessionId) {
		
		 log.info("hostsManager:invokeLocal");
		 try{
			 String orderQuery=null;
			 log.info("hostsManager:invokeLocal");
			 
			 AppMyUserStateHolder appMyUserStateHolder = (AppMyUserStateHolder)
					  Component.getInstance("appMyUserStateHolder",ScopeType.SESSION);
			 HashMap<String, String> filterMap = appMyUserStateHolder.getColumnFilterValues();
			 String st=null;
			  
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				 Set<Map.Entry<String, String>> set = appMyUserStateHolder.getSortOrders().entrySet();
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
    	        	   //делаем фильтр на начало  
    	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	   
    	   		     }else if(me.getKey().equals("t1_iogv_bind_type")&&(me.getValue()!=null && me.getValue().equals("-2"))){
    	    	    	 
    	    	    	 st=(st!=null?st+" and " :"")+" t1_usr_code is null ";
    	    	    	 
    	    	     }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	        	  }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);

             
               List<Object[]> lo=null;
               AppUserItem ui = null;
               DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
               

             lo=entityManager.createNativeQuery(
             "select t1.t1_id, t1.t1_created, t1.t1_status, t1_org_name,  t1_user_fio, t1_reject_reason, t1_comment, "+
                  "t1_SURNAME_USER, "+
                  "t1_NAME_USER, "+
                  "t1_PATRONYMIC_USER, "+
                  "t1_SIGN_USER, "+ 
                  "t1_POSITION_USER, "+
                  "t1_EMAIL_USER, "+ 
                  "t1_PHONE_USER, "+
                  "t1_CERTIFICATE_USER, "+ 
                  "t1_NAME_DEPARTAMENT, "+ 
                  "t1_NAME_ORG, "+ 
                  "t1_SIGN_ORG, "+
              "t1_user_id, t1_user_login, " +
              "t1_comment_app "+
              "from( "+ 
             "select JAS.ID_SRV t1_id, JAS.CREATED t1_created, "+
                  "JAS.SURNAME_USER t1_SURNAME_USER, "+
                  "JAS.NAME_USER t1_NAME_USER, "+
                  "JAS.PATRONYMIC_USER t1_PATRONYMIC_USER, "+
                  "JAS.SIGN_USER t1_SIGN_USER, "+
                  "JAS.POSITION_USER t1_POSITION_USER, "+
                  "JAS.EMAIL_USER t1_EMAIL_USER, "+ 
                  "JAS.PHONE_USER t1_PHONE_USER, "+
                  "JAS.CERTIFICATE_USER t1_CERTIFICATE_USER, "+ 
                  "JAS.NAME_DEPARTAMENT t1_NAME_DEPARTAMENT, "+ 
                  "JAS.NAME_ORG t1_NAME_ORG, "+ 
                  "JAS.SIGN_ORG t1_SIGN_ORG, "+
                  "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
              "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
              "JAS.REJECT_REASON t1_reject_reason, "+ 
              "au_APP.ID_SRV t1_user_id, AU_APP.LOGIN t1_user_login, " +
              "JAS.COMMENT_ t1_comment, " +
              "JAS.COMMENT_APP t1_comment_app "+
             "from JOURN_APP_USER_BSS_T jas, "+
               "AC_USERS_KNL_T au_FULL, "+  
                "ISP_BSS_T cl_org_full, "+
                 "ISP_BSS_T cl_usr_full, "+
                  "AC_USERS_KNL_T au_APP, "+
              "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE "+ 
                "from ISP_BSS_T cl_org "+
                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
                "group by CL_ORG.SIGN_OBJECT) t03, "+
                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
                            "from ISP_BSS_T cl_usr "+
                            "where CL_USR.FIO is not null "+
                            "group by CL_usr.SIGN_OBJECT) t02 "+  
                "where JAS.UP_USER=AU_FULL.ID_SRV "+
                "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
                "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
                "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
                "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
               "and au_APP.ID_SRV(+) =JAS.UP_USER_APP "+
               "and JAS.UP_USER= :idUser "+ 
               
               ") t1"+
              (st!=null ? " where "+st :" ")+
              (orderQuery!=null ? orderQuery+", t1_id desc " : " order by t1_id desc "))
              .setFirstResult(firstRow)
              .setMaxResults(numberOfRows)
              .setParameter("idUser", getCurrentUser().getBaseId())
              .getResultList();
               auditList = new ArrayList<BaseItem>();
               
               for(Object[] objectArray :lo){
            	   try{
            		 ui= new AppUserItem(
            				 (objectArray[0]!=null?new Long(objectArray[0].toString()):null),
            				 (objectArray[1]!=null?df.format((Date)objectArray[1]) :""),
            				 (objectArray[2]!=null?Integer.parseInt(objectArray[2].toString()):0),	
            				 (objectArray[3]!=null?objectArray[3].toString():""),
            				 (objectArray[4]!=null?objectArray[4].toString():""),
            				 (objectArray[5]!=null?objectArray[5].toString():""),
            				 (objectArray[6]!=null?objectArray[6].toString():""),
            				 
            				 (objectArray[7]!=null?objectArray[7].toString():""),
            				 (objectArray[8]!=null?objectArray[8].toString():""),
            				 (objectArray[9]!=null?objectArray[9].toString():""),
            				 (objectArray[10]!=null?objectArray[10].toString():""),
            				 (objectArray[11]!=null?objectArray[11].toString():""),
            				 (objectArray[12]!=null?objectArray[12].toString():""),
            				 (objectArray[13]!=null?objectArray[13].toString():""),
            				 (objectArray[14]!=null?objectArray[14].toString():""),
            				 (objectArray[15]!=null?objectArray[15].toString():""),
            				 (objectArray[16]!=null?objectArray[16].toString():""),
            				 (objectArray[17]!=null?objectArray[17].toString():""),
            				 (objectArray[18]!=null?new Long(objectArray[18].toString()):null),
              			     (objectArray[19]!=null?objectArray[19].toString():""),
              			     (objectArray[20]!=null?objectArray[20].toString():"")
            				);  
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
    	        		//делаем фильтр на начало
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
							"from( "+ 
				             "select JAS.ID_SRV t1_id, JAS.CREATED t1_created, "+
				                  "JAS.SURNAME_USER t1_SURNAME_USER, "+
				                  "JAS.NAME_USER t1_NAME_USER, "+
				                  "JAS.PATRONYMIC_USER t1_PATRONYMIC_USER, "+
				                  "JAS.SIGN_USER t1_SIGN_USER, "+
				                  "JAS.POSITION_USER t1_POSITION_USER, "+
				                  "JAS.EMAIL_USER t1_EMAIL_USER, "+ 
				                  "JAS.PHONE_USER t1_PHONE_USER, "+
				                  "JAS.CERTIFICATE_USER t1_CERTIFICATE_USER, "+ 
				                  "JAS.NAME_DEPARTAMENT t1_NAME_DEPARTAMENT, "+ 
				                  "JAS.NAME_ORG t1_NAME_ORG, "+ 
				                  "JAS.SIGN_ORG t1_SIGN_ORG, "+
				                  "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
				              "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
				              "JAS.REJECT_REASON t1_reject_reason, "+ 
				              "au_APP.ID_SRV t1_user_id, AU_APP.LOGIN t1_user_login, "+
				              "JAS.COMMENT_ t1_comment "+
				             "from JOURN_APP_USER_BSS_T jas, "+
				               "AC_USERS_KNL_T au_FULL, "+  
				                "ISP_BSS_T cl_org_full, "+
				                 "ISP_BSS_T cl_usr_full, "+
				                  "AC_USERS_KNL_T au_APP, "+
				              "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE "+ 
				                "from ISP_BSS_T cl_org "+
				                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
				                "group by CL_ORG.SIGN_OBJECT) t03, "+
				                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
				                            "from ISP_BSS_T cl_usr "+
				                            "where CL_USR.FIO is not null "+
				                            "group by CL_usr.SIGN_OBJECT) t02 "+  
				                "where JAS.UP_USER=AU_FULL.ID_SRV "+
				                "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
				                "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
				                "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
				                "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
				               "and au_APP.ID_SRV(+) =JAS.UP_USER_APP "+
				               "and JAS.UP_USER= :idUser "+ 
				               
				               ") t1"+
		         (st!=null ? " where "+st :" "))
		       .setParameter("idUser", getCurrentUser().getBaseId())
               .getSingleResult()).longValue();
                 
                 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if(type.equals("bean")){
				 
			 }
		}catch(Exception e){
			  log.error("invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("Ошибка!");
		}

	}
	
	 private AppUserItem getUserItem(Long idUser){
		 if(idUser==null){
			  return null;
		   }
		   
		   try{
	           List<Object[]> lo=null;
	           AppUserItem ui = null;
	           DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm:ss");
	           
	           lo=entityManager.createNativeQuery(
	        		   "select t1.t1_id, t1.t1_created, t1.t1_status, t1_org_name,  t1_user_fio, t1_reject_reason, t1_comment, "+
	        	                  "t1_SURNAME_USER, "+
	        	                  "t1_NAME_USER, "+
	        	                  "t1_PATRONYMIC_USER, "+
	        	                  "t1_SIGN_USER, "+ 
	        	                  "t1_POSITION_USER, "+
	        	                  "t1_EMAIL_USER, "+ 
	        	                  "t1_PHONE_USER, "+
	        	                  "t1_CERTIFICATE_USER, "+ 
	        	                  "t1_NAME_DEPARTAMENT, "+ 
	        	                  "t1_NAME_ORG, "+ 
	        	                  "t1_SIGN_ORG, "+
	        	              "t1_user_id, t1_user_login, "+
	        	              "t1_comment_app "+
	        	              "from( "+ 
	        	             "select JAS.ID_SRV t1_id, JAS.CREATED t1_created, "+
	        	                  "JAS.SURNAME_USER t1_SURNAME_USER, "+
	        	                  "JAS.NAME_USER t1_NAME_USER, "+
	        	                  "JAS.PATRONYMIC_USER t1_PATRONYMIC_USER, "+
	        	                  "JAS.SIGN_USER t1_SIGN_USER, "+
	        	                  "JAS.POSITION_USER t1_POSITION_USER, "+
	        	                  "JAS.EMAIL_USER t1_EMAIL_USER, "+ 
	        	                  "JAS.PHONE_USER t1_PHONE_USER, "+
	        	                  "JAS.CERTIFICATE_USER t1_CERTIFICATE_USER, "+ 
	        	                  "JAS.NAME_DEPARTAMENT t1_NAME_DEPARTAMENT, "+ 
	        	                  "JAS.NAME_ORG t1_NAME_ORG, "+ 
	        	                  "JAS.SIGN_ORG t1_SIGN_ORG, "+
	        	                  "JAS.STATUS t1_status,  CL_ORG_FULL.FULL_ t1_org_name, "+
	        	              "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO ) t1_user_fio, "+
	        	              "JAS.REJECT_REASON t1_reject_reason, "+ 
	        	              "au_APP.ID_SRV t1_user_id, AU_APP.LOGIN t1_user_login, "+
	        	              "JAS.COMMENT_ t1_comment, "+
	        	              "JAS.COMMENT_APP t1_comment_app "+
	        	             "from JOURN_APP_USER_BSS_T jas, "+
	        	               "AC_USERS_KNL_T au_FULL, "+  
	        	                "ISP_BSS_T cl_org_full, "+
	        	                 "ISP_BSS_T cl_usr_full, "+
	        	                  "AC_USERS_KNL_T au_APP, "+
	        	              "(select max(CL_ORG.ID_SRV) CL_ORG_ID,  CL_ORG.SIGN_OBJECT  CL_ORG_CODE "+ 
	        	                "from ISP_BSS_T cl_org "+
	        	                "where  CL_ORG.SIGN_OBJECT LIKE '%00000' "+
	        	                "group by CL_ORG.SIGN_OBJECT) t03, "+
	        	                 "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+ 
	        	                            "from ISP_BSS_T cl_usr "+
	        	                            "where CL_USR.FIO is not null "+
	        	                            "group by CL_usr.SIGN_OBJECT) t02 "+  
	        	                "where JAS.UP_USER=AU_FULL.ID_SRV "+
	        	                "and AU_FULL.UP_SIGN=t03.CL_ORG_CODE "+
	        	                "and CL_ORG_FULL.ID_SRV=t03.CL_ORG_ID "+
	        	                "and AU_FULL.UP_SIGN_USER=t02.CL_USR_CODE(+) "+
	        	                "and CL_USR_FULL.ID_SRV(+)=t02.CL_USR_ID "+
	        	               "and au_APP.ID_SRV(+) =JAS.UP_USER_APP " +
	        	               "and JAS.ID_SRV=? "+
	        	               ") t1")
	         .setParameter(1, idUser)
	         .getResultList();
	           
	           for(Object[] objectArray :lo){
	        	   try{
	        		   log.info("AppMyUserManager:getUserItem:login:"+objectArray[1].toString());
	        		   
	        		   ui= new AppUserItem(
	            				 (objectArray[0]!=null?new Long(objectArray[0].toString()):null),
	            				 (objectArray[1]!=null?df.format((Date)objectArray[1]) :""),
	            				 (objectArray[2]!=null?Integer.parseInt(objectArray[2].toString()):0),	
	            				 (objectArray[3]!=null?objectArray[3].toString():""),
	            				 (objectArray[4]!=null?objectArray[4].toString():""),
	            				 (objectArray[5]!=null?objectArray[5].toString():""),
	            				 (objectArray[6]!=null?objectArray[6].toString():""),
	            				 
	            				 (objectArray[7]!=null?objectArray[7].toString():""),
	            				 (objectArray[8]!=null?objectArray[8].toString():""),
	            				 (objectArray[9]!=null?objectArray[9].toString():""),
	            				 (objectArray[10]!=null?objectArray[10].toString():""),
	            				 (objectArray[11]!=null?objectArray[11].toString():""),
	            				 (objectArray[12]!=null?objectArray[12].toString():""),
	            				 (objectArray[13]!=null?objectArray[13].toString():""),
	            				 (objectArray[14]!=null?objectArray[14].toString():""),
	            				 (objectArray[15]!=null?objectArray[15].toString():""),
	            				 (objectArray[16]!=null?objectArray[16].toString():""),
	            				 (objectArray[17]!=null?objectArray[17].toString():""),
	            				 (objectArray[18]!=null?new Long(objectArray[18].toString()):null),
	              			     (objectArray[19]!=null?objectArray[19].toString():""),
	              			     (objectArray[20]!=null?objectArray[20].toString():"")
	            				);  
	        	     return ui;
	        	   }catch(Exception e1){
	        		   log.error("getUserItem:for:error:"+e1);
	        	   }
	           }  
		   }catch(Exception e){
			   log.error("getUserItem:error:"+e);
		   }
		   return null;
	 }
	 
	 
	 public void createArm(){
		   log.info("AppMyUserManager:createArm:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserManager:createArm:sessionId:"+sessionId);
	     
		   try{
			   
			   
			 //armBeanCrt.setIdAppSys(new Long(sessionId));
			 
			 UsrManager usrManager = (UsrManager)
			          Component.getInstance("usrManager", ScopeType.EVENT);
		   
		     usrManager.addUsr();
		   
		     AcUser usrBeanCrt = (AcUser)
					  Component.getInstance("usrBeanCrt",ScopeType.CONVERSATION);  
		     
		     entityManager.createNativeQuery(
	 	     		   "update JOURN_APP_USER_BSS_T t1 " +
	 	     		   "set t1.UP_USER_APP=?, t1.STATUS=1, t1.UP_USER_EXEC=? " +
	 	     		   "where t1.ID_SRV=? ")
	 	     		 .setParameter(1, usrBeanCrt.getBaseId())
	 	     		 .setParameter(2, getCurrentUser().getBaseId())
	 	     		 .setParameter(3, new Long(sessionId))
	         	 	 .executeUpdate();
		    
		     AppUserItem ui = getUserItem(new Long(sessionId));
		     
		     Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserManager:createArm:error:"+e);
		   }
	 }
	 
	 public void reject(){
		   log.info("AppMyUserManager:reject:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserManager:reject:sessionId:"+sessionId);
	     
		   try{
			   
		     entityManager.createNativeQuery(
	 	     		   "update JOURN_APP_USER_BSS_T t1 " +
	 	     		   "set t1.STATUS=2,  t1.REJECT_REASON=?, t1.UP_USER_EXEC=? " +
	 	     		   "where t1.ID_SRV=? ")
	 	     		 .setParameter(1, this.rejectReason)
	 	     		 .setParameter(2, getCurrentUser().getBaseId())
	 	     		 .setParameter(3, new Long(sessionId))
	 	     	 	 .executeUpdate();
		     
             AppUserItem ui = getUserItem(new Long(sessionId)); 
		     
		     Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserManager:reject:error:"+e);
		   }
	 }
	 
	 public void comment(){
		   log.info("AppMyUserManager:comment:01");
		  
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	       log.info("AppMyUserManager:comment:sessionId:"+sessionId);
	     
		   try{
			   
		     entityManager.createNativeQuery(
	 	     		   "update JOURN_APP_USER_BSS_T t1 " +
	 	     		   "set t1.COMMENT_=? " +
	 	     		   "where t1.ID_SRV=? ")
	 	     		 .setParameter(1, this.commentText)
	 	     		 .setParameter(2, new Long(sessionId))
	 	     	 	 .executeUpdate();
		     
           AppUserItem ui = getUserItem(new Long(sessionId)); 
		     
		   Contexts.getEventContext().set("contextBeanView", ui);
		     
		   }catch(Exception e){
			   log.error("AppMyUserManager:reject:error:"+e);
		   }
	 }
	 
	 public void forViewCrt() {
		 
		  IspBssT clUsrBean = null;
		  IspBssT clOrgBean = null; 		 
		  try{
			 String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
		     log.info("AppMyUserManager:forViewCrt:sessionId:"+sessionId);
		     
		     AppUserItem ui = getUserItem(new Long(sessionId));
	    
		     ClUsrManager clUsrManager = (ClUsrManager)
                    Component.getInstance("clUsrManager", ScopeType.EVENT);
	    
		     ClOrgManager clOrgManager = (ClOrgManager)
                    Component.getInstance("clOrgManager", ScopeType.EVENT);
	    
		    
		    log.info("AppMyUserManager:forViewCrt:IogvCodeUser:"+ui.getIogvCodeUser());
		     
		    if(ui.getIogvCodeUser()!=null&&ui.getIogvCodeUser().length()==8){ //в заявке корректный код иогв пользователя
		    	
		      log.info("AppMyUserManager:forViewCrt:01");
		    	 
		      //берём из него код организации
		      clOrgManager.forViewAutocomplete(ui.getIogvCodeUser().substring(0, 3)+"00000");
		    	 
		      clOrgBean = (IspBssT)
		                    Component.getInstance("clOrgBean", ScopeType.EVENT);
				    
			  if(clOrgBean.getBaseId()!=null){ //нашли действующую организацию
					
				    ui.setNameOrg(clOrgBean.getFull());
				    
				    log.info("AppMyUserManager:forViewCrt:02:"+clOrgBean.getFull());
				    
				    //проводим поиск действующего пользователя
				    clUsrManager.forViewAutocomplete(ui.getIogvCodeUser());
				      
				    clUsrBean = (IspBssT)
			                    Component.getInstance("clUsrBean", ScopeType.EVENT);
					    
					  //  log.info("AppMyUserManager:forViewCrt:clOrgBean.BaseId:"+clOrgBean.getBaseId());
					    
				    if(clUsrBean.getBaseId()!=null){ //нашли действующего пользователя
						  
						    log.info("AppMyUserManager:forViewCrt:03");
						  
						    ui.setFioIogvUser(clUsrBean.getFio());
				    }
			  }else{
				  //если из кода пользователя нет дйствующей организации,
				  //то поиск действующего пользователя не проводим
			  }

		      
		    }
		    //в итоге имеем: 
		    //1) организация + пользователь :
		    //   clOrgBean!=null && clOrgBean.getBaseId()!=null  && clUsrBean!= null && clUsrBean.getBaseId()!=null
		    //2) организация  :
		    //   clOrgBean!=null && clOrgBean.getBaseId()!=null && clUsrBean!= null && clUsrBean.getBaseId()==null
		    //3) ничего
		    //   clOrgBean!=null && clOrgBean.getBaseId()==null && clUsrBean==null
		    
		    log.info("AppMyUserManager:forViewCrt:04");
		    
		    if((clOrgBean==null||clOrgBean.getBaseId()==null)&&ui.getIogvCodeOrg()!=null&&ui.getIogvCodeOrg().length()==8){
		     //нет корректного кода иогв пользователя или не нашли действующую организацию
		     //но при этом есть корректный код иогв организации
		    	
		    	log.info("AppMyUserManager:forViewCrt:05");
		    	  
		      clOrgManager.forViewAutocomplete(ui.getIogvCodeOrg());
		   
		    
		      clOrgBean = (IspBssT)
                    Component.getInstance("clOrgBean", ScopeType.EVENT);
		    
		  //  log.info("AppMyUserManager:forViewCrt:clOrgBean.BaseId:"+clOrgBean.getBaseId());
		    
		      if(clOrgBean.getBaseId()!=null){
		    	  
		    	 ui.setNameOrg(clOrgBean.getFull());
		    	
		    	 log.info("AppMyUserManager:forViewCrt:06:"+clOrgBean.getFull());
		      }
		    }
		    
		    log.info("AppMyUserManager:forViewCrt:07");
		    
		    ui.setPasswordTechUser(BaseUtil.password());
		    
		    Contexts.getEventContext().set("contextBeanView", ui);
		    
		   
		    
		    
		   /* IspBssT ao = new IspBssT();
	    		
	    	 ao.setFull(ui.getNameOrg());
	    	 ao.setFio(ui.getSurnameUser()+" "+ui.getNameUser()+" "+ui.getPatronymicUser());
	    	 
		    Contexts.getEventContext().set("clOrgBean", ao);
		    
		    AcUser au = new AcUser();
		    
		    au.setCertificate(ui.getCertificateUser());
		    
		    Contexts.getEventContext().set("usrBeanCrt", au);*/
		    
		   }catch(Exception e){
			 log.error("AppMyUserManager:forViewCrt:Error:"+e);
		   }
	   } 
	 
	
	 
	 public void forViewUpdDel() {
		 try{
		     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
		     log.info("AppMyUserManager:forViewUpdDel:sessionId:"+sessionId);
		     if(sessionId!=null){
		    	
		     	 
		    	 AppUserItem ui = getUserItem(new Long(sessionId));
		        	 
		   	 Contexts.getEventContext().set("appMyUserBean", ui);
		     }
		   }catch(Exception e){
			   log.error("AppMyUserManager:forViewUpdDel:Error:"+e);
		   }
	 }
	 
	 public void forViewComment() {
		   
		   try{
			     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
					        .getRequestParameterMap()
					        .get("sessionId");
			     log.info("AppMyUserManager:forViewComment:sessionId:"+sessionId);
			     if(sessionId!=null){
			    	
			     	 
			    	 AppUserItem ui = getUserItem(new Long(sessionId));
			    	 
			    	 this.commentText=ui.getComment();

	     }
			}catch(Exception e){
				   log.error("AppMyUserManager:forViewComment:Error:"+e);
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
		   AppMyUserContext ac= new AppMyUserContext();
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
	   log.info("AppMyUserManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   AppMyUserContext ac= new AppMyUserContext();
		  // auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
		   
	   }
	   return this.auditItemsListContext;
  }
  
  public List<HeaderTableItem> getHeaderItemsListContext() {
	  
	  if(headerItemsListContext==null){
		   AppMyUserContext ac= new AppMyUserContext();
		//   headerItemsListContext = new ArrayList<BaseTableItem>();
		   headerItemsListContext=ac.getHeaderItemsList();
		   
		/*   
		   AppSystemItem ui = (AppSystemItem)
					  Component.getInstance("contextBeanView",ScopeType.EVENT); 
		   
		   log.info("AppSystemManager:getHeaderItemsListContext:01");
		   
		   if(ui!=null){
			   log.info("AppSystemManager:getHeaderItemsListContext:ui.getStatus():"+ui.getStatus());
			   if(ui.getStatus()!=2){
				   log.info("AppSystemManager:getHeaderItemsListContext:03:"+headerItemsListContext.get(2).getItems().g);
				   headerItemsListContext.get(2).getItems().remove("rejectReason");
			   }
		   }*/
		   
	   }
	  
	   return this.headerItemsListContext;
  }
}
