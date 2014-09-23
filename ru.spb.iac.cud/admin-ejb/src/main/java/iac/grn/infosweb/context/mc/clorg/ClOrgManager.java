package iac.grn.infosweb.context.mc.clorg;

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
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcLegalEntityType;
import iac.cud.infosweb.entity.AcLinkRoleAppPagePrmssn;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.IspBssT;
import iac.cud.infosweb.entity.AcPermissionsList;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
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
@Name("clOrgManager")
public class ClOrgManager {
	
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
	
	private List<AcLegalEntityType> listLET = null;
	
	private List<IspBssT> listOrg = null;
	
 	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> clOrgListCached = (List<BaseItem>)
			  Component.getInstance("clOrgListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    clOrgListCached!=null){
		 	//	log.info("getAuditList:02:"+clOrgListCached.size());
			    	this.auditList=clOrgListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("clOrgListCached", this.auditList);
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
				 
				 ClOrgStateHolder clOrgStateHolder = (ClOrgStateHolder)
						  Component.getInstance("clOrgStateHolder",ScopeType.SESSION);
				 Set<Map.Entry<String, String>> set = clOrgStateHolder.getSortOrders().entrySet();
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
				 // "select o from IspBssT o where o.status='A' and o.signObject like '%00000' " +
					"select o from IspBssT o  " +
					 (orderQuery!=null ? orderQuery : ""))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
						// "from IspBssT o where o.status='A' and o.signObject like '%00000' ")
				         "from IspBssT o  ")
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
			if(modelType.equals("clOrgDataModel")){
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
			IspBssT ar = searchBean(sessionId);
		 Contexts.getEventContext().set("clOrgBean", ar);
	  }
   }
   
   private IspBssT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<IspBssT> clOrgListCached = (List<IspBssT>)
				  Component.getInstance("clOrgListCached",ScopeType.SESSION);
		if(clOrgListCached!=null){
			for(IspBssT it : clOrgListCached){
				 
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
	   log.info("clOrgManager:addOrg:01");
	   
	   IspBssT clOrgBeanCrt = (IspBssT)
				  Component.getInstance("clOrgBeanCrt",ScopeType.CONVERSATION);
	   
	   if(clOrgBeanCrt==null){
		   return;
	   }
	 
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		   
	      clOrgBeanCrt.setCreator(au.getIdUser());
		  clOrgBeanCrt.setCreated(new Date());
	      entityManager.persist(clOrgBeanCrt);
	    	   
	      entityManager.flush();
	      entityManager.refresh(clOrgBeanCrt);
	    	     
	    }catch (Exception e) {
	       log.error("clOrgManager:addOrg:ERROR:"+e);
	    }
	   
   }
   
   public void updOrg(){
	   
	   log.info("clOrgManager:updOrg:01");
	   
	   IspBssT clOrgBean = (IspBssT)
				  Component.getInstance("clOrgBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("clOrgManager:updOrg:sessionId:"+sessionId);
	
	   if(clOrgBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		 //	entityManager.merge(acUsrBean);
		  IspBssT aom = entityManager.find(IspBssT.class, new Long(sessionId));
		  
		  aom.setFull(clOrgBean.getFull());
		 /* aom.setShortName(clOrgBean.getShortName());
		  aom.setContactEmployeeFio(clOrgBean.getContactEmployeeFio());
		  aom.setContactEmployeePosition(clOrgBean.getContactEmployeePosition());
		  aom.setContactEmployeePhone(clOrgBean.getContactEmployeePhone());
		  aom.setAcLegalEntityType(clOrgBean.getAcLegalEntityType());
		  aom.setIsExternal(clOrgBean.getIsExternal());*/
		  
		  aom.setModificator(au.getIdUser());
		  aom.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aom);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	      Contexts.getEventContext().set("clOrgBean", aom);
	    	  
	     }catch (Exception e) {
           log.error("clOrgManager:updOrg:ERROR:"+e);
         }
   }
   
   public void delOrg(){
	 try{
		log.info("clOrgManager:delOrg:01");  
		
		IspBssT clOrgBean = (IspBssT)
				  Component.getInstance("clOrgBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{usrBean.idUser}"/>
		
		if(clOrgBean==null){
			return;
		}
		 
		log.info("clOrgManager:delOrg:IdOrg:"+clOrgBean.getBaseId());
		
		IspBssT aom = entityManager.find(IspBssT.class, clOrgBean.getBaseId());
		  
		entityManager.remove(aom);
		
	 }catch(Exception e){
		 log.error("clOrgManager:delOrg:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 IspBssT ao = entityManager.find(IspBssT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("clOrgBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
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
			 Contexts.getEventContext().set("clOrgBean", ao);
		 }	*/
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
  
    public List<IspBssT> getListOrg() throws Exception{
	    log.info("getListOrg:01");
	    try {
	    	if(listOrg==null){
	    		log.info("getListOrg:02");
	    		listOrg=entityManager.createQuery("select o from IspBssT o where o.status='A' and o.signObject like '%00000' ").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("getListOrg:ERROR:"+e);
	         throw e;
	     }
	    return listOrg;
   }
    
    public List<IspBssT> autocomplete(Object suggest) throws Exception{
    	String pref = (String)suggest;
    	
	    log.info("autocomplete:01:pref:"+pref);
	    try {
	    	
	    	if(listOrg==null){
	    		
	    		log.info("autocomplete:02");
	    		
	    		AcUser  cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
	    		
	    		listOrg=entityManager.createQuery(
	    				"select o from IspBssT o where o.status='A' " +
	    				"and o.signObject like '%00000' " +
	    				"and ( 1 = :orgAccFlag  or o.signObject = :orgCode) " +
	    			//	"and o.full like '"+pref+"%' " +
	    				"and upper(o.full) like upper(:pref) " +
	    				"order by o.full ")
	    				.setParameter("pref", pref+"%")
	    				.setParameter("orgAccFlag", cau.getIsAccOrgManagerValue() ? -1 : 1)
	    				.setParameter("orgCode", cau.getUpSign()!=null? cau.getUpSign():"")
	    				.getResultList();
	    		
	    		log.info("autocomplete:03:size:"+listOrg.size());
	    	}
	     } catch (Exception e) {
	    	 log.error("autocomplete:ERROR:"+e);
	         throw e;
	     }
	    return listOrg;
   }
    
   public int getConnectError(){
	   return connectError;
   }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		  // log.info("getAuditItemsListSelect:01");
	
	    ClOrgContext ac= new ClOrgContext();
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
	   log.info("clOrgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   ClOrgContext ac= new ClOrgContext();
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
	   IspBssT ao = new IspBssT();
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
	    
	    Contexts.getEventContext().set("clOrgBean", ao);
	    }
	  }
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
   } 
   
   public void forViewAutocomplete() {
	   try{
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
		    	/* IspBssT ao = new IspBssT();
		    	 ao.setSignObject(signObject);*/
		    	 
		    	 Contexts.getEventContext().set("clOrgBean", ao);
		   	 }
		   }catch(Exception e){
			   log.error("forViewAutocomplete:Error:"+e);
		   }
   }
   
   public void forViewAutocomplete(String signObject) {
	   try{
		     log.info("forViewAutocomplete:signObject:"+signObject);
		     if(signObject!=null){
		    	 IspBssT ao = (IspBssT)entityManager.createQuery(
		    				"select o from IspBssT o where o.status='A' " +
		    				"and o.signObject = :signObject ")
		    		    	.setParameter("signObject", signObject)
		    		    	.getSingleResult();
		    	/* IspBssT ao = new IspBssT();
		    	 ao.setSignObject(signObject);*/
		    	 
		    	 Contexts.getEventContext().set("clOrgBean", ao);
		   	 }
		   }catch(Exception e){
			   log.error("forViewAutocomplete:Error:"+e);
		   }
   }
   
   public Boolean getEvaluteForList() {
	
   	log.info("clOrgManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("clOrgManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
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
		   log.info("clOrgManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("clOrgManager:evaluteForListFooter!!!");
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
			    log.info("clOrgManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("clOrgManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("clOrgManager:evaluteForBean!!!");
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
