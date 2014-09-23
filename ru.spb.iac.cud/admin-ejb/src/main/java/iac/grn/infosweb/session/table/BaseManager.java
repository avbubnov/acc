package iac.grn.infosweb.session.table;

import org.jboss.seam.annotations.Name;


	import org.jboss.seam.ScopeType;
	import org.jboss.seam.annotations.In;
	import org.jboss.seam.annotations.Logger;
	import org.jboss.seam.contexts.Contexts;
	import org.jboss.seam.log.Log;
	import iac.cud.infosweb.dataitems.BaseItem;
	import iac.cud.infosweb.entity.AcUser;
import iac.grn.infosweb.context.app.access.AppAccessContext;
	import iac.grn.infosweb.session.audit.export.ActionsMap;
	import iac.grn.infosweb.session.audit.export.AuditExportData;
	import iac.grn.infosweb.session.audit.export.ResourcesMap;
	import iac.grn.infosweb.session.navig.LinksMap;
	import java.util.*;

	import org.jboss.seam.Component;
	import javax.faces.context.FacesContext;
	import javax.persistence.EntityManager;
import iac.grn.serviceitems.BaseTableItem;
import iac.grn.serviceitems.HeaderTableItem;

	/**
	 * Управляющий Бин
	 * @author bubnov
	 *
	 */
	
	//bindListCached -> contextListCached
	//selRecBind -> contextSelRec
	//bindBeanView-> contextBeanView
	
	@Name("baseManager")
	public class BaseManager {
		
		@Logger 
		protected Log log;
		
	    @In 
	    protected EntityManager entityManager;
		 
		protected List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
		
		protected Long auditCount;
		
		protected List <BaseTableItem> auditItemsListSelect;
		
		protected List <BaseTableItem> auditItemsListContext;
		
		//protected HashMap<String, List<BaseTableItem>> headerItemsListContext;
		
		protected List<HeaderTableItem> headerItemsListContext;
		
		protected Boolean evaluteForList;
		protected Boolean evaluteForListFooter;  
		protected Boolean evaluteForBean;
		
		private LinksMap linksMap = null;
		private AcUser currentUser = null;
		
		private String dellMessage = null;
		
		
		public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
		  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("baseManager:getAuditList:remoteAudit:"+remoteAudit);
		  
		  
		  log.info("baseManager:getAuditList:firstRow:"+firstRow);
		  log.info("baseManager:getAuditList:numberOfRows:"+numberOfRows);
		  
		  List<BaseItem> contextListCached = (List<BaseItem>)
				  Component.getInstance("contextListCached",ScopeType.SESSION);
		  if(auditList==null){
			  log.info("baseManager:getAuditList:01");
			 	if((remoteAudit.equals("rowSelectFact")||
				    remoteAudit.equals("selRecAllFact")||
				    remoteAudit.equals("clRecAllFact")||
				    remoteAudit.equals("clSelOneFact")||
				    remoteAudit.equals("onSelColSaveFact"))&&
				    contextListCached!=null){
			 		log.info("baseManager:getAuditList:02:"+contextListCached.size());
				    	this.auditList=contextListCached;
				}else{
					log.info("baseManager:getAuditList:03");
			    	invokeLocal("list", firstRow, numberOfRows, null);
				    Contexts.getSessionContext().set("contextListCached", this.auditList);
				    log.info("baseManager:getAuditList:03:"+this.auditList.size());
				}
			 	
			 	ArrayList<String> contextSelRec = (ArrayList<String>)
						  Component.getInstance("contextSelRec",ScopeType.SESSION);
			 	if(this.auditList!=null && contextSelRec!=null) {
			 		 for(BaseItem it:this.auditList){
					   if(contextSelRec.contains(it.getBaseId().toString())){
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
			
			 log.info("baseManager:invokeLocal");

		}
		  /**
		  * Подготовка сущности Аудит УФМС 
		  * для последующих операций просмотра
		  */
	   public void forView() {
		   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
		  log.info("baseManager:forView:sessionId:"+sessionId);
		   if(sessionId!=null /*&& usrBean==null*/){
			  
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
			 
			//UserItem au = (UserItem)searchBean(sessionId);
			 BaseItem au = searchBean(sessionId);
			 
			/* Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
				
	    	 
		     List<AcRole> rlist = entityManager.createQuery(
		    			"select ar from AcRole ar, AcLinkUserToRoleToRaion alur " +
		    	 		"where alur.acRole = ar and alur.pk.acUser = :acUser " +
		    	 		"and ar.acApplication= :acApplication ")
		    	 		// .setParameter("acUser", au.getIdUser())
		    	 		 .setParameter("acUser", new Long(sessionId))
		    	 		 .setParameter("acApplication", appCode)
		    	 		 .getResultList();
		    	
		    	// log.info("forView:rlist.size:"+rlist.size());
		    	 
		    	if(!rlist.isEmpty()){
		    		log.info("forView:setCudRole");
		    		au.setIsCudRole(1L);
		    		
		    		for(AcRole ar :rlist){
		    			
		    			if (ar.getSign().equals("role:urn:sys_admin_cud")){
		    				au.setIsSysAdmin(1L);
		    				break;
		    			}
		    			
		    		}
		    		
		    }*/
		    	
	     /*    try{
	        	String[] fio = au.getFio().trim().split("\\s+");
		    	
		    	for(int i=0; i<3; i++ ){
					 
					 if(i<fio.length){
						 this.fioArray[i]=fio[i];
					 }
					 
				 }
		      	
		     }catch(Exception e){
		    	  System.out.println("BindManager:forView:split:Error:"+e);
		     }*/
	 
		     Contexts.getEventContext().set("contextBeanView", au);
			 //Contexts.getEventContext().set("usrBean", au);
		     
		  //   AcUser uzp = entityManager.find(AcUser.class, new Long(sessionId));
		   //  Contexts.getEventContext().set("bindBeanViewUzp", uzp);
		  }
	   }
	   
	   protected BaseItem searchBean(String sessionId){
	    	
	      if(sessionId!=null){
	    	 List<BaseItem> contextListCached = (List<BaseItem>)
					  Component.getInstance("contextListCached",ScopeType.SESSION);
			if(contextListCached!=null){
				for(BaseItem it : contextListCached){
					 
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
	   
	   public void add(){
		   log.info("BaseManager:add:01");
		   
	   }
	   
	   public void upd(){
		   
		   log.info("BaseManager:upd:01");
	   }
	   
	   public void del(){
			
				log.info("BaseManager:del:01");  
	   }		
	
	   public void forViewUpdDel() {
		   
		   log.info("forViewUpdDel");
	   } 
	   
	   public List <BaseTableItem> getAuditItemsListSelect() {
	       return this.auditItemsListSelect;
	   }
	   
	   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
			this.auditItemsListSelect=auditItemsListSelect;
	   }
	   
	   public List <BaseTableItem> getAuditItemsListContext() {
		   return this.auditItemsListContext;
	   }
	      
	   public List<HeaderTableItem> getHeaderItemsListContext() {
		   return this.headerItemsListContext;
	   }
	  
	   public void selectRecord(){
		    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
		  //  log.info("selectRecord:sessionId="+sessionId);
		    
		   //  forView(); //!!!
		    ArrayList<String> contextSelRec = (ArrayList<String>)
					  Component.getInstance("contextSelRec",ScopeType.SESSION);
		    
		    if(contextSelRec==null){
		    	contextSelRec = new ArrayList<String>();
		      // log.info("selectRecord:01");
		    }
		    
		   // AcUser au = searchBean(sessionId);
		  //  UserItem au = new UserItem();
		    
		    BaseItem au = new BaseItem();
		    
		  // в getAuditList : else{it.setSelected(false);}
		    
		    if(au!=null){ 
		     if(contextSelRec.contains(sessionId)){
		    	 contextSelRec.remove(sessionId);
		    	au.setSelected(false);
		    	//log.info("selectRecord:02");
		     }else{
		    	 contextSelRec.add(sessionId);
		    	au.setSelected(true);
		    	//log.info("selectRecord:03");
		    }
		    Contexts.getSessionContext().set("contextSelRec", contextSelRec);	
		    
	       // !!! переделано через getIsSelect()
		   // в centerCenterUnit.xhtml contextBeanView.selected  заменено на appSystemManager.isSelect
		   // Contexts.getEventContext().set("contextBeanView", au);
		    }
	   }
	   
	 
	   public boolean getIsSelect(){
		   
	      boolean result=false;
	   
		   try{
			   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
			    log.info("BaseManager:isSelect:sessionId="+sessionId);

			 	ArrayList<String> contextSelRec = (ArrayList<String>)
						  Component.getInstance("contextSelRec",ScopeType.SESSION);
			 	if(contextSelRec!=null) {
			 		if(contextSelRec.contains(sessionId)){
			 			result=true;
			 		}
			 	}
			    
		   }catch(Exception e){
			   log.error("BaseManager:isSelect:error:"+e);
		   }
		   
		   return result;
	   }
	   public LinksMap getLinksMap() {
		   if(this.linksMap==null){
			   linksMap= (LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION);
		   }
		   return linksMap;
	   }
	   
	   public AcUser getCurrentUser() {
		   if(this.currentUser==null){
			   currentUser= (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   }
		   return currentUser;
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
			   log.error("BaseManager:audit:error:"+e);
		   }
	   }
	   
	   public Boolean getEvaluteForList() {
		
	   	log.info("BaseManager:evaluteForList:01");
	   	if(evaluteForList==null){
	   		evaluteForList=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("BaseManager:evaluteForList:remoteAudit:"+remoteAudit);
	     	
	    	if(remoteAudit!=null&&
	    	 
	    	   !remoteAudit.equals("OpenCrtFact")&&	
	    	   !remoteAudit.equals("OpenUpdFact")&&
	    	   !remoteAudit.equals("OpenDelFact")&&
	   	       !remoteAudit.equals("onSelColFact")&&
	   	       !remoteAudit.equals("refreshPdFact")&&
	   	       !remoteAudit.equals("OpenCommentFact")
	   	    ){
	    		log.info("BaseManager:evaluteForList!!!");
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
			   log.info("BaseManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
		     
		    	if(getEvaluteForList()&&
		    	   //new-1-	
		    	   !remoteAudit.equals("protBeanWord")&&	
		    	   //new-2-	
		   	       !remoteAudit.equals("selRecAllFact")&&
		   	       !remoteAudit.equals("clRecAllFact")&&
		   	      // !remoteAudit.equals("clSelOneFact")&&
		   	       !remoteAudit.equals("onSelColSaveFact")){
		    		log.info("BaseManager:evaluteForListFooter!!!");
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
				    log.info("BaseManager:evaluteForBean:remoteAudit:"+remoteAudit);
					String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				             .getRequestParameterMap()
				             .get("sessionId");
				    log.info("BaseManager:evaluteForBean:sessionId:"+sessionId);
			    	if(sessionId!=null && remoteAudit!=null &&
			    	   (remoteAudit.equals("rowSelectFact")||	
			    	    remoteAudit.equals("UpdFact")||
			    	    remoteAudit.equals("CommentFact"))){
			    	      log.info("BaseManager:evaluteForBean!!!");
			   		      evaluteForBean=true;
			    	}
			   	 }
			     return evaluteForBean;
			   }
	
	}

