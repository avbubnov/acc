package iac.grn.infosweb.context.mc.armsub;

import org.apache.xml.security.utils.Base64;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;
//import org.picketlink.common.util.Base64;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.SystemCertItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcSubsystemCertBssT;
import iac.cud.infosweb.entity.AcUser;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.infosweb.session.cache.CacheManager;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jboss.seam.Component;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;

import iac.grn.ramodule.entity.VAuditReport;
import iac.grn.serviceitems.BaseTableItem;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;

/**
 * Управляющий Бин
 * @author bubnov
 *
 */
@Name("armSubManager")
public class ArmSubManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * Экспортируемая сущности 
     * для отображения
     */
	//private BaseItem usrBean;             !!! Проверить !!!
	
	private String dellMessage=null;
	private int delNot=0;
	
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private List<AcSubsystemCertBssT> listArmSub = null;
	
	private boolean armSubCodeExist=false;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> armSubListCached = (List<BaseItem>)
			  Component.getInstance("armSubListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    armSubListCached!=null){
		 	//	log.info("getAuditList:02:"+orgListCached.size());
			    	this.auditList=armSubListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("armSubListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecArmSub = (ArrayList<String>)
					  Component.getInstance("selRecArmSub",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecArmSub!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecArmSub.contains(it.getBaseId().toString())){
					// log.info("invoke:Selected!!!");
					 it.setSelected(true);
				   }else{
					  it.setSelected(false);
				   }
				 }
		    }
		}
		return this.auditList;
	}
	public void setAuditList(List<BaseItem> auditList){
		this.auditList=auditList;
	}
	public void invokeLocal(String type, int firstRow, int numberOfRows,
	           String sessionId) {
		try{
			 String orderQuery=null;
			 log.info("hostsManager:invokeLocal");
			 
			 ArmSubStateHolder orgStateHolder = (ArmSubStateHolder)
					  Component.getInstance("armSubStateHolder",ScopeType.SESSION);
			 
			 HashMap<String, String> filterMap = orgStateHolder.getColumnFilterValues();
			 String st=null;
			 
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				
				 Set<Map.Entry<String, String>> set = orgStateHolder.getSortOrders().entrySet();
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
    	            
    	            	/*if(me.getKey().equals("dateAction")){  
    	    	        	// st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	    	   		     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	    	}else{*/
    	   		       if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	                  }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);
                 
				// auditList = new ArrayList<BaseItem>();
				 auditList = entityManager.createQuery("select o from AcSubsystemCertBssT o "+
						 (st!=null ? " where "+st :"")+
						 (orderQuery!=null ? orderQuery+", o.idSrv " : " order by o.idSrv "))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 
				 if(filterMap!=null){
    	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
    	              for (Map.Entry<String, String> me : set_filter) {
    	            	  log.info("me.getKey+:"+me.getKey());
    	            	  log.info("me.getValue:"+me.getValue());
    	            
    	            	/*if(me.getKey().equals("dateAction")){  
    	    	        	// st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('%"+me.getValue()+"%') ";
    	    	   		     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
    	    	    	}else{*/
    	   		       if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	               }
    	              }
    	    	   }
                 log.info("invokeLocal:count:filterQuery:"+st);
				 
				 
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +  
				         "from AcSubsystemCertBssT o "+
				         (st!=null ? " where "+st :""))
		                .getSingleResult();
				 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if(type.equals("bean")){
				 
			 }
		}catch(Exception e){
			  log.error("invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("Ошибка!");
		}
	}
	  /**
	  * Подготовка сущности Аудит УФМС 
	  * для последующих операций просмотра
	  */
   public void forView(String modelType) {
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	  log.info("forView:sessionId:"+sessionId);
	  log.info("forView:modelType:"+modelType);
	  if(sessionId!=null /*&& usrBean==null*/){
		  
		    String service="";
			if(modelType==null){
		    	return ;
		    }
			if(modelType.equals("armSubDataModel")){
				//service=ServiceReestr.Repos;
			}  
		//  invoke("bean", 0, 0, sessionId, service);
		//  Contexts.getEventContext().set("logContrBean", logContrBean);
	
		 /* 
	 	 List<AcUser> usrListCached = (List<AcUser>)
				  Component.getInstance("usrListCached",ScopeType.SESSION);
		  if(usrListCached!=null){
			 for(AcUser it : usrListCached){
				 
				 log.info("forView_inside_for");
				 
				 if(it.getBaseId().toString().equals(sessionId)){
					 log.info("forView_Achtung!!!");
					// this.usrBean=it;
					// Contexts.getEventContext().set("usrBean", usrBean);
					 Contexts.getEventContext().set("usrBean", it);
					 return;
				 }
			 }
		 }*/
		 AcSubsystemCertBssT ar = searchBean(sessionId);
		 
		 if(!isAllowedSys(new Long(sessionId))){
			 log.info("armSubManager:forView:02");
			 ar.setIsAllowedSys(false);
		 }		 
		 
		 Contexts.getEventContext().set("armSubBean", ar);
		 
		 forViewCert();
	  }
   }
   
   public boolean isAllowedSys(Long idSub){
	   log.info("armSubManager:isAllowedSys:01:"+idSub);
	 
	   boolean result = false;
	   
	   try {
	   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
  		 
   		  if(au.getAllowedSys()!=null){
   		   
   			//список ИС в группе, которых нет в привязке к пользователю  
		    List<Object> list= entityManager.createNativeQuery(
		    		
			    		"SELECT SUB.UP_IS " + 
			    		"    FROM AC_SUBSYSTEM_CERT_BSS_T sub " + 
			    		"   WHERE   sub.ID_SRV = :idSub " + 
			    		"         AND  SUB.UP_IS NOT IN (:idsArm) " + 
			    		"GROUP BY SUB.UP_IS")
                      .setParameter("idSub", idSub)
                      .setParameter("idsArm", au.getAllowedSys())
                      .getResultList();
		    
		    //если нет групп, которые не привязаны к пользователю,
		    //тогда у пользователя есть доступ к этой группе
		    //в т. ч. пустые группы
		    if(list.isEmpty()){
		    	result = true;
		    }
		    
	    }else{
	    	result = true;
	    }
	   
	   }catch (Exception e) {
	       log.error("armSubManager:isAllowedSys:ERROR:"+e);
	    }
	   
	   log.info("armSubManager:isAllowedSys:02:"+result);
	   
	   return result;
  }
   
   private AcSubsystemCertBssT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<AcSubsystemCertBssT> armSubListCached = (List<AcSubsystemCertBssT>)
				  Component.getInstance("armSubListCached",ScopeType.SESSION);
		if(armSubListCached!=null){
			for(AcSubsystemCertBssT it : armSubListCached){
				 
			// log.info("searchBean_inside_for");
			  if(it.getBaseId().toString().equals(sessionId)){
					 log.info("searchBean_Achtung!!!");
					 return it;
			  }
			}
		 }
	   }
	   return null;
    }
    public Long getAuditCount(){
	   log.info("getAuditCount");
	 
	   invokeLocal("count",0,0,null);
	  
	   return auditCount;
	  // FacesMessages.instance().add("Ошибка доступа к серверу xxx.xxx.x.xxx!");
   }
   
   public void addArmSub(){
	   log.info("armSubManager:addOrg:01");
	   
	   AcSubsystemCertBssT armSubBeanCrt = (AcSubsystemCertBssT)
				  Component.getInstance("armSubBeanCrt",ScopeType.CONVERSATION);
	   
	   if(armSubBeanCrt==null){
		   return;
	   }
	 
	   try {
		  
		 if(!armSubCodeExistCrt(armSubBeanCrt.getAcIsBssTLong(), armSubBeanCrt.getSubsystemCode().trim())){
		   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		  
		  armSubBeanCrt.setSubsystemName(armSubBeanCrt.getSubsystemName().trim());
		  armSubBeanCrt.setSubsystemCode(armSubBeanCrt.getSubsystemCode().trim());
		  /*
		  if(armSubBeanCrt.getCertAlias()!=null&&!armSubBeanCrt.getCertAlias().trim().equals("")){
			  armSubBeanCrt.setCertAlias(armSubBeanCrt.getCertAlias().trim());
		  }else{
			  armSubBeanCrt.setCertAlias(null);
		  }*/
		  
		  armSubBeanCrt.setCreator(au.getIdUser());
		  armSubBeanCrt.setCreated(new Date());
	      entityManager.persist(armSubBeanCrt);
	    	
	    
	     // audit(ResourcesMap.IS, ActionsMap.CREATE); 
	      
	    }
	      
	    }catch (Exception e) {
	       log.error("armSubManager:addArmSub:ERROR:"+e);
	    }
	   
   }
   
   public void updArmSub(){
	   
	   log.info("armSubManager:updArmSub:01");
	   
	   AcSubsystemCertBssT armSubBean = ( AcSubsystemCertBssT)
				  Component.getInstance("armSubBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("armSubManager:updArmSub:sessionId:"+sessionId);
	
	   if(armSubBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   
		 if(!armSubCodeExistUpd(armSubBean.getAcIsBssTLong(), armSubBean.getSubsystemCode().trim(), new Long(sessionId))){  
		
		 AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		  AcSubsystemCertBssT aam = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
		  
		  aam.setSubsystemName(armSubBean.getSubsystemName().trim());
		  aam.setSubsystemCode(armSubBean.getSubsystemCode().trim());
		  
		  aam.setAcIsBssTLong(armSubBean.getAcIsBssTLong());
			  
		  aam.setModificator(au.getIdUser());
		  aam.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	      Contexts.getEventContext().set("armSubBean", aam);
		
	    //  audit(ResourcesMap.IS, ActionsMap.UPDATE); 
	      
		 }
		 
	     }catch (Exception e) {
           log.error("armSubManager:updSrm:ERROR:"+e);
         }
   }
   
    public void saveArmSubCertificate(byte[] file_byte, Long id_sys){
	   
	   log.info("armSubManager:saveArmSubCertificate:01:"+(file_byte!=null));
	   log.info("armSubManager:saveArmSubCertificate:02:"+id_sys);
	   
	   try {
		   
		  
		   
		   CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
           X509Certificate user_cert = (X509Certificate)
        		   user_cf.generateCertificate(new  ByteArrayInputStream(file_byte));
          // String x509Cert = org.picketlink.common.util.Base64.encodeBytes(user_cert.getEncoded());
           String x509Cert = Base64.encode(user_cert.getEncoded());
           
           log.info("armSubManager:saveArmSubCertificate:03:"+x509Cert);
           
           Transaction.instance().begin();
		   
		   Transaction.instance().enlist(entityManager);
		   
		   entityManager.createNativeQuery("update AC_SUBSYSTEM_CERT_BSS_T t1 " + 
		   		                           "set T1.CERT_DATE=? " + 
		   		                           "where t1.ID_SRV=? ")
		   .setParameter(1, x509Cert)
		   .setParameter(2, id_sys)
		   .executeUpdate();  
			 
		   Transaction.instance().commit();
		   
	     }catch (Exception e) {
           log.error("armSubManager:saveArmSubCertificate:ERROR:"+e);
           try{
             Transaction.instance().rollback();
           }catch(Exception et){}
         }
   }

    public void removeCert(){
 	   
 	   try {
 		   
 		   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 		   log.info("armSubManager:removeCert:sessionId:"+sessionId);
 		
 		   if(sessionId==null||sessionId.trim().equals("")){
 			   return;
 		   }
 			   
 		   entityManager.createNativeQuery("update AC_SUBSYSTEM_CERT_BSS_T t1 " + 
 		   		                           "set T1.CERT_DATE=null " + 
 		   		                           "where t1.ID_SRV=? ")
 		   .setParameter(1, new Long(sessionId))
 		   .executeUpdate();  
 			 
 			   
 	     }catch (Exception e) {
            log.error("armSubManager:removeCert:ERROR:"+e);
          }
   }
    
   public void delArmSub(){
	 try{
		log.info("armSubManager:delArmSub:01");  
		
		AcSubsystemCertBssT armSubBean = (AcSubsystemCertBssT)
				  Component.getInstance("armSubBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{armSubBean.idArmSub}"/>
		
		if(armSubBean==null){
			return;
		}
		 
		log.info("armSubManager:delArmSub:IdArmSub:"+armSubBean.getBaseId());
		
		AcSubsystemCertBssT aom = entityManager.find(AcSubsystemCertBssT.class, armSubBean.getBaseId());
		  
		entityManager.remove(aom);
		
		audit(ResourcesMap.IS, ActionsMap.DELETE); 
		
	 }catch(Exception e){
		 log.error("armSubManager:delArmSub:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 AcSubsystemCertBssT ao = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("armSubBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }
    } 
   
    public void forViewDelMessage() {
		  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDelMessage:sessionId:"+sessionId);
		  if(sessionId!=null){
		
			  DateFormat df = new SimpleDateFormat ("dd.MM.yy");
			  
			  AcSubsystemCertBssT aa = entityManager.find(AcSubsystemCertBssT.class, new Long(sessionId));
			
			  /*
		      List<Object[]> lo = (List<Object[]>) entityManager.createNativeQuery(
		    		                          "select JAS.ID_SRV, JAS.CREATED "+
                                              "from JOURN_APP_SYSTEM_BSS_T jas, "+
                                              "AC_IS_BSS_T arm "+
                                              "where ARM.ID_SRV =JAS.UP_IS "+
                                              "and ARM.ID_SRV=? ")
                           .setParameter(1, new Long(sessionId))
                           .getResultList();
			  
		      if(lo.size()==0){
			  
			    if(aa.getAcRoles()!=null&&!aa.getAcRoles().isEmpty()){
				  dellMessage="У ИС есть порождённые записи: Роли";
			    }
			    List<AcAppPage> aapl = aa.getAcAppPages();
			    if(aapl!=null&&!aapl.isEmpty()){
			      int NOT_ROOT_NODE=0;
			   // log.info("forViewDelMessage:01");
				  for(AcAppPage aap :aapl){
					//log.info("forViewDelMessage:02");
					 if(aap.getIdParent2()!=null&&!aap.getIdParent2().equals(1L)){
						 
					//	 log.info("forViewDelMessage:03");
						 
						 NOT_ROOT_NODE=1;
						 break;
					 }
					// log.info("forViewDelMessage:04");
				  }
			  //	log.info("forViewDelMessage:05");
				
				 if(NOT_ROOT_NODE==1){
				   if(dellMessage!=null){ 
				     dellMessage+=", Ресурсы.";
				  }else{
				    dellMessage="У ИС есть порождённые записи: Ресурсы.";
				  }
				 }
			   }
			   if(dellMessage!=null){
				 dellMessage+="<br/>При удалении они будут удалены!";
			   }
		      }else{
		    	  
		    	  dellMessage="У ИС есть привязка к заявке на создание системы " +
		    	  		      "<br/>№ "+lo.get(0)[0].toString()+
		    	  		      " от "+df.format((Date)lo.get(0)[1])+
		    			      ".<br/>Удаление невозможно! ";
		    	  delNot=1;
		      }
			 */
			 
			 Contexts.getEventContext().set("armSubBean", aa);
			 
			 
			 
		 }	
    }
  
    public void forViewCert() {
 	   try{
 	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 	     log.info("forViewCert:sessionId:"+sessionId);
 	     
 	     DateFormat df = new SimpleDateFormat ("dd.MM.yy");
 	    
 	     if(sessionId!=null){
 	    	 
 	    	String cert_data = (String) entityManager.createNativeQuery(
 	    			 "select to_char(T1.CERT_DATE) " + 
 	    	 		 "from AC_SUBSYSTEM_CERT_BSS_T t1 " + 
 	    	 		 "where T1.ID_SRV=? ")
                 .setParameter(1, new Long(sessionId))
                 .getSingleResult();
 	    	
 	    	 log.info("forViewCert:cert_data:"+cert_data); 
 	    	
 	     if(cert_data!=null&&!cert_data.trim().equals("")){
 	    	 byte[] cert_byte = Base64.decode(cert_data);
 	    	 // String x509Cert = org.picketlink.common.util.Base64.encodeBytes(user_cert.getEncoded());
 	         
 	    	 
 	    	 CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
 	           X509Certificate user_cert = (X509Certificate)
 	        		   user_cf.generateCertificate(new  ByteArrayInputStream(cert_byte ));
 	          
 	           
 	           log.info("armSubManager:saveArmSubCertificate:02:"+user_cert);
 	         
 	          SystemCertItem sci = new SystemCertItem();
 	    	 
 	          sci.setName(user_cert.getSubjectDN().getName());
 	          sci.setIssuer(user_cert.getIssuerDN().getName());
 	          sci.setSerial(dec_to_hex(user_cert.getSerialNumber()));
 	          sci.setDate1(df.format(user_cert.getNotBefore()));
 	          sci.setDate2(df.format(user_cert.getNotAfter()));
 	          
 	    	// AcApplication ao = entityManager.find(AcApplication.class, new Long(sessionId));
 	    	 Contexts.getEventContext().set("systemCertBean", sci);
 	    	 
 	       }
 	   	 }
 	   }catch(Exception e){
 		   log.error("forViewCert:Error:"+e);
 	   }
     } 
    
   public int getConnectError(){
	   return connectError;
   }
   
   public List<AcSubsystemCertBssT> getListArmSub() throws Exception{
	    log.info("armSubManager::getListArmSub:01");
	    try {
	    	if(listArmSub==null){
	       		listArmSub=entityManager.createQuery("select o from AcSubsystemCertBssT o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("armSubManager::getListArmSub:ERROR:"+e);
	         throw e;
	     }
	    return listArmSub;
  }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    ArmSubContext ac= new ArmSubContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			   
			 /* String reposType = FacesContext.getCurrentInstance().getExternalContext()
			      .getRequestParameterMap()
			      .get("reposType");
	            log.info("getAuditItemsListSelect:reposType:"+reposType);
			    if(reposType!=null){
					 if(reposType.equals("1")){
					 }else if(reposType.equals("2")){
					 }else if(reposType.equals("3")){
					 }else if(reposType.equals("4")){
				     }else{
				     }
			    }else{
			    }*/
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("subsystemName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("subsystemCode"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("armName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   ArmSubContext ac= new ArmSubContext();
		   auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
      
    
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  forView(); //!!!
	    ArrayList<String> selRecArmSub = (ArrayList<String>)
				  Component.getInstance("selRecArmSub",ScopeType.SESSION);
	    
	    if(selRecArmSub==null){
	       selRecArmSub = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    // AcApplication aa = searchBean(sessionId);
	    AcSubsystemCertBssT aa = new AcSubsystemCertBssT();
  	    // в getAuditList : else{it.setSelected(false);}
	    
	    if(aa!=null){
	     if(selRecArmSub.contains(sessionId)){
	    	selRecArmSub.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecArmSub.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecArmSub", selRecArmSub);	
	    
	    Contexts.getEventContext().set("armSubBean", aa);
	   }
	}
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
   } 
   
   public int getDelNot() {
	   return delNot;
   }
   public boolean getArmSubCodeExist() {
	    return armSubCodeExist;
   }
   private boolean armSubCodeExistCrt(Long idArm, String armSubCode) throws Exception {
		log.info("ArmSubManager:ArmSubcodeExistCrt:armSubCode="+armSubCode);
		if(armSubCode!=null){
		  try{
			  //в подсистемах
			  List<Object> lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_SUBSYSTEM_CERT_BSS_T ass "+
                      "where ASS.UP_IS = ? "+
                      "and ASS.SUBSYSTEM_CODE = ? ")
	  				.setParameter(1, idArm)
	  				.setParameter(2, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
		        return this.armSubCodeExist;
	          }
	          
	          //в системах
	          lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_IS_BSS_T ass "+
                      "where ASS.SIGN_OBJECT = ? ")
	  				.setParameter(1, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
	          }
	          
			  log.info("ArmSubManager:ArmSubCodeExistCrt:addLoginExist!");	
			  
		  }catch(Exception e){
	           log.error("ArmSubManager:ArmSubCodeExistCrt:Error:"+e);
	           throw e;
       }
		}
		return this.armSubCodeExist;
 }
  
  private boolean armSubCodeExistUpd(Long idArm, String armSubCode, Long idArmSub) throws Exception {
	   
		log.info("ArmSubManager:ArmSubcodeExistUpd:armSubCode:"+armSubCode);
		log.info("ArmSubManager:ArmSubcodeExistUpd:idArmSub:"+idArmSub);
			
		if(armSubCode!=null){
		  try{
			//в подсистемах
			  List<Object> lo=entityManager.createNativeQuery(
					          "select 1 "+
		                      "from AC_SUBSYSTEM_CERT_BSS_T ass "+
		                      "where ASS.UP_IS = ? "+
		                      "and ASS.SUBSYSTEM_CODE = ? " +
		                      "and ASS.ID_SRV!= ? ")
			  				.setParameter(1, idArm)
			  				.setParameter(2, armSubCode)
			  				.setParameter(3, idArmSub)
			  				.getResultList();
			  
			  if(!lo.isEmpty()){
				  armSubCodeExist=true;
				  return this.armSubCodeExist;
			  }
			  
			  //в системах
	          lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from AC_IS_BSS_T ass "+
                      "where ASS.SIGN_OBJECT = ? ")
	  				.setParameter(1, armSubCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armSubCodeExist=true;
	          }
			  
			  log.info("ArmSubManager:armSubCodeExistUpd:addLoginExist!");		     
		    }catch(Exception e){
	           log.error("ArmSubManager:armSubCodeExistUpd:Error:"+e);
	           throw e;
         }
		}
		return this.armSubCodeExist;
  }
  
  public void audit(ResourcesMap resourcesMap, ActionsMap actionsMap){
	   try{
		   AuditExportData auditExportData = (AuditExportData)Component.getInstance("auditExportData",ScopeType.SESSION);
		   auditExportData.addFunc(resourcesMap.getCode()+":"+actionsMap.getCode());
		   
	   }catch(Exception e){
		   log.error("GroupManager:audit:error:"+e);
	   }
  }
  
   public Boolean getEvaluteForList() {
	
   	log.info("armSubManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("armSubManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
    	if(remoteAudit!=null&&
    	 
    	   !remoteAudit.equals("OpenCrtFact")&&	
    	   !remoteAudit.equals("OpenUpdFact")&&
    	   !remoteAudit.equals("OpenDelFact")&&
   	       !remoteAudit.equals("onSelColFact")&&
   	       !remoteAudit.equals("refreshPdFact")){
    		log.info("reposManager:evaluteForList!!!");
   		    evaluteForList=true;
    	}
   	 }
       return evaluteForList;
   }
   public Boolean getEvaluteForListFooter() {
		
	  // 	log.info("reposManager:evaluteForListFooter:01");
	   	if(evaluteForListFooter==null){
	   		evaluteForListFooter=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("armSubManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("armSubManager:evaluteForListFooter!!!");
	   		      evaluteForListFooter=true;
	    	}
	   	 }
	       return evaluteForListFooter;
	   }
   
   public Boolean getEvaluteForBean() {
		
		  // 	log.info("reposManager:evaluteForListFooter:01");
		   	if(evaluteForBean==null){
		   		evaluteForBean=false;
		    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("remoteAudit");
			    log.info("armSubManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("armSubManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("armSubManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
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
/*
Department dept = em.getReference(Department.class, 30);
Employee emp = new Employee();
emp.setId(53);
emp.setName("Peter");
emp.setDepartment(dept);
dept.getEmployees().add(emp);
em.persist(emp);
*/
