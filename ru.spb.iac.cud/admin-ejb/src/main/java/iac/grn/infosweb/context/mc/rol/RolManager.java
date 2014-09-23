package iac.grn.infosweb.context.mc.rol;

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
import iac.cud.infosweb.entity.AcLinkRoleAppPagePrmssn;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.AcPermissionsList;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.GroupUsersKnlT;
import iac.cud.infosweb.entity.LinkGroupUsersUsersKnlT;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.AuditExportData;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
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
@Name("rolManager")
public class RolManager {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * Экспортируемая сущности 
     * для отображения
     */
	//private BaseItem usrBean;             !!! Проверить !!!
	
		
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private List<AcApplication> listRolArm = null;
	private List<AcAppPage> listRolRes = null;
	private List<AcAppPage> listRolResEdit = null;
	private List<AcPermissionsList> listRolPerm = null;
	private List<Long> checkboxPerm = null;
	
	private List<AcApplication> listArm = null;
	
	//private List<AcApplication> listArmFull = null;
	
	private List<AcApplication> listArmUgroup = null;
	
	private boolean roleCodeExist=false;
	
	private String dellMessage=null;
	
	private List<BaseItem> usrAlfList;
	
	private List<AcUser> usrSelectListForView;
	
	//private boolean updRoleCodeExist=false;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> rolListCached = (List<BaseItem>)
			  Component.getInstance("rolListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    rolListCached!=null){
		 		log.info("getAuditList:02:"+rolListCached.size());
			    	this.auditList=rolListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("rolListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecRol = (ArrayList<String>)
					  Component.getInstance("selRecRol",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecRol!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecRol.contains(it.getBaseId().toString())){
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
			 
			 RolStateHolder rolStateHolder = (RolStateHolder)
					  Component.getInstance("rolStateHolder",ScopeType.SESSION);
			 
			 HashMap<String, String> filterMap = rolStateHolder.getColumnFilterValues();
			 String st=null;
			 
			 if(type.equals("list")){
				 log.info("invokeLocal:list:01");
				 
				
				 
				 Set<Map.Entry<String, String>> set = rolStateHolder.getSortOrders().entrySet();
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
    	   		       if(me.getKey().equals("acApplication")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	               }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);
                 
				
                AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		 
 	    		if(au.getAllowedSys()!=null){
 	    			 auditList = entityManager.createQuery("select o from AcRole o "+
 	    					"where o.acApplication in (:idsArm) "+
 	    					(st!=null ? " and "+st :"")+
 	    				    (orderQuery!=null ? orderQuery+", o.idRol " : " order by o.idRol "))
 	                           .setFirstResult(firstRow)
 	                           .setMaxResults(numberOfRows)
 	                           .setParameter("idsArm", au.getAllowedSys())
 	                           .getResultList();
 	    		}else{
 	    			 auditList = entityManager.createQuery("select o from AcRole o "+
 	    					(st!=null ? " where "+st :"")+
 	    				    (orderQuery!=null ? orderQuery+", o.idRol " : " order by o.idRol "))
 	                           .setFirstResult(firstRow)
 	                           .setMaxResults(numberOfRows)
 	                           .getResultList();
 	    		}
				
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
  	   		         if(me.getKey().equals("acApplication")){  
  	   		    	   st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
  	    	         }else{
  	        		  // st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
  	        		  //делаем фильтр на начало
  	            	    st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
  	            	   }
    	             }
    	    	   }
                 log.info("invokeLocal:count:filterQuery:"+st);
				 
                 AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		 
  	    		 if(au.getAllowedSys()!=null){
					 auditCount = (Long)entityManager.createQuery(
							 "select count(ar) " +
					         "from AcRole ar " +
					         "where ar.acApplication in (:idsArm) "+
					         (st!=null ? " and "+st :""))
					        .setParameter("idsArm", au.getAllowedSys())
			                .getSingleResult();
  	    		 }else{
  	    			auditCount = (Long)entityManager.createQuery(
							 "select count(ar) " +
					         "from AcRole ar " +
					         (st!=null ? " where "+st :""))
			                .getSingleResult();
  	    		 }
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
			if(modelType.equals("rolDataModel")){
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
		 AcRole ar = searchBean(sessionId);
		 
		 Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
			
		 if(ar.getAcApplication().equals(appCode)){
	    		log.info("forView:setCudRole");
	    		ar.setIsCudRole(1L);
	    	if(ar.getSign().equals("role:urn:sys_admin_cud")){
	 	    	 log.info("forView:setSysAdmiRole");
	 	    	 ar.setIsSysAdminRole(1L);
	 	     }	
	     }
		 
		 
		 Contexts.getEventContext().set("rolBean", ar);
	  }
   }
   
