package iac.grn.infosweb.context.mc.clusr;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import java.io.File;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcLegalEntityType;
import iac.cud.infosweb.entity.AcLinkRoleAppPagePrmssn;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.IspBssT;
import iac.cud.infosweb.entity.IspTempBssT;
import iac.cud.infosweb.entity.AcPermissionsList;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.JournIspLoad;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.local.service.ServiceReestrPro;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;
import iac.cud.infosweb.ws.classifierzip.clientsample.ClassifLoadProcessor;
import iac.cud.infosweb.ws.classifierzip.clientsample.ClientSample;
import iac.cud.infosweb.ws.classifierzip.clientsample.GRuNProFileLiteLocal;
import iac.cud.infosweb.ws.classifierzip.clientsample.PojoRunProcess;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import org.jboss.seam.Component;

import javax.ejb.TransactionManagement;
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
import  javax.ejb.TransactionManagementType;


/**
 * Управляющий Бин
 * @author bubnov
 *
 */
@Name("clUsrManager")
public class ClUsrManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * Экспортируемая сущности 
     * для отображения
     */
	//private BaseItem usrBean;             !!! Проверить !!!
	
	private int versionDetectFlag = 1;
	
	private int workExistFlag = 1;
	
	private String classifLoadVersion = "Не установлена";
	 
	private String classifExistVersion = "Не установлена";
	
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private List<AcLegalEntityType> listLET = null;
	
	private List<IspTempBssT> listOrg = null;
	
	private List<IspTempBssT> listUsr = null;
	
	private List<IspBssT> listUsrAutocomplete = null;
	
	private String loadMessage;
	private int loadFlag =0;
	
	private static String jndiBinding = "java:global/InfoS-ear/InfoS-ejb/IRemoteFrontage!iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal";
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> clUsrListCached = (List<BaseItem>)
			  Component.getInstance("clUsrListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    clUsrListCached!=null){
		 	//	log.info("getAuditList:02:"+clUsrListCached.size());
			    	this.auditList=clUsrListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("clUsrListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecOrg = (ArrayList<String>)
					  Component.getInstance("selRecOrg",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecOrg!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecOrg.contains(it.getBaseId().toString())){
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
			 
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				 ClUsrStateHolder clUsrStateHolder = (ClUsrStateHolder)
						  Component.getInstance("clUsrStateHolder",ScopeType.SESSION);
				 Set<Map.Entry<String, String>> set = clUsrStateHolder.getSortOrders().entrySet();
                 for (Map.Entry<String, String> me : set) {
      		       log.info("me.getKey+:"+me.getKey());
      		       log.info("me.getValue:"+me.getValue());
      		       
      		       if(orderQuery==null){
      		    	 orderQuery="order by o."+me.getKey()+" "+me.getValue();
      		       }else{
      		    	 orderQuery=orderQuery+", o."+me.getKey()+" "+me.getValue();  
      		       }
      		     }
                 log.info("invokeLocal:list:orderQuery:"+orderQuery);
                 
				// auditList = new ArrayList<BaseItem>();
				 auditList = entityManager.createQuery(
				//	"select o from IspBssT o where o.status='A' and o.signObject not like '%000' " +
					"select o from IspTempBssT o " +
					 (orderQuery!=null ? orderQuery : ""))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
				      //   "from IspBssT o where o.status='A' and o.signObject not like '%000' ")
				         "from IspTempBssT o  ")
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
			if(modelType.equals("clUsrDataModel")){
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
			IspTempBssT ar = searchBean(sessionId);
		 Contexts.getEventContext().set("clTempBean", ar);
	  }
   }
   
   private IspTempBssT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<IspTempBssT> clUsrListCached = (List<IspTempBssT>)
				  Component.getInstance("clUsrListCached",ScopeType.SESSION);
		if(clUsrListCached!=null){
			for(IspTempBssT it : clUsrListCached){
				 
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
   
   public void addOrg(){
	   log.info("clUsrManager:addOrg:01");
	   
	   IspTempBssT clUsrBeanCrt = (IspTempBssT)
				  Component.getInstance("clUsrBeanCrt",ScopeType.CONVERSATION);
	   
	   if(clUsrBeanCrt==null){
		   return;
	   }
	 
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		   
	    //  clUsrBeanCrt.setCreator(au.getIdUser());
		  clUsrBeanCrt.setCreated(new Date());
	      entityManager.persist(clUsrBeanCrt);
	    	   
	      entityManager.flush();
	      entityManager.refresh(clUsrBeanCrt);
	    	     
	    }catch (Exception e) {
	       log.error("clUsrManager:addOrg:ERROR:"+e);
	    }
	   
   }
   
   public void loadClassif(){
	   log.info("clUsrManager:loadClassif:01");
	   
	   try {
		   
		   String clVersion = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("clVersion");
		   
		  log.info("clUsrManager:loadClassif:02:"+clVersion);
		  
		  // entityManager.createNativeQuery("TRUNCATE TABLE ISP_TEMP_BSS_T")
		 //   .executeUpdate();
		   
		  // entityManager.createNativeQuery("TRUNCATE TABLE ISP_TEMP_BSS_T")
		//	.executeUpdate();
		   
		  // ClientSample.run();
		   
		   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		 
		   
		   String idSess =(String) this. entityManager.createNativeQuery("select to_char(JOURN_ISP_LOAD_SEQ.nextval) sgnc from dual").getSingleResult();
	        
	       int seancact = Integer.valueOf(idSess);
		   
		   entityManager.createNativeQuery(
				   "INSERT INTO JOURN_ISP_LOAD (ID_SRV, LOAD_START, CREATED, CREATOR) "+
		   		   "VALUES ("+seancact+", sysdate, sysdate, "+au.getIdUser()+" ) ")
			.executeUpdate();
		  
		 //  PojoRunProcess pp = new PojoRunProcess();
		 //  pp.startProcess();
			
		  /*
		   GRuNProFileLiteLocal gl = (GRuNProFileLiteLocal) Component.getInstance("gRuNProFileLite",ScopeType.EVENT);
			
		   System.out.println("IServCatalogLoadinge:init:02");
		   int result=gl.process();
		   System.out.println("IServCatalogLoading:init:result:"+result);
		   
		   log.info("clUsrManager:loadClassif:02");
		   
		   */
		   Context ctx = new InitialContext(); 
	    	 
	       BaseParamItem bpi = new BaseParamItem(ServiceReestrPro.ClassifLoad.name());
	
	       bpi.put("gactiontype", ServiceReestrAction.TASK_RUN.name());
	       
	       bpi.put("seancact", new Long(seancact));
	       
	       if(clVersion!=null&&!clVersion.isEmpty()){
	         bpi.put("clVersion", new Integer(clVersion));
	       }
	       
	       IRemoteFrontageLocal obj = (IRemoteFrontageLocal)ctx.lookup(jndiBinding);
        		   
           obj.run(bpi);
		   
	    }catch (Exception e) {
	       log.error("clUsrManager:loadClassif:ERROR:"+e);
	       e.printStackTrace(System.out);
	    }
	   
   }
   
   public void moveClassif(){
	   
	   log.info("clUsrManager:moveClassif:01");
	   
	   try {
		   
		   String clVersion = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("clVersion");
		   
		  log.info("clUsrManager:moveClassif:02:"+clVersion);
		 
		  
		   Context ctx = new InitialContext(); 
	    	 
	       BaseParamItem bpi = new BaseParamItem(ServiceReestrPro.ClassifLoad.name());
	
	       bpi.put("gactiontype", ServiceReestrAction.TO_WORK.name());
	       
	       IRemoteFrontageLocal obj = (IRemoteFrontageLocal)ctx.lookup(jndiBinding);
       		   
           obj.run(bpi);
          
		  /*
		  //чистим архив
		  //clear ISP_ARH_BSS_T
		   entityManager.createNativeQuery(
				   "TRUNCATE TABLE ISP_ARH_BSS_T")
			.executeUpdate();
		 
		   //копируем текущее в архив на случай отката
		   //ISP_BSS_T -> ISP_ARH_BSS_T
		   entityManager.createNativeQuery(
				   "insert*/ /*+ APPEND */ /*into ISP_ARH_BSS_T( "+
				     "ID_SRV, "+
                     "SIGN_OBJECT, "+
					  "SIGN_OKOGY, "+
					  "FULL_, "+
					  "POSITION, "+
					  "FIO, "+
					  "POSTAL_CODE, "+
					  "COUNTRY, "+
					  "REGION, "+
					  "PLACE, "+
					  "PREFIX, "+
					  "HOUSE, "+
					  "HOUSING, "+
					  "FLAT, "+
					  "PHONE, "+
					  "FAX, "+
					  "EMAIL, "+
					  "STATUS, "+
					  "DATE_IN_SRV, "+
					  "DATE_DEL_SRV, "+
					  "DOC_IN_SRV, "+
					  "DOC_DEL_SRV, "+
					  "UNI_ID, "+
					  "IS_EXTERNAL, "+
					  "SIGN_OKATO, "+
					  "UP_ISP_LOAD, "+
					  "CREATED "+
				 ") select "+
					 "ID_SRV, "+
                     "SIGN_OBJECT, "+
					  "SIGN_OKOGY, "+
					  "FULL_, "+
					  "POSITION, "+
					  "FIO, "+
					  "POSTAL_CODE, "+
					  "COUNTRY, "+
					  "REGION, "+
					  "PLACE, "+
					  "PREFIX, "+
					  "HOUSE, "+
					  "HOUSING, "+
					  "FLAT, "+
					  "PHONE, "+
					  "FAX, "+
					  "EMAIL, "+
					  "STATUS, "+
					  "DATE_IN_SRV, "+
					  "DATE_DEL_SRV, "+
					  "DOC_IN_SRV, "+
					  "DOC_DEL_SRV, "+
					  "UNI_ID, "+
					  "IS_EXTERNAL, "+
					  "SIGN_OKATO, "+
					  "UP_ISP_LOAD, "+
					  "sysdate "+
				   " from ISP_TEST_BSS_T  ")
					.executeUpdate();
		
		  //чистим текущее
		  //clear ISP_BSS_T
		   entityManager.createNativeQuery(
				   "TRUNCATE TABLE ISP_TEST_BSS_T")
			.executeUpdate();
		
		   
		   //копируем из технологич. т. в текущее
		   //ISP_TEMP_BSS_T -> ISP_BSS_T	   
		   entityManager.createNativeQuery(
				   "insert*/ /*+ APPEND */ /*into ISP_TEST_BSS_T( "+
				     "ID_SRV, "+
                     "SIGN_OBJECT, "+
					  "SIGN_OKOGY, "+
					  "FULL_, "+
					  "POSITION, "+
					  "FIO, "+
					  "POSTAL_CODE, "+
					  "COUNTRY, "+
					  "REGION, "+
					  "PLACE, "+
					  "PREFIX, "+
					  "HOUSE, "+
					  "HOUSING, "+
					  "FLAT, "+
					  "PHONE, "+
					  "FAX, "+
					  "EMAIL, "+
					  "STATUS, "+
					  "DATE_IN_SRV, "+
					  "DATE_DEL_SRV, "+
					  "DOC_IN_SRV, "+
					  "DOC_DEL_SRV, "+
					  "UNI_ID, "+
					  "IS_EXTERNAL, "+
					  "SIGN_OKATO, "+
					  "UP_ISP_LOAD, "+
					  "CREATED "+
				 ") select "+
					 "ID_SRV, "+
                     "SIGN_OBJECT, "+
					  "SIGN_OKOGY, "+
					  "FULL_, "+
					  "POSITION, "+
					  "FIO, "+
					  "POSTAL_CODE, "+
					  "COUNTRY, "+
					  "REGION, "+
					  "PLACE, "+
					  "PREFIX, "+
					  "HOUSE, "+
					  "HOUSING, "+
					  "FLAT, "+
					  "PHONE, "+
					  "FAX, "+
					  "EMAIL, "+
					  "STATUS, "+
					  "DATE_IN_SRV, "+
					  "DATE_DEL_SRV, "+
					  "DOC_IN_SRV, "+
					  "DOC_DEL_SRV, "+
					  "UNI_ID, "+
					  "IS_EXTERNAL, "+
					  "SIGN_OKATO, "+
					  "UP_ISP_LOAD, "+
					  "sysdate "+
				   " from ISP_TEMP_BSS_T  ")
					.executeUpdate();
		
		   entityManager.createNativeQuery(
				   "TRUNCATE TABLE ISP_TEMP_BSS_T")
			.executeUpdate();
		*/
		   
		   
		   
	    }catch (Exception e) {
	       log.error("clUsrManager:moveClassif:ERROR:"+e);
	       e.printStackTrace(System.out);
	    }
	   
   }
   
   public void updOrg(){
	   
	   log.info("clUsrManager:updOrg:01");
	   
	   IspTempBssT clUsrBean = (IspTempBssT)
				  Component.getInstance("clUsrBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("clUsrManager:updOrg:sessionId:"+sessionId);
	
	   if(clUsrBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		 //	entityManager.merge(acUsrBean);
		  IspTempBssT aom = entityManager.find(IspTempBssT.class, new Long(sessionId));
		  
		  aom.setFull(clUsrBean.getFull());
		 /* aom.setShortName(clUsrBean.getShortName());
		  aom.setContactEmployeeFio(clUsrBean.getContactEmployeeFio());
		  aom.setContactEmployeePosition(clUsrBean.getContactEmployeePosition());
		  aom.setContactEmployeePhone(clUsrBean.getContactEmployeePhone());
		  aom.setAcLegalEntityType(clUsrBean.getAcLegalEntityType());
		  aom.setIsExternal(clUsrBean.getIsExternal());*/
		  
		 // aom.setModificator(au.getIdUser());
		 // aom.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aom);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	      Contexts.getEventContext().set("clUsrBean", aom);
	    	  
	     }catch (Exception e) {
           log.error("clUsrManager:updOrg:ERROR:"+e);
         }
   }
   
   public void delOrg(){
	 try{
		log.info("clUsrManager:delOrg:01");  
		
		IspTempBssT clUsrBean = (IspTempBssT)
				  Component.getInstance("clUsrBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{usrBean.idUser}"/>
		
		if(clUsrBean==null){
			return;
		}
		 
		log.info("clUsrManager:delOrg:IdOrg:"+clUsrBean.getBaseId());
		
		IspTempBssT aom = entityManager.find(IspTempBssT.class, clUsrBean.getBaseId());
		  
		entityManager.remove(aom);
		
	 }catch(Exception e){
		 log.error("clUsrManager:delOrg:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	  /*  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 IspTempBssT ao = entityManager.find(IspTempBssT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("clUsrBean", ao);
	   	 }*/
		   
		  try{
			  //1 - успех
			  //0 или null- не успех
			  String cl_ver_temp = (String) entityManager.createNativeQuery(
				   "select to_char(max(JIL.CLASSIF_VERSION)) cl_ver "+
                   "from JOURN_ISP_LOAD jil, "+
                //   "ISP_TEST_BSS_T it "+
                   "ISP_BSS_T it "+
                   "where JIL.ID_SRV=IT.UP_ISP_LOAD  ")
		   		.getSingleResult();
			
			  if(cl_ver_temp!=null&&!cl_ver_temp.trim().isEmpty()){
	   				this.classifExistVersion =cl_ver_temp;
			  }
			  
		  }catch(NoResultException e){
			  log.info("clUsrManager:forViewUpdDel:NoResultException");
		  }
		   
		  log.info("clUsrManager:forViewUpdDel:02:"+this.classifExistVersion);
		  
           String clLdVer = ClientSample.getVersion();
		   
           log.info("clUsrManager:forViewUpdDel:02+:"+clLdVer);
        		   
           if(clLdVer!=null){
             this.classifLoadVersion=clLdVer;
             
             if(clLdVer.equals(this.classifExistVersion)){
            	 this.versionDetectFlag=-2;
             }
             
           }else{
        	   this.versionDetectFlag=0;
           }
           
		   log.info("clUsrManager:forViewUpdDel:03:"+this.classifLoadVersion);
		   
		   if(ClassifLoadProcessor.getControls().containsKey("classif_load")){
	    		log.info("clUsrManager:forViewUpdDel:04:return");
	    		this.versionDetectFlag=-1;
	    		return;
	       }
		   
		   
	   }catch(Exception e){
		   log.error("clUsrManager:forViewUpdDel:Error:"+e);
		   e.printStackTrace(System.out);
		   this.versionDetectFlag=0;
	   }
    } 
   
    public void forViewDelMessage() {
		/*  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDel:sessionId:"+sessionId);
		  if(sessionId!=null){
			 IspBssT ao = entityManager.find(IspBssT.class, new Long(sessionId));
			 if(ao.getAcUsers()!=null&&!ao.getAcUsers().isEmpty()){
				dellMessage="У организации есть порождённые записи! При удалении они будут удалены!";
			 }
			 Contexts.getEventContext().set("clUsrBean", ao);
		 }	*/
    }
    
     public void forViewLoadMessage() {
		  log.info("forViewLoadMessage:01");
		  
		  JournIspLoad ao = null;
				  
         try{
			  
        	 ao = entityManager.createQuery(
				   "select t from JournIspLoad t "+
		   		   "where t.idSrv = "+
		   		   "(select max(t1.idSrv) from JournIspLoad t1 ) ", 
		   		JournIspLoad.class)
		   		.getSingleResult();
			  
		  }catch(NoResultException e){
			  log.info("clUsrManager:forViewUpdDel:NoResultException");
		  }
  
         if(ao==null){
        	 loadMessage="Загрузок не было";
         }else{
        	 
        	 if(ao.getLoadFinish()==null){
        		 loadMessage="Процесс загрузки выполняется";
        		 loadFlag=1;
        	 }else{
        		 loadMessage="Процесс загрузки завершен";
        		 loadFlag=2;
        	 }
            
        	 Contexts.getEventContext().set("journIspLoadBean", ao);
         }
		 
	
    }
    
     public void forViewMove() {
	   log.info("forViewMove:01");
		  
				  
      	 List<Object[]> lo = (List<Object[]>)entityManager.createNativeQuery(
				   "select 1 from dual where EXISTS (select * from ISP_TEMP_BSS_T) ")
		   		.getResultList();
			  
		
        if(lo==null||lo.isEmpty()){
       	 // moveMessage="Таблица пуста!";
        	workExistFlag=-1;
        }else{
        	
        	 try{
   			  //1 - успех
   			  //0 или null- не успех
        		String cl_ver_temp = (String) entityManager.createNativeQuery(
   				   "select to_char(max(JIL.CLASSIF_VERSION)) cl_ver "+
                      "from JOURN_ISP_LOAD jil, "+
                    //  "ISP_TEST_BSS_T it "+
                      "ISP_BSS_T it "+
                      "where JIL.ID_SRV=IT.UP_ISP_LOAD  ")
   		   		.getSingleResult();
   			  
   			 if(cl_ver_temp!=null&&!cl_ver_temp.trim().isEmpty()){
   				this.classifExistVersion =cl_ver_temp;
			  }
   			
   		  }catch(NoResultException e){
   			  log.info("clUsrManager:forViewMove:1:NoResultException");
   		  }
         }
        
        try{
 			  //1 - успех
 			  //0 или null- не успех
 			  String cl_ver_temp = (String) entityManager.createNativeQuery(
 				   "select to_char(max(JIL.CLASSIF_VERSION)) cl_ver "+
                    "from JOURN_ISP_LOAD jil, "+
                    "ISP_TEMP_BSS_T it "+
                    "where JIL.ID_SRV=IT.UP_ISP_LOAD  ")
 		   		.getSingleResult();
 			 
 			  if(cl_ver_temp!=null&&!cl_ver_temp.trim().isEmpty()){
 			     this.classifLoadVersion =cl_ver_temp;
 			  }
 		  }catch(NoResultException e){
 			  log.info("clUsrManager:forViewMove:2:NoResultException");
 		  }
		 
	
   }
     
    public List<AcLegalEntityType> getListLET() throws Exception{
	   log.info("getLET");
	    try {
	    	if(listLET==null){
	    	  listLET = entityManager.createQuery("select r from AcLegalEntityType r").getResultList();
	    	 }
	    	} catch (Exception e) {
	    	 log.error("getLET:ERROR="+e);
	         throw e;
           }
	    return listLET;
   }
  
    public List<IspTempBssT> getListOrg() throws Exception{
	    log.info("getListOrg:01");
	    try {
	    	if(listOrg==null){
	    		log.info("getListOrg:02");
	    		listOrg=entityManager.createQuery("select o from IspTempBssT o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("getListOrg:ERROR:"+e);
	         throw e;
	     }
	    return listOrg;
   }
    
   public int getConnectError(){
	   return connectError;
   }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		  // log.info("getAuditItemsListSelect:01");
	
	    ClUsrContext ac= new ClUsrContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("signObject"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("full"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("fio"));
			   
			/*   auditItemsListSelect.add(ac.getAuditItemsMap().get("letValue"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("contactEmployeePosition"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("contactEmployeeFio"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("isExternalValue"));*/
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("clUsrManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   ClUsrContext ac= new ClUsrContext();
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
	    ArrayList<String> selRecOrg = (ArrayList<String>)
				  Component.getInstance("selRecOrg",ScopeType.SESSION);
	    
	    if(selRecOrg==null){
	       selRecOrg = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	  //  AcOrganization ao = searchBean(sessionId);
	   IspTempBssT ao = new IspTempBssT();
	 // в getAuditList : else{it.setSelected(false);}
	    
	    if(ao!=null){
	     if(selRecOrg.contains(sessionId)){
	    	selRecOrg.remove(sessionId);
	    	ao.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecOrg.add(sessionId);
	    	ao.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecOrg", selRecOrg);	
	    
	    Contexts.getEventContext().set("clTempBean", ao);
	    }
	  }
   
  
   
   //!!!
   //IspBssT а не IspTempBssT
   public List<IspBssT> autocomplete(Object suggest) throws Exception{
   	String pref = (String)suggest;
   	
	    log.info("Usr:autocomplete:01:pref:"+pref);
	    
	    String  signObject = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("signObject");
	    log.info("Usr:autocomplete:signObject="+signObject);
	    
	    String codeOrg=signObject.substring(0, 3);
	    
	    try {
	    	if(listUsrAutocomplete==null){
	    		log.info("Usr:autocomplete:02");
	    		listUsrAutocomplete=entityManager.createQuery(
	    				"select o from IspBssT o where o.status='A' and o.signObject not like '%000' " +
	    			//	"and o.full like '"+pref+"%' " +
	    				"and upper(o.fio) like upper(:pref) " +
	    				"and substr(o.signObject,1,3) = :codeOrg " +
	    				"order by o.fio ")
	    				.setParameter("pref", pref+"%")
	    				.setParameter("codeOrg", codeOrg)
	    				.getResultList();
	    		log.info("Usr:autocomplete:03:size:"+listUsrAutocomplete.size());
	    	}
	     } catch (Exception e) {
	    	 log.error("Usr:autocomplete:ERROR:"+e);
	         throw e;
	     }
	    return listUsrAutocomplete;
  }
   //!!!
   //IspBssT а не IspTempBssT
   public void forViewAutocomplete() {
	   try{
		    String department = null;
		     String signObject = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("signObject");
		     log.info("forViewAutocomplete:signObject:"+signObject);
		     if(signObject!=null){
		    	 IspBssT ao = (IspBssT)entityManager.createQuery(
		    				"select o from IspBssT o where o.status='A' " +
		    				"and o.signObject = :signObject ")
		    		    	.setParameter("signObject", signObject)
		    		    	.getSingleResult();
		    	 
		    	   try{
		    		 
		    	     IspBssT ao_dep = (IspBssT)entityManager.createQuery(
		    				  "select o from IspBssT o where o.status='A' " +
		    				  "and o.signObject = :signObject ")
		    		    	  .setParameter("signObject", signObject.substring(0, 5)+"000")
		    		    	  .getSingleResult();
		    	    
		    	     department=ao_dep.getFull();
		    	    
		    	    }catch(NoResultException e){
					   log.error("forViewAutocomplete:NoResultError:"+e);
				    }
		    	   
		    	   if(department!=null){
		    	     ao.setDepartment(department);
		    	   }
		     	 Contexts.getEventContext().set("clUsrBean", ao);
		   	 }
		   }catch(Exception e){
			   log.error("forViewAutocomplete:Error:"+e);
		   }
   }
   
   //!!!
   //IspBssT а не IspTempBssT
   public void forViewAutocomplete(String signObject) {
	   try{
		    String department = null;
		     log.info("forViewAutocomplete:signObject:"+signObject);
		     if(signObject!=null){
		    	 IspBssT ao = (IspBssT)entityManager.createQuery(
		    				"select o from IspBssT o where o.status='A' " +
		    				"and o.signObject = :signObject ")
		    		    	.setParameter("signObject", signObject)
		    		    	.getSingleResult();
		    	 
		    	   try{
		    		 
		    	     IspBssT ao_dep = (IspBssT)entityManager.createQuery(
		    				  "select o from IspBssT o where o.status='A' " +
		    				  "and o.signObject = :signObject ")
		    		    	  .setParameter("signObject", signObject.substring(0, 5)+"000")
		    		    	  .getSingleResult();
		    	    
		    	     department=ao_dep.getFull();
		    	    
		    	    }catch(NoResultException e){
					   log.error("forViewAutocomplete:NoResultError:"+e);
				    }
		    	   
		    	   if(department!=null){
		    	     ao.setDepartment(department);
		    	   }
		     	 Contexts.getEventContext().set("clUsrBean", ao);
		   	 }
		   }catch(Exception e){
			   log.error("forViewAutocomplete:Error:"+e);
		   }
   }
   
   public Boolean getEvaluteForList() {
	
   	log.info("clUsrManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("clUsrManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
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
		   log.info("clUsrManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("clUsrManager:evaluteForListFooter!!!");
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
			    log.info("clUsrManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("clUsrManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("clUsrManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
		   }
public String getClassifLoadVersion() {
	return classifLoadVersion;
}
public void setClassifLoadVersion(String classifLoadVersion) {
	this.classifLoadVersion = classifLoadVersion;
}
public String getClassifExistVersion() {
	return classifExistVersion;
}
public void setClassifExistVersion(String classifExistVersion) {
	this.classifExistVersion = classifExistVersion;
}
public int getVersionDetectFlag() {
	return versionDetectFlag;
}
public void setVersionDetectFlag(int versionDetectFlag) {
	this.versionDetectFlag = versionDetectFlag;
}
public String getLoadMessage() {
	return loadMessage;
}
public void setLoadMessage(String loadMessage) {
	this.loadMessage = loadMessage;
}
public int getWorkExistFlag() {
	return workExistFlag;
}
public void setWorkExistFlag(int workExistFlag) {
	this.workExistFlag = workExistFlag;
}
public int getLoadFlag() {
	return loadFlag;
}
public void setLoadFlag(int loadFlag) {
	this.loadFlag = loadFlag;
}
public List<IspBssT> getListUsrAutocomplete() {
	return listUsrAutocomplete;
}
public void setListUsrAutocomplete(List<IspBssT> listUsrAutocomplete) {
	this.listUsrAutocomplete = listUsrAutocomplete;
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
