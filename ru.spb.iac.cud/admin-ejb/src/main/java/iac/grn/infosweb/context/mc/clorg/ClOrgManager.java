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
	
	 private String dellMessage;
	 
	private List<BaseItem> auditList; 
	
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
		 	if(("rowSelectFact".equals(remoteAudit)||
			    "selRecAllFact".equals(remoteAudit)||
			    "clRecAllFact".equals(remoteAudit)||
			    "clSelOneFact".equals(remoteAudit)||
			    "onSelColSaveFact".equals(remoteAudit))&&
			    clOrgListCached!=null){
			    	this.auditList=clOrgListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("clOrgListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	List<String>  selRecOrg = (ArrayList<String>)
					  Component.getInstance("selRecOrg",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecOrg!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecOrg.contains(it.getBaseId().toString())){
					 
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
			 
			 if("list".equals(type)){
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
                 
				 auditList = entityManager.createQuery(
				 // "select o from IspBssT o where o.status='A' and o.signObject like '%00000' " +
					"select o from IspBssT o  " +
					 (orderQuery!=null ? orderQuery : ""))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if("count".equals(type)){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
						// "from IspBssT o where o.status='A' and o.signObject like '%00000' ")
				         "from IspBssT o  ")
		                .getSingleResult();
				 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if("bean".equals(type)){
				 
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
		  
		   
			if(modelType==null){
		    	return ;
		    }
			
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
		   
		 
		  IspBssT aom = entityManager.find(IspBssT.class, new Long(sessionId));
		  
		  aom.setFull(clOrgBean.getFull());
		
		  
		  aom.setModificator(au.getIdUser());
		  aom.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aom);
	    	  
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
		   
	
	    ClOrgContext ac= new ClOrgContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			   
			
			   
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("signObject"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("full"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("fio"));
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
		   
		   
		   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
      
    
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  forV/ew(); //!!!
	    List<String>  selRecOrg = (ArrayList<String>)
				  Component.getInstance("selRecOrg",ScopeType.SESSION);
	    
	    if(selRecOrg==null){
	       selRecOrg = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	  
	   IspBssT ao = new IspBssT();
	
	    
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
    	 
    	   !"OpenCrtFact".equals(remoteAudit)&&	
    	   !"OpenUpdFact".equals(remoteAudit)&&
    	   !"OpenDelFact".equals(remoteAudit)&&
   	       !"onSelColFact".equals(remoteAudit)&&
   	       !"refreshPdFact".equals(remoteAudit)){
    		log.info("reposManager:evaluteForList!!!");
   		    evaluteForList=true;
    	}
   	 }
       return evaluteForList;
   }
   public Boolean getEvaluteForListFooter() {
		
	  
	   	if(evaluteForListFooter==null){
	   		evaluteForListFooter=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("clOrgManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !"protBeanWord".equals(remoteAudit)&&	
	    	   //new-2-	
	   	       !"selRecAllFact".equals(remoteAudit)&&
	   	       !"clRecAllFact".equals(remoteAudit)&&
	   	      // !remoteAudit equals "clSelOneFact"
	   	       !"onSelColSaveFact".equals(remoteAudit)){
	    		  log.info("clOrgManager:evaluteForListFooter!!!");
	   		      evaluteForListFooter=true;
	    	}
	   	 }
	       return evaluteForListFooter;
	   }
   
   public Boolean getEvaluteForBean() {
		
		  
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
		    	   ("rowSelectFact".equals(remoteAudit)||	
		    	    "UpdFact".equals(remoteAudit))){
		    	      log.info("clOrgManager:evaluteForBean!!!");
		   		      evaluteForBean=true;
		    	}
		   	 }
		     return evaluteForBean;
		   }

}