   private AcRole searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<AcRole> rolListCached = (List<AcRole>)
				  Component.getInstance("rolListCached",ScopeType.SESSION);
		if(rolListCached!=null){
			for(AcRole it : rolListCached){
				 
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
   
   public void addRol(){
	   log.info("hostsManager:addUsr:01");
	   
	   List<AcLinkRoleAppPagePrmssn> arList = new ArrayList<AcLinkRoleAppPagePrmssn>();
	   AcRole rolBeanCrt = (AcRole)
				  Component.getInstance("rolBeanCrt",ScopeType.CONVERSATION);
	   
	   if(rolBeanCrt==null){
		   return;
	   }
	 
	   try {
		   
		   if(!roleCodeExistCrt(rolBeanCrt.getAcApplication(), rolBeanCrt.getSign().trim())){
				 
		   
		      AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		      rolBeanCrt.setRoleTitle(rolBeanCrt.getRoleTitle().trim());
		      rolBeanCrt.setSign(rolBeanCrt.getSign().trim());
		      
		      if(rolBeanCrt.getRoleDescription()!=null&&!rolBeanCrt.getRoleDescription().trim().equals("")){
		    	  rolBeanCrt.setRoleDescription(rolBeanCrt.getRoleDescription().trim());
			  }else{
				  rolBeanCrt.setRoleDescription(null);
			  }
		      
		      rolBeanCrt.setCreator(au.getIdUser());
		      rolBeanCrt.setCreated(new Date());
	    	  entityManager.persist(rolBeanCrt);
	    	   
	    	  for(AcAppPage res:listRolRes){
	    		  log.info("RolManager:addRol:Res:"+res.getPageName());
	    		  for(Object l:res.getPermList()){
	    			  log.info("RolManager:addRol:perm:"+l.toString());
	    			  AcLinkRoleAppPagePrmssn ap = new AcLinkRoleAppPagePrmssn(res.getIdRes(), new Long(l.toString()), rolBeanCrt.getIdRol());
	    			  ap.setCreated(new Date());
	    			  ap.setCreator(new Long(1));
	    			  arList.add(ap);
	    			//  entityManager.persist(ap);
	    		  }
	    	  }
	    	   /*  for(AcApplication arm:listUsrArm){
		    		  log.info("UsrManager:addUsr:Arm:"+arm.getName());
		    		  for(AcRole rol:arm.getAcRoles()){
		    			  log.info("UsrManager:addUsr:RolTitle:"+rol.getRoleTitle());
		    			  log.info("UsrManager:addUsr:RolChecked:"+rol.getUsrChecked());
		    			  
		    			  if(rol.getUsrChecked().booleanValue()){
		    				  
		    				 if(rol.getRaions()!=null&&!rol.getRaions().isEmpty()){
		    					 for(Object idRai: rol.getRaions()){
		    					   AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(new Long(idRai.toString()), rol.getIdRol(), usrBeanCrt.getIdUser());
			    			       au.setCreated(new Date());
			    			       au.setCreator(new Long(1));
			    			       //entityManager.persist(au);
			    			       arList.add(au);
			    			      }
		    				 }else{
		    			       AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(new Long(-1), rol.getIdRol(), usrBeanCrt.getIdUser());
		    			       au.setCreated(new Date());
		    			       au.setCreator(new Long(1));
		    			      // entityManager.persist(au);
		    			       arList.add(au);
		    			      }
		    			  }
		    		  }
		    	  }*/
	    	     
	    	     if(arList.size()>0){
	    	 		//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    	 	     rolBeanCrt.setAcLinkRoleAppPagePrmssns(new HashSet(arList));
	    	 	 }
	    	 	   
	    	 	 entityManager.flush();
	    	  	 entityManager.refresh(rolBeanCrt);
	    	   
	    	  	audit(ResourcesMap.ROLE, ActionsMap.CREATE); 
	    	  	 
	           }
	    	  	 
	          }catch (Exception e) {
	             log.error("RolManager:addRol:ERROR="+e);
	          }
	   
   }
   
   public void updRol(){
	   
	   log.info("hostsManager:updHosts:01");
	   
	   List<AcLinkRoleAppPagePrmssn> arList = new ArrayList<AcLinkRoleAppPagePrmssn>();
	   AcRole rolBean = (AcRole)
				  Component.getInstance("rolBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("rolManager:updRol:sessionId:"+sessionId);
	
	   if(rolBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		 
		 if(!roleCodeExistUpd(rolBean.getAcApplication(), rolBean.getSign().trim(), new Long(sessionId))){
		   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		 //	entityManager.merge(acUsrBean);
		  AcRole arm = entityManager.find(AcRole.class, new Long(sessionId));
		  
		  arm.setRoleTitle(rolBean.getRoleTitle().trim());
		  arm.setSign(rolBean.getSign().trim());
		  
		  if(rolBean.getRoleDescription()!=null&&!rolBean.getRoleDescription().trim().equals("")){
			  arm.setRoleDescription(rolBean.getRoleDescription().trim());
		  }else{
			  arm.setRoleDescription(null);
		  }
		  
		  arm.setAcApplication(rolBean.getAcApplication());
		
		  
		  arm.setModificator(au.getIdUser());
		  arm.setModified(new Date());
		  
		  for(AcLinkRoleAppPagePrmssn apl : arm.getAcLinkRoleAppPagePrmssns()){
			   entityManager.remove(apl);
		  }
		  arm.setAcLinkRoleAppPagePrmssns(null);
		   
		  entityManager.flush();
		  
		  for(AcAppPage res:listRolResEdit){
    		  log.info("RolManager:editRol:Res:"+res.getPageName());
    		  for(Object l:res.getPermList()){
    			  log.info("RolManager:editRol:perm:"+l.toString());
    			  AcLinkRoleAppPagePrmssn ap = new AcLinkRoleAppPagePrmssn(res.getIdRes(), new Long(l.toString()), new Long(sessionId));
    			  ap.setCreated(new Date());
    			  ap.setCreator(new Long(1));
    			 // entityManager.persist(ap);
    			  arList.add(ap);
    		  }
    	  }
	    	  
	       	  if(arList.size()>0){
	    	 	//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    		  arm.setAcLinkRoleAppPagePrmssns(new HashSet(arList));
	    	  }
	    	 	   
	    	 entityManager.flush();
	    	 entityManager.refresh(arm);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	    	 Contexts.getEventContext().set("rolBean", arm);
	    	 
	    	 audit(ResourcesMap.ROLE, ActionsMap.UPDATE); 
	    	 
	       }
	    	 
	     }catch (Exception e) {
           log.error("RolManager:updRol:ERROR:"+e);
         }
   }
 
  public void updUgroupUserAlf(){
	   
	   log.info("rolManager:updUgroupUserAlf:01");
	   
	   AcLinkUserToRoleToRaion lguu=null;
	   
	   AcRole roleBean = (AcRole)
				  Component.getInstance("rolBean",ScopeType.CONVERSATION);
	   
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("rolManager:updUgroupUserAlf:sessionId:"+sessionId);
	   
	   if(roleBean==null || sessionId==null){
		   return;
	   }
	
	 	   
	   try {
		   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		   AcRole aum = entityManager.find(AcRole.class, new Long(sessionId));
		   
		   List<AcLinkUserToRoleToRaion> oldLinkList = aum.getAcLinkUserToRoleToRaions();
		   
		 // log.info("rolManager:updUgroupUserAlf:size1:"+(this.usrAlfList!=null?this.usrAlfList.size():"null"));
		 // log.info("rolManager:updUgroupUserAlf:size2:"+(oldLinkList!=null?oldLinkList.size():"null"));
		   
		   for(BaseItem user:this.usrAlfList){
 			 // log.info("ugroupManager:updUgroupUserAlf:Login:"+((AcUser)user).getLogin());
 			//  log.info("ugroupManager:updUgroupUserAlf:UsrChecked:"+((AcUser)user).getUsrChecked());
 			  
 			  if(((AcUser)user).getUsrChecked().booleanValue()){ //отмечен
 				
 				 log.info("rolManager:updUgroupUserAlf:02:"+((AcUser)user).getLogin());
 				 
 				//  if(listIdUsr.contains(user.getBaseId())){ //база и так содержит
 				 
 				 lguu=new AcLinkUserToRoleToRaion(new Long(sessionId), user.getBaseId());
 				 if(oldLinkList.contains(lguu)){  
 				 
 					log.info("rolManager:updUgroupUserAlf:03");
 					
 				 }else{//нет в базе
 					 
 					log.info("rolManager:updUgroupUserAlf:04");
 					 
 					 lguu.setCreated(new Date()); 
 					 lguu.setCreator(au.getIdUser());
			         
			         oldLinkList.add(lguu);
			         
 					// entityManager.persist(au);
 				  }
 				  
 			  }else{//не отмечен
 				// if(listIdUsr.contains(user.getBaseId())){ //есть в базе
 				  
 				 lguu=new AcLinkUserToRoleToRaion(new Long(sessionId), user.getBaseId());
 				 if(oldLinkList.contains(lguu)){
 					 
 					log.info("rolManager:updUgroupUserAlf:05");
 					 
 					oldLinkList.remove(lguu);
 					entityManager.createQuery("DELETE FROM AcLinkUserToRoleToRaion gu " +
 							                  "WHERE gu.pk.acRole=:acRole " +
 							                  "and gu.pk.acUser=:acUser ")
 					    .setParameter("acRole", new Long(sessionId))
 					    .setParameter("acUser", user.getBaseId())
 					    .executeUpdate();
				  }else{//в базе и так нет
				 
					//  log.info("rolManager:updUgroupUserAlf:06");
					  
				  }
 			  }
 		  }
			   
  		   
	        entityManager.flush();
	    	
		    entityManager.refresh(aum);
	    	  
	    	Contexts.getEventContext().set("rolBean", aum);
	    	 
	    	audit(ResourcesMap.ROLE, ActionsMap.UPDATE); 
	    	
	    	
	     }catch (Exception e) {
         log.error("rolManager:updUgroupUserAlf:ERROR:"+e);
       }
 }
   
   
   public void delRol(){
	 try{
		log.info("rorManager:delRols:01");  
		
		AcRole rolBean = (AcRole)
				  Component.getInstance("rolBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{usrBean.idUser}"/>
		
		if(rolBean==null){
			return;
		}
		 
		log.info("rolManager:delRol:IdRol:"+rolBean.getIdRol());
		
		AcRole arm = entityManager.find(AcRole.class, rolBean.getIdRol());
		  
		entityManager.remove(arm);
		
		audit(ResourcesMap.ROLE, ActionsMap.DELETE); 
		
	 }catch(Exception e){
		 log.error("rolManager:delRol:error:"+e); 
	 }
   }
 
   public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 AcRole ar = entityManager.find(AcRole.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("rolBean", ar);
	    	 
	    	//устанавливаем на 1 страницу пагинатор в модальном окне
	    	 RolStateHolder rolStateHolder = (RolStateHolder)
					  Component.getInstance("rolStateHolder",ScopeType.SESSION);
	    	 rolStateHolder.resetPageNumber();
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

			  List<Object[]> lo=entityManager.createNativeQuery(
	  			      "select  t1.cnt usr_cnt, t2.cnt gusr_cnt "+
                      "from " +
                      "(select count(*) cnt "+
                      "from AC_USERS_LINK_KNL_T usl "+
                      "where USL.UP_ROLES=? ) t1, "+
                      "(select count(*) cnt "+
                      "from LINK_GROUP_USERS_ROLES_KNL_T gusl "+
                      "where GUSL.UP_ROLES=? ) t2 ")
	  				.setParameter(1, new Long(sessionId))
	  				.setParameter(2, new Long(sessionId))
	  				.getResultList();
			  
			  if(!lo.isEmpty()){
				  if(!lo.get(0)[0].toString().equals("0")){
					  dellMessage="Есть пользователи с этой ролью!<br/>";
				  }
				  if(!lo.get(0)[1].toString().equals("0")){
					  if(dellMessage!=null){
					   dellMessage+="Есть группы пользователей с этой ролью!";
					  }else{
						dellMessage="Есть группы пользователей с этой ролью!";
					  }
				  }
			  }
			 AcRole ar = entityManager.find(AcRole.class, new Long(sessionId));	 
			 Contexts.getEventContext().set("rolBean", ar);
		 }	
  }
   
  public List<AcApplication> getListRolArm() throws Exception{
	    log.info("RolManager:getListRolArm:01");
	    try {
	    	if(listRolArm==null){
	     		listRolArm=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("RolManager:getListRolArm:ERROR:"+e);
	         throw e;
	     }
	    return listRolArm;
 }
   public void setListRolArm(List<AcApplication> listRolArm){
	   this.listRolArm=listRolArm;
   }
   
   public List<AcAppPage> getListRolRes() throws Exception{
	   // log.info("RolManager:getListRolRes:arm:"+this.arm);
	    try {
	    	log.info("RolManager:getListRolRes:01");
	    
	    	//При заходе создать - список должен быть пуст,
	    	//т.к. АРМ ещё не выбран
	    	
	    	//у h:selectOneRadio immediate="true", иначе -
	    	//acRolBeanCrt будет null
	    	String pidArm=null;
	    	
	    	AcRole rolBeanCrt = (AcRole)
					  Component.getInstance("rolBeanCrt",ScopeType.CONVERSATION);
		   
	    	if(rolBeanCrt!=null){
	    	
	    	if(rolBeanCrt.getAcApplication()==null){
	    		 pidArm= FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("CBformCrt:appl"); //идАРМ при ajax смене АРМа
	    		// .get("CBformCrt:alCrt");
	    		 log.info("RolManager:getListRolRes:pidArm:"+pidArm);
	    	 }
	    		    	
	    	if(listRolRes==null&&(rolBeanCrt.getAcApplication()!=null||pidArm!=null)){
	    		log.info("RolManager:getListRolRes:02");
	    		//идём сюда:
	    		//1)при ajax смене АРМ(pidArm!=null)
	    		//2)при нажатии Сохранить (pidArm!=null)
	    		listRolRes=entityManager.createQuery("select o from AcAppPage o where " +
	    				"o.idResCollection is empty and "+
	    				"o.visible=1 and "+
	    		    	"o.acApplication = :idArm and " +
	    				"o.idParent2 !=1 and o.pageCode is not null ")
	    				.setParameter("idArm", (pidArm!=null ? new Long(pidArm) : rolBeanCrt.getAcApplication()))
	    				.getResultList();
	   		   for(AcAppPage aap:listRolRes){
	   			  log.info("RolManager:getListRolRes:Cicle:1");
	   			  String st=aap.getPageName();
	   			  AcAppPage aapin=aap.getIdParent();
	   			   while(!aapin.getIdParent2().equals(new Long(1))){
	   				log.info("RolManager:getListRolRes:Cicle:2");
	   				   st=aapin.getPageName()+"/"+st;
	   				   aapin=aapin.getIdParent();
	   			   }
	   			   aap.setFullPageName(st);
	   		   }
	    		
	    	}
	       }
	    	log.info("RolManager:getListRolRes:03");
	     } catch (Exception e) {
	    	 log.error("RolManager:getListRolRes:ERROR:"+e);
	         throw e;
	     }
	    return listRolRes;
   }
   
   public void setListRolRes(List<AcAppPage> listRolRes){
	   this.listRolRes=listRolRes;
   }
   
   public List<AcAppPage> getListRolResEdit() throws Exception{
	   // log.info("RolManager:getListRolResEdit:arm:"+this.arm);
	  /*  String pscipAllFlag= FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("scipAllFlag");
	    	
	   if(pscipAllFlag==null){ */
	    try {
	    	
	    	// при заходе со списка - 1)идАРМ = editRol.getAcApplication(),
	    	// 2)идРоли = pidRol (оно же и editRol.idRol)
	        // при ajax смене АРМ - 1)идАРМ = pidArm,
	    	// 2)идРоли = idForAjax
	    	// при нажатии Сохранить - 1)идАРМ = pidArm,
	    	// 2)идРоли = idForAjax -не используем, так как устанавливаем saveEditFlag=1
	    	
	    	String idRol = FacesContext.getCurrentInstance().getExternalContext()
			       .getRequestParameterMap()
			       .get("sessionId");
		    log.info("rolManager:getListUsrArmEdit:sessionId:"+idRol);
		    
		  /*  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			       .getRequestParameterMap()
			       .get("remoteAudit");
		    log.info("rolManager:getListUsrArmEdit:remoteAudit:"+remoteAudit);*/
		  
	    	//pidRol -global переменная!!!
	    	String pidArm=null, saveEditFlag,idForAjax;
	    	
	    	AcRole rolBean = (AcRole)
					  Component.getInstance("rolBean", ScopeType.EVENT);
	    	
	    	//log.info("RolManager:getListRolResEdit:editRol.getAcApplication:"+rolBean.getAcApplication());
	    	
	    	if(rolBean.getAcApplication()==null){ //при заходе со списка условие = нет
	    		 pidArm= FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("CBformUpd:appl"); //идАРМ при нажатии кнопки сохранить
	    		// .get("CBformUpd:alUpd")
	    		 log.info("RolManager:getListRolResEdit:pidArm:"+pidArm);
	       	}
	    	if(listRolResEdit==null&&(rolBean.getAcApplication()!=null||pidArm!=null)){
	    		//идём сюда:
		    	//1)при заходе со списка (editRol.getAcApplication()) 	
	    		//2)при ajax смене АРМ (pidArm!=null)
	    		//3)при нажатии Сохранить (pidArm!=null)
		    	
	    		listRolResEdit=entityManager.createQuery("select o from AcAppPage o where " +
	    				"o.idResCollection is empty and " +
	    				"o.visible=1 and "+
	    				"o.acApplication = :idArm and " +
	    				"o.idParent2 !=1 and o.pageCode is not null ")
	    				.setParameter("idArm",(pidArm!=null ? new Long(pidArm) : rolBean.getAcApplication()))
	    				.getResultList();
	    		saveEditFlag= FacesContext.getCurrentInstance().getExternalContext()
	 			        .getRequestParameterMap()
	 			        .get("saveEditFlag");
	    		 log.info("RolManager:getListRolResEdit:saveEditFlag:"+saveEditFlag);
	    		 
	    		 idForAjax= FacesContext.getCurrentInstance().getExternalContext()
	 			        .getRequestParameterMap()
	 			      //  .get("CBformUpd:idForAjax");
	    		         .get("idForAjax");
	    		 log.info("RolManager:getListRolResEdit:idForAjax!!!:"+idForAjax);
	    		
	    	 	 
	    	  if(saveEditFlag==null){
	    		  log.info("RolManager:getListRolResEdit:01");
	    		  //здесь всегда, кроме нажатия кнопки Сохранить - там нам история не нужна, 
	    		  //у нас теперь новые значения с формы
	           for(AcAppPage aca :listRolResEdit){
	        	   log.info("RolManager:getListRolResEdit:02:1");
	        	   
	        		  String st=aca.getPageName();
		   			  AcAppPage aapin=aca.getIdParent();
		   			   while(!aapin.getIdParent2().equals(new Long(1))){
		   				   st=aapin.getPageName()+"/"+st;
		   				   aapin=aapin.getIdParent();
		   			   }
		   			   aca.setFullPageName(st);
		   			 log.info("RolManager:getListRolResEdit:02:2");
		   			 
	        	if(aca.getAcLinkRoleAppPagePrmssns()!=null){ 
	        		 log.info("RolManager:getListRolResEdit:03");
	        	 Iterator it= aca.getAcLinkRoleAppPagePrmssns().iterator();
	        	 List<Long> ls = new ArrayList<Long>();
	        	 log.info("RolManager:getListRolResEdit:04");
	        	 while (it.hasNext()){
	        		 log.info("RolManager:getListRolResEdit:05");
	            AcLinkRoleAppPagePrmssn me = (AcLinkRoleAppPagePrmssn) it.next();
	            log.info("RolManager:getListRolResEdit:06");
		            if((me.getAcAppPage().getIdRes().equals(aca.getIdRes())) && 
		               (me.getAcRole().getIdRol().equals(new Long((idRol!=null?idRol:idForAjax)))) ){
		        	   log.info("RolManager:getListRolResEdit:IdPerm:"+me.getAcPermissionsList().getIdPerm());
		        	 ls.add(me.getAcPermissionsList().getIdPerm());
		         }
       	        } 
	        	 aca.setPermList(ls);
	           }
	           
	           }
	    	  }
	    	}
	     } catch (Exception e) {
	    	 log.error("getListRolResEdit:ERROR="+e);
	         throw e;
	     }
	  //  }
	    return listRolResEdit;
   }
   public void setListRolResEdit( List<AcAppPage> listRolResEdit ){
	   log.info("RolManager:setListRolResEdit");
	   this.listRolResEdit=listRolResEdit;
   }

   public List<AcPermissionsList> getListRolPerm() throws Exception{
	    log.info("getListRolPerm_01");
	    try {
	    	if(listRolPerm==null){
	     		listRolPerm=entityManager.createQuery("select o from AcPermissionsList o").getResultList();
	       	}
	    	if(listRolPerm!=null){
	    	  log.info("getListRolPerm:size:"+listRolPerm.size());
	    	}else{
	    		 log.info("getListRolPerm:size:_null");
	    	}
	     } catch (Exception e) {
	    	 log.error("getListRolPerm_ERROR="+e);
	         throw e;
	     }
	    return listRolPerm;
  }
  public List<Long> getCheckboxPerm() throws Exception{
	    log.info("getCheckboxPerm_01");
	    if (checkboxPerm==null){
	    	checkboxPerm = new ArrayList();
	    	checkboxPerm.add(new Long(1));
	    	checkboxPerm.add(new Long(3));
	    	checkboxPerm.add(new Long(5));
	       }
	    return checkboxPerm;
  }
  public void setCheckboxPerm(List<Long> checkboxPerm) throws Exception{
	    log.info("setCheckboxPerm_01");
	    this.checkboxPerm=checkboxPerm;
  }
  
  
  public List<AcUser> getUsrSelectListForView(){
  	
	  List<Object[]> lo=null;
      try{
     		
     	if(this.usrSelectListForView==null){
     		String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
 	             .getRequestParameterMap()
 	             .get("remoteAudit");
     		String sessionId = FacesContext.getCurrentInstance().getExternalContext()
    	             .getRequestParameterMap()
    	             .get("sessionId");
     		
     		if(remoteAudit==null||sessionId==null){
     			return null;
     		}
     		
 	        log.info("rolManager:getUsrSelectListForView:remoteAudit:"+remoteAudit);
 	        log.info("rolManager:getUsrSelectListForViewt:sessionId:"+sessionId);
 	        
 	        //if(!remoteAudit.equals("UpdFact")){
 	 	        
 	       lo=entityManager.createNativeQuery(
		    	   "select t1.t1_id, t1.t1_login, t1.t1_fio "+
                   "from (select AU_FULL.ID_SRV t1_id, AU_FULL.LOGIN t1_login, "+  
                  "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) t1_fio "+
                     "from "+ 
                     "AC_USERS_KNL_T AU_full, "+ 
                     "AC_USERS_LINK_KNL_T uul, "+ 
                     "ISP_BSS_T CL_USR_FULL, "+
                     "(select max(CL_usr.ID_SRV) CL_USR_ID,  CL_USR.SIGN_OBJECT  CL_USR_CODE "+
                         "from ISP_BSS_T cl_usr, "+ 
                         "AC_USERS_KNL_T au "+ 
                        "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
                        "group by CL_usr.SIGN_OBJECT) t2 "+   
                     "where  AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
                     "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
                     "and UUL.UP_USERS= AU_FULL.ID_SRV "+ 
                     "and UUL.UP_ROLES=? "+
                     //!!!
					 "and AU_FULL.STATUS !=3 "+
                     "order by t1_fio "+ 
                     ") t1 ")
		      		.setParameter(1, new Long(sessionId))
				 .getResultList();
 	    	//}
 	         
 	       this.usrSelectListForView=new ArrayList<AcUser>();
 	       
 	       for(Object[] objectArray :lo){
 	    	 AcUser au = new AcUser();
 	    	 
 	    	 this.usrSelectListForView.add(au);
 	    	 
 	    	 //не используется при только отображении
 	    	 //au.setIdUser(new Long(objectArray[0].toString()));
 	    	 au.setFio(objectArray[2]!=null?objectArray[2].toString():"");
 	    	 au.setLogin(objectArray[1]!=null?objectArray[1].toString():"");
 	       }
 	         
     	 }
     	}catch(Exception e){
 			log.error("rolManager:getUsrSelectListForView:error:"+e);
 		}
        	return this.usrSelectListForView;
     }
  
  
   public int getConnectError(){
	   return connectError;
   }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		  // log.info("getAuditItemsListSelect:01");
	
	    RolContext ac= new RolContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("roleTitle"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("sign"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("roleDescription"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("armName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("usrManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   RolContext ac= new RolContext();
		   auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
   /*
   public List<AcApplication> getListArm() throws Exception{
	    log.info("roleManager:getListArm:01");
	    try {
	    	if(listArm==null){
	    		
	    		String query="select o from AcApplication o ";
	    		
	    		AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
				
	    		if(!cau.getIsSysAdmin().equals(1L)){
	    			query+="where o.idArm!="+appCode;
	    		}
	    		
	    		
	    		
	    		listArm=entityManager.createQuery(query).getResultList();
	    		
	       		//listArm=entityManager.createQuery("select o from AcApplication o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("roleManager:getListArm:ERROR:"+e);
	         throw e;
	     }
	    return listArm;
  }*/
   
   public List<AcApplication> getListArm() throws Exception{
	    log.info("roleManager:getListArm:01");
	    try {
	    	if(listArm==null){
	    		
	    		String query = null;
	    		
	    		AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
				
	    		if(!cau.getIsSysAdmin().equals(1L)){
	    			query=" o.idArm!="+appCode;
	    		}
	    		
	    		if(cau.getAllowedSys()!=null){
	    			listArm=entityManager.createQuery(
	    					"select o from AcApplication o "+
	    				    "where o.idArm in (:idsArm) " +
	    				    (query!=null?" and "+query:" "))
	       				    .setParameter("idsArm", cau.getAllowedSys())
	    					.getResultList();
	    		}else{
	    			listArm=entityManager.createQuery(
	    					"select o from AcApplication o "+
	    					(query!=null?" where "+query:" "))
	    					.getResultList();
	    		}
	    		
	      	}
	     } catch (Exception e) {
	    	 log.error("roleManager:getListArm:ERROR:"+e);
	         throw e;
	     }
	    return listArm;
 }
  
   
 /*
  //используется  armManager.listArm
   public List<AcApplication> getListArmFull() throws Exception{
	    log.info("roleManager:getListArmFull:01");
	    try {
	    	if(listArmFull==null){
	    		
	    		String query="select o from AcApplication o ";
	    		
	    		listArmFull=entityManager.createQuery(query).getResultList();
	    		
	    	}
	     } catch (Exception e) {
	    	 log.error("roleManager:getListArmFull:ERROR:"+e);
	         throw e;
	     }
	    return listArmFull;
 }*/
   
   public List<AcApplication> getListArmUgroup() throws Exception{
	   
	   //вариант жоще, чем getListArm
	   //вообще убираем ЦУД из списка армов
	   //даже для суперпользователя
	   //то есть через группы роли цуда не назначаются
	   
	    log.info("roleManager:getListArmUgroup:01");
	    try {
	    	if(listArmUgroup==null){
	    		
	    		AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    		Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
				
	    		String query="select o from AcApplication o where o.idArm!="+appCode+" ";
	    		
	    		if(cau.getAllowedSys()!=null){
	    			listArmUgroup=entityManager.createQuery(
	    					      query+
	    					      " and o.idArm in (:idsArm) " )
	    					      .setParameter("idsArm", cau.getAllowedSys())
                                  .getResultList();
	    		}else{
	    			listArmUgroup=entityManager.createQuery(query).getResultList();
	    		}
	    		
	    		
	    	}
	     } catch (Exception e) {
	    	 log.error("roleManager:getListArmUgroup:ERROR:"+e);
	         throw e;
	     }
	    return listArmUgroup;
  }
  
   public List<BaseItem> getUsrAlfList(){
	   
	   //!!!
	  //не используется 
	   
   	log.info("rolManager:getUsrAlfList:01");
   	
   	List<Object[]> lo=null;
   	String alfDiap=null;
   	
   	if(this.usrAlfList==null){
   		
   		String idAlf = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("idAlf");
 		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 		   String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("remoteAudit");
 		   log.info("rol:getUsrAlfList:idAlf:"+idAlf);
 		   log.info("rol:getUsrAlfList:sessionId:"+sessionId);
 		   log.info("rol:getUsrAlfList:remoteAudit:"+remoteAudit);
 		   
 		   if(sessionId==null){
 			  return this.usrAlfList;
 		   }
 		   
 		   
 		   if(idAlf==null||idAlf.equals("1")){
 			 alfDiap="А-ЕЁ";
 		   }else if(idAlf.equals("2")){
 			 alfDiap="Ж-Л";
 		   }else if(idAlf.equals("3")){
 			 alfDiap="М-Т";
 		   }else if(idAlf.equals("4")){
 			 alfDiap="У-Ш";
 		   }else if(idAlf.equals("5")){
 			 alfDiap="Щ-Я";
 		   }else{
 			 alfDiap="А-ЕЁ"; 
 		   }
 		   
 		   
   		lo=entityManager.createNativeQuery(
  		    		"select t1.t1_id, t1.t1_login, t1.t1_fio "+
                   "from ( "+
                   "select  AU_FULL.ID_SRV t1_id, AU_FULL.LOGIN t1_login, "+           
                   "decode(AU_FULL.UP_SIGN_USER, null, AU_FULL.SURNAME||' '||AU_FULL.NAME_ ||' '|| AU_FULL.PATRONYMIC,  CL_USR_FULL.FIO) t1_fio "+ 
                   "from  AC_USERS_KNL_T AU_FULL, "+ 
                   "ISP_BSS_T cl_usr_full, "+
                   "(select max(CL_usr.ID_SRV) CL_USR_ID, CL_USR.SIGN_OBJECT CL_USR_CODE "+
                   "from ISP_BSS_T cl_usr, "+ 
                   "AC_USERS_KNL_T au "+ 
                   "where AU.UP_SIGN_USER  = CL_usr.SIGN_OBJECT "+ 
                   "group by CL_usr.SIGN_OBJECT) t2 "+ 
                   "where  AU_FULL.UP_SIGN_USER=t2.CL_USR_CODE(+) "+ 
                   "and CL_USR_FULL.ID_SRV(+)=t2.CL_USR_ID "+
                   "and AU_FULL.STATUS!=2 "+ 
                   ") t1 " +
                   "where REGEXP_LIKE(UPPER(t1_fio), '^["+alfDiap+"]') "+
                   "order by t1_fio ")
  		    	 .getResultList();
  	    	//}
   		
  	       this.usrAlfList=new ArrayList<BaseItem>();
  	       
  	       for(Object[] objectArray :lo){
  	    	 AcUser au = new AcUser();
  	    	 
  	    	 this.usrAlfList.add(au);
  	    	 
  	    	 au.setIdUser(new Long(objectArray[0].toString()));
  	    	 au.setFio(objectArray[2]!=null?objectArray[2].toString():"");
  	    	 au.setLogin(objectArray[1]!=null?objectArray[1].toString():"");
  	       }
  	       
  	     log.info("RolManager:getUsrAlfList:size1:"+this.usrAlfList.size());
  	      	
  	    if(!"UpdFact".equals(remoteAudit)){
 		   
  	    	     List<Long> listUsr=entityManager.createQuery(
  	 		    		 "select o.idUser from AcUser o,  AcLinkUserToRoleToRaion o1 " +
  	 		    		 "where o1.pk.acUser = o.idUser " +
  	 		    		 "and o1.pk.acRole = :acRole ")
  	 					 .setParameter("acRole", new Long(sessionId))
  	 		      		 .getResultList();
  	 		 
  	    	     log.info("RolManager:getUsrAlfList:size2:"+listUsr.size());
  	    	     
  	 		     for(BaseItem user :this.usrAlfList){
  	 	           if (listUsr.contains(user.getBaseId())){  
  	 	        	  ((AcUser)user).setUsrChecked(true);
  	 			   }
  	 	         } 
  	 		     
  	 	 }
  	  	}
   	return this.usrAlfList;
   }
   public void setUsrAlfList(List<BaseItem> usrAlfList){
 	   this.usrAlfList=usrAlfList;
   }
   
   
   
   public String getStListRolesCodesArm(Long idArm){
	   
	   String result="";
	   
	   log.info("rolManager:getStListRolesCodesArm:idArm:"+idArm);
	   
	   if(idArm==null){
	    	return result;
	   }
	    
	    List<Object> lo=null;
	    
		try{
			
	 	lo=entityManager.createNativeQuery(
	  			"select rl.sign_object "+
                "from AC_ROLES_BSS_T rl "+
                "where rl.up_IS=? ")
			.setParameter(1, idArm)
			.getResultList();
	  	
	       for(Object objectArray :lo){
         	   if(result.equals("")){
        	      result+=objectArray.toString();
        	   }else{
        		  result+="`~"+objectArray.toString();
        	   }
           }
       
		}catch(Exception e){
			 log.error("rolManager:getStListRolesCodesArm:error:"+e);
		} 
	   return result;
   }
   
   public boolean getRoleCodeExist() {
	    return roleCodeExist;
   }
   
  /* public boolean getUpdRoleCodeExist() {
	    return updRoleCodeExist;
   }*/
   
   private boolean roleCodeExistCrt(Long idArm, String roleCode) throws Exception {
		log.info("RoleManager:codeRoleExistCrt:roleCode="+roleCode);
		if(roleCode!=null){
		  try{
			  List<Object> lo=entityManager.createNativeQuery(
	  			      "select rl.sign_object "+
                      "from AC_ROLES_BSS_T rl "+
                      "where rl.up_IS=? "+
                      "and RL.SIGN_OBJECT=? ")
	  				.setParameter(1, idArm)
	  				.setParameter(2, roleCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        roleCodeExist=true;
	          }
			  log.info("RoleManager:roleCodeExistCrt:addLoginExist!");	
			  
		  }catch(Exception e){
	           log.error("RoleManager:roleCodeExistCrt:Error:"+e);
	           throw e;
        }
		}
		return this.roleCodeExist;
  }
   
   private boolean roleCodeExistUpd(Long idArm, String roleCode, Long idRole) throws Exception {
	   
		log.info("RoleManager:codeRoleExistUpd:roleCode:"+roleCode);
		log.info("RoleManager:codeRoleExistUpd:idRole:"+idRole);
		log.info("RoleManager:codeRoleExistUpd:idArm:"+idArm);
		
		if(roleCode!=null){
		  try{
			  List<Object> lo=entityManager.createNativeQuery(
			  			        "select rl.sign_object "+
                                "from AC_ROLES_BSS_T rl "+
                                "where rl.up_IS=? "+
                                "and RL.SIGN_OBJECT=? "+
                                "and RL.ID_SRV !=? ")
			  				.setParameter(1, idArm)
			  				.setParameter(2, roleCode)
			  				.setParameter(3, idRole)
			  				.getResultList();
			  
			  if(!lo.isEmpty()){
				  roleCodeExist=true;
			  }
			  
			  log.info("RoleManager:roleCodeExistUpd:addLoginExist!");		     
		    }catch(Exception e){
	           log.error("RoleManager:roleCodeExistUpd:Error:"+e);
	           throw e;
          }
		}
		return this.roleCodeExist;
 }
  
   public String getDellMessage() {
	   return dellMessage;
   }
   
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  forView(); //!!!
	    ArrayList<String> selRecRol = (ArrayList<String>)
				  Component.getInstance("selRecRol",ScopeType.SESSION);
	    
	    if(selRecRol==null){
	       selRecRol = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	   // AcRole ar = searchBean(sessionId);
	    AcRole ar = new  AcRole();
	   // в getAuditList : else{it.setSelected(false);}
	    
	    if(ar!=null){
	     if(selRecRol.contains(sessionId)){
	    	selRecRol.remove(sessionId);
	    	ar.setSelected(false);
	    	 log.info("selectRecord:02");
	     }else{
	    	selRecRol.add(sessionId);
	    	ar.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecRol", selRecRol);	
	    
	    Contexts.getEventContext().set("rolBean", ar);
	    }
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
	
   	log.info("reposManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("usrManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
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
		   log.info("usrManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		log.info("usrManager:evaluteForListFooter!!!");
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
			    log.info("usrManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("usrManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("usrManager:evaluteForBean!!!");
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
