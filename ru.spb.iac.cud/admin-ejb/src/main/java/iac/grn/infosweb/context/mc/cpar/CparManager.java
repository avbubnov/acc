package iac.grn.infosweb.context.mc.cpar;

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
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcLegalEntityType;
import iac.cud.infosweb.entity.AcLinkRoleAppPagePrmssn;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.AcOrganization;
import iac.cud.infosweb.entity.AcPermissionsList;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.SettingsKnlT;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.infosweb.session.cache.CacheManager;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
@Name("cparManager")
public class CparManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * Экспортируемая сущности 
     * для отображения
     */
	//private BaseItem usrBean;             !!! Проверить !!!
	
	private String dellMessage;
	 
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;

	private SettingsKnlT setting;
	
	private String settingValue;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> cparListCached = (List<BaseItem>)
			  Component.getInstance("cparListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    cparListCached!=null){
		 	//	log.info("getAuditList:02:"+orgListCached.size());
			    	this.auditList=cparListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("cparListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecCpar = (ArrayList<String>)
					  Component.getInstance("selRecCpar",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecCpar!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecCpar.contains(it.getBaseId().toString())){
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
				 
				 CparStateHolder orgStateHolder = (CparStateHolder)
						  Component.getInstance("cparStateHolder",ScopeType.SESSION);
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
                 
				 if(orderQuery!=null&&orderQuery.contains("o1.full")){
                	 auditList = entityManager.createQuery(
                	"select o from SettingsKnlT o LEFT JOIN o.servicesBssT o1 "+		 
                	 (orderQuery!=null ? orderQuery : ""))
                             .setFirstResult(firstRow)
                             .setMaxResults(numberOfRows)
                             .getResultList();
                 }else{
				  auditList = entityManager.createQuery("select o from SettingsKnlT o "+(orderQuery!=null ? orderQuery : ""))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
				 }
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
				         "from SettingsKnlT o ")
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
			if(modelType.equals("cparDataModel")){
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
		 SettingsKnlT ar = searchBean(sessionId);
		 Contexts.getEventContext().set("cparBean", ar);
	  }
   }
   
   private SettingsKnlT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<SettingsKnlT> cparListCached = (List<SettingsKnlT>)
				  Component.getInstance("cparListCached",ScopeType.SESSION);
		if(cparListCached!=null){
			for(SettingsKnlT it : cparListCached){
				 
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
   /*
   public void addCpar(){
	   log.info("cparManager:addOrg:01");
	   
	   SettingsKnlT cparBeanCrt = (SettingsKnlT)
				  Component.getInstance("cparBeanCrt",ScopeType.CONVERSATION);
	   
	   if(cparBeanCrt==null){
		   return;
	   }
	 
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		   
		  cparBeanCrt.setCreator(au.getIdUser());
		  cparBeanCrt.setCreated(new Date());
	      entityManager.persist(cparBeanCrt);
	    	
	      
	      entityManager.flush();
	      entityManager.refresh(cparBeanCrt);
	    
	    }catch (Exception e) {
	       log.error("cparManager:addCpar:ERROR:"+e);
	    }
	   
   }*/
   
   public void updCpar(){
	   
	   log.info("cparManager:updCpar:01");
	   
	   SettingsKnlT cparBean = (SettingsKnlT)
				  Component.getInstance("cparBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("cparManager:updCpar:sessionId:"+sessionId);
	
	   if(cparBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		//  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		  SettingsKnlT aam = entityManager.find(SettingsKnlT.class, new Long(sessionId));
		  
		  aam.setValueParam(cparBean.getValueParam());
				  
		//  aam.setModificator(au.getIdUser());
		//  aam.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	      Contexts.getEventContext().set("cparBean", aam);
	    	  
	      audit(ResourcesMap.CONF_PARAM, ActionsMap.UPDATE); 
	      
	     }catch (Exception e) {
           log.error("cparManager:updSrm:ERROR:"+e);
         }
   }
 /*  
   public void delCpar(){
	 try{
		log.info("cparManager:delCpar:01");  
		
		SettingsKnlT cparBean = (SettingsKnlT)
				  Component.getInstance("cparBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{cparBean.idArm}"/>
		
		if(cparBean==null){
			return;
		}
		 
		log.info("cparManager:delCpar:IdCpar:"+cparBean.getIdSrv());
		
		SettingsKnlT aom = entityManager.find(SettingsKnlT.class, cparBean.getIdSrv());
		  
		entityManager.remove(aom);
		
	 }catch(Exception e){
		 log.error("cparManager:delCpar:error:"+e); 
	 }
    }*/
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 SettingsKnlT ao = entityManager.find(SettingsKnlT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("cparBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }
    } 
   /*
    public void forViewDelMessage() {
		  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDel:sessionId:"+sessionId);
		  if(sessionId!=null){
			   SettingsKnlT aa = entityManager.find(SettingsKnlT.class, new Long(sessionId));
			    if((aa.getAcAppPages()!=null&&!aa.getAcAppPages().isEmpty()) ||
				(aa.getAcRoles()!=null&&!aa.getAcRoles().isEmpty())){
				dellMessage="У консоли есть порождённые записи! При удалении они будут удалены!";
			 }
			 Contexts.getEventContext().set("cparBean", aa);
		 }	
    }*/
  
    public SettingsKnlT getSetting(String codeParam){
    	
    	log.info("cparManager:getParam:01:"+codeParam);
    	 
    	try{
    		if(setting==null){
    		    setting = entityManager.createQuery(
					   "select o " +
			           "from SettingsKnlT o " +
			           "where o.signObject = :codeParam ", SettingsKnlT.class)
			           .setParameter("codeParam", codeParam)
	                   .getSingleResult();
    		 
    		}
    	}catch(Exception e){
    		log.info("cparManager:getParam:error:"+e);
    	}
   	    return setting;
   }
   
  public String getSettingValue(String codeParam){
    	
    	log.info("cparManager:getSettingValue:01:"+codeParam);
    	 
    	try{
    		if(settingValue==null){
    			settingValue = (String) entityManager.createQuery(
					   "select o.valueParam " +
			           "from SettingsKnlT o " +
			           "where o.signObject = :codeParam ")
			           .setParameter("codeParam", codeParam)
	                   .getSingleResult();
    		 
    		}
    	}catch(Exception e){
    		log.info("cparManager:getSettingValue:error:"+e);
    	}
   	    return settingValue;
   }

   public int getConnectError(){
	   return connectError;
   }
   
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    CparContext ac= new CparContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("nameParam"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("valueParam"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("servName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   CparContext ac= new CparContext();
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
	    ArrayList<String> selRecCpar = (ArrayList<String>)
				  Component.getInstance("selRecCpar",ScopeType.SESSION);
	    
	    if(selRecCpar==null){
	       selRecCpar = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    // AcApplication aa = searchBean(sessionId);
	    SettingsKnlT aa = new SettingsKnlT();
  	    // в getAuditList : else{it.setSelected(false);}
	    
	    if(aa!=null){
	     if(selRecCpar.contains(sessionId)){
	    	selRecCpar.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecCpar.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecCpar", selRecCpar);	
	    
	    Contexts.getEventContext().set("cparBean", aa);
	   }
	}
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
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
	
   	log.info("cparManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("cparManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
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
		   log.info("cparManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("cparManager:evaluteForListFooter!!!");
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
			    log.info("cparManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("cparManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("cparManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
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
