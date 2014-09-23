package iac.grn.infosweb.context.mc.ugroup;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import java.io.File;


import iac.cud.infosweb.dataitems.AuditFuncItem;
import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.GroupUsersKnlT;
import iac.cud.infosweb.entity.LinkGroupUsersRolesKnlT;
import iac.cud.infosweb.entity.LinkGroupUsersUsersKnlT;
import iac.cud.infosweb.ws.AccessServiceClient;
import iac.grn.infosweb.session.cache.CacheManager;
import iac.grn.infosweb.session.navig.LinksMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.persistence.TemporalType;

import iac.grn.ramodule.entity.VAuditReport;
import iac.grn.serviceitems.BaseTableItem;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;

/**
 * ����������� ���
 * @author bubnov
 *
 */
//@Name("ugroupManager")
public class UgroupManager_19_02_13 {
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * �������������� �������� 
     * ��� �����������
     */
	//private BaseItem usrBean;             !!! ��������� !!!
	
	private String dellMessage;
	 
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	private List<AcUser> usrList;
	
	private List<String> usrSelectList;
	
	private List<String> usrSelectEditList;
	
	private List<AcUser> usrSelectListForView;
	
	private List<BaseItem> roleList;
	
	private List<AcApplication> listGroupArmForView = null;
	
//	private List<AcApplication> listArm = null;
	 
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> ugroupListCached = (List<BaseItem>)
			  Component.getInstance("ugroupListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    ugroupListCached!=null){
		 	//	log.info("getAuditList:02:"+orgListCached.size());
			    	this.auditList=ugroupListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("ugroupListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecArm = (ArrayList<String>)
					  Component.getInstance("selRecUgroup",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecArm!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecArm.contains(it.getBaseId().toString())){
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
				 
				 UgroupStateHolder ugroupStateHolder = (UgroupStateHolder)
						  Component.getInstance("ugroupStateHolder",ScopeType.SESSION);
				 Set<Map.Entry<String, String>> set = ugroupStateHolder.getSortOrders().entrySet();
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
				 auditList = entityManager.createQuery("select o from GroupUsersKnlT o "+(orderQuery!=null ? orderQuery : " "))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +
				         "from GroupUsersKnlT o ")
		                .getSingleResult();
				 
               log.info("invokeLocal:count:02:"+auditCount);
           	 } else if(type.equals("bean")){
				 
			 }
		}catch(Exception e){
			  log.error("invokeLocal:error:"+e);
			  evaluteForList=false;
			  FacesMessages.instance().add("������!");
		}
	}
	  /**
	  * ���������� �������� ����� ���� 
	  * ��� ����������� �������� ���������
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
			if(modelType.equals("ugroupDataModel")){
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
			GroupUsersKnlT ar = searchBean(sessionId);
		 Contexts.getEventContext().set("ugroupBean", ar);
	  }
   }
   
   private  GroupUsersKnlT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<GroupUsersKnlT> ugroupListCached = (List<GroupUsersKnlT>)
				  Component.getInstance("ugroupListCached",ScopeType.SESSION);
    	 
		if(ugroupListCached!=null){
			for(GroupUsersKnlT it : ugroupListCached){
				 
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
	  // FacesMessages.instance().add("������ ������� � ������� xxx.xxx.x.xxx!");
   }
   
   public void addUgroup(){
	  log.info("ugroupManager:addugroup:01");
	   
	  LinkGroupUsersUsersKnlT lguu=null;
	  List<LinkGroupUsersUsersKnlT> lguu_list = new ArrayList<LinkGroupUsersUsersKnlT>();
	  
	  GroupUsersKnlT ugroupBeanCrt = (GroupUsersKnlT)
				  Component.getInstance("ugroupBeanCrt",ScopeType.CONVERSATION);
	   
	   if(ugroupBeanCrt==null){
		   return;
	   }
	 
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		   
		  ugroupBeanCrt.setCreator(au.getIdUser());
		  ugroupBeanCrt.setCreated(new Date());
		  
		  entityManager.persist(ugroupBeanCrt);
		  
		  if(this.usrSelectList!=null){
			  
			  for(String user :this.usrSelectList){
				  lguu = new LinkGroupUsersUsersKnlT(new Long(user), ugroupBeanCrt.getIdSrv());
				  lguu.setCreator(au.getIdUser());
				  lguu.setCreated(new Date());
				  
				  lguu_list.add(lguu);
			  }
		  }
		  
		  ugroupBeanCrt.setLinkGroupUsersUsersKnlTs(lguu_list);
	      
		  entityManager.flush();
	      
	    //  entityManager.refresh(permBeanCrt);
	    
	    }catch (Exception e) {
	       log.error("ugroupManager:addugroup:ERROR:"+e);
	    }
	  
   }
   
   public void updUgroup(){
	  
	   log.info("ugroupManager:updugroup:01");
	   
	   LinkGroupUsersUsersKnlT lguu=null;
	   List<LinkGroupUsersUsersKnlT> lguu_list = new ArrayList<LinkGroupUsersUsersKnlT>();
	   
	   GroupUsersKnlT ugroupBean = (GroupUsersKnlT)
				  Component.getInstance("ugroupBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("ugroupManager:updugroup:sessionId:"+sessionId);
	
	   if(ugroupBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		  GroupUsersKnlT aam = entityManager.find(GroupUsersKnlT.class, new Long(sessionId));
		  
		  aam.setFull(ugroupBean.getFull());
		  aam.setDescription(ugroupBean.getDescription());
		  
		  aam.setModificator(au.getIdUser());
		  aam.setModified(new Date());
		 
		  
		  for(LinkGroupUsersUsersKnlT lg :aam.getLinkGroupUsersUsersKnlTs()){
			  entityManager.remove(lg);
		  }
		  
		  aam.setLinkGroupUsersUsersKnlTs(null);
		  
		  entityManager.flush();
		  
		  if(this.usrSelectEditList!=null){
			  
			  for(String user :this.usrSelectEditList){
				  lguu = new LinkGroupUsersUsersKnlT(new Long(user), new Long(sessionId));
				  lguu.setCreator(au.getIdUser());
				  lguu.setCreated(new Date());
				  
				  lguu_list.add(lguu);
			  }
		  }
		  
		  aam.setLinkGroupUsersUsersKnlTs(lguu_list);
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId));
	      Contexts.getEventContext().set("ugroupBean", aam);
	    	  
	     }catch (Exception e) {
           log.error("armManager:updSrm:ERROR:"+e);
         }
   }
   
 public void updUgroupRole(){
	   
	   log.info("usrManager:updUsrRole:01");
	   
	   List<LinkGroupUsersRolesKnlT> arList = new ArrayList<LinkGroupUsersRolesKnlT>();
	   List<LinkGroupUsersRolesKnlT> arRemovedList = new ArrayList<LinkGroupUsersRolesKnlT>();
	   
	   GroupUsersKnlT ugroupBean = (GroupUsersKnlT)
				  Component.getInstance("ugroupBean",ScopeType.CONVERSATION);
	   
	   String idArm = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("idArm");
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("usrManager:updUsr:idArm:"+idArm);
	   log.info("usrManager:updUsr:sessionId:"+sessionId);
	
	   if(ugroupBean==null || sessionId==null || idArm==null){
		   return;
	   }
	
	   try {
		   
		   GroupUsersKnlT aum = entityManager.find(GroupUsersKnlT.class, new Long(sessionId));
	
		  log.info("usrManager:updUsrRole:size1:"+aum.getLinkGroupUsersRolesKnlTs().size());
		  
		  for(LinkGroupUsersRolesKnlT apl : aum.getLinkGroupUsersRolesKnlTs()){
			  
			  log.info("usrManager:updUsrRole:AcApplication:"+apl.getAcRolesBssT().getAcApplication());
			  
			  if(apl.getAcRolesBssT().getAcApplication().equals(new Long(idArm))){
			       entityManager.remove(apl);
			       arRemovedList.add(apl);
			  }
		  }

		  for(LinkGroupUsersRolesKnlT rem : arRemovedList){
			  aum.getLinkGroupUsersRolesKnlTs().remove(rem);
		  }
		   
		  entityManager.flush();
		  
		  log.info("usrManager:updUsrRole:size2:"+aum.getLinkGroupUsersRolesKnlTs().size());
			  
		   for(BaseItem rol:this.roleList){
	    			  log.info("UsrManager:editUsr:updUsrRole:"+((AcRole)rol).getRoleTitle());
	    			  log.info("UsrManager:editUsr:updUsrRole:"+((AcRole)rol).getUsrChecked());
	    			  
	    			  if(((AcRole)rol).getUsrChecked().booleanValue()){
	    				  LinkGroupUsersRolesKnlT au = new LinkGroupUsersRolesKnlT(((AcRole)rol).getIdRol(), new Long(sessionId));
	    			            au.setCreated(new Date());
	    			            au.setCreator(new Long(1));
	    			            arList.add(au);
	    			  }
	    		  }
	     	  
	     	if(arList.size()>0){
	    	 	//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    		  aum.getLinkGroupUsersRolesKnlTs().addAll(arList) ;
	    	}
	    	log.info("usrManager:updUsrRole:size3:"+aum.getLinkGroupUsersRolesKnlTs().size());
	    	  
	        entityManager.flush();
	    	 
	        entityManager.refresh(aum);
	    	  
	    	Contexts.getEventContext().set("ugroupBean", aum);
	    	  
	     }catch (Exception e) {
           log.error("UsrManager:editUsrRole:ERROR:"+e);
         }
   }
   
   public void delUgroup(){
	 try{
		log.info("ugroupManager:delugroup:01");  
		
		GroupUsersKnlT armBean = (GroupUsersKnlT)
				  Component.getInstance("ugroupBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{armBean.idArm}"/>
		
		if(armBean==null){
			return;
		}
		 
		log.info("ugroupManager:delugroup:Idugroup:"+armBean.getIdSrv());
		
		GroupUsersKnlT aom = entityManager.find(GroupUsersKnlT.class, armBean.getIdSrv());
		  
		entityManager.remove(aom);
		
	 }catch(Exception e){
		 log.error("armManager:delArm:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 GroupUsersKnlT ao = entityManager.find(GroupUsersKnlT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("ugroupBean", ao);
	   	 }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }
    } 
   
    public void forViewDelMessage() {
		 String sessionId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap()
				.get("sessionId");
		  log.info("forViewDel:sessionId:"+sessionId);
		  if(sessionId!=null){
			  GroupUsersKnlT aa = entityManager.find(GroupUsersKnlT.class, new Long(sessionId));
			/* if((aa.!=null&&!aa.getAcLinkRoleAppPagePrmssns().isEmpty())){
				dellMessage="� ���������� ���� ���������� ������! ��� �������� ��� ����� �������!";
			 }*/
			 Contexts.getEventContext().set("ugroupBean", aa);
		 }	
    }
  
    public void forViewWord(String fileName){
		log.info("JournManager:forViewWord:01");
		try{
		 
			if(fileName==null || fileName.equals("")){
				fileName="invoke_services";
			}
			
		  HttpServletResponse response = (HttpServletResponse)
				  FacesContext.getCurrentInstance().getExternalContext().getResponse();
		  response.setHeader("Content-disposition", "attachment; filename="+fileName+".doc");
	      response.setContentType("application/msword");

		}catch(Exception e){
			log.error("auditReportsManager:forViewWord:error:"+e);
		}
		log.info("auditReportsManager:forViewWord:02");
	}
    
    public List<AcUser> getUsrList(){
    	if(this.usrList==null){
    		/*String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
   	             .getRequestParameterMap()
   	             .get("remoteAudit");
   	        log.info("ugroupManager:getUsrList:remoteAudit:"+remoteAudit);
   	        if(!remoteAudit.equals("CrtFact")){*/
    		  this.usrList=entityManager.createQuery("select o from AcUser o order by o.surname ")
    				.getResultList();
    	//	}
    	}
    	return this.usrList;
    }
    
    public List<String> getUsrSelectList(){
       	return this.usrSelectList;
    }
    public void setUsrSelectList(List<String> usrSelectList){
    	this.usrSelectList=usrSelectList;
    }
    
    public List<String> getUsrSelectEditList(){
    	
      try{
    		
    	if(this.usrSelectEditList==null){
    		String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
    		String sessionId = FacesContext.getCurrentInstance().getExternalContext()
   	             .getRequestParameterMap()
   	             .get("sessionId");
    		
    		if(remoteAudit==null||sessionId==null){
    			return null;
    		}
    		
	        log.info("ugroupManager:getUsrSelectEditList:remoteAudit:"+remoteAudit);
	        log.info("ugroupManager:getUsrSelectEditList:sessionId:"+sessionId);
	        
	        if(!remoteAudit.equals("UpdFact")){
		      this.usrSelectEditList=entityManager.createQuery(
		    		"select o.pk.acUser from LinkGroupUsersUsersKnlT o " +
		      		"where o.pk.groupUser = :sessionId ")
		      		.setParameter("sessionId", new Long(sessionId))
				 .getResultList();
	    	}
    	 }
    	}catch(Exception e){
			log.error("ugroupManager:getUsrSelectEditList:error:"+e);
		}
       	return this.usrSelectEditList;
    }
    public void setUsrSelectEditList(List<String> usrSelectEditList){
    	this.usrSelectEditList=usrSelectEditList;
    }
    
    
    public List<AcUser> getUsrSelectListForView(){
    	
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
      		
  	        log.info("ugroupManager:getUsrSelectListForView:remoteAudit:"+remoteAudit);
  	        log.info("ugroupManager:getUsrSelectListForViewt:sessionId:"+sessionId);
  	        
  	        //if(!remoteAudit.equals("UpdFact")){
  		      this.usrSelectListForView=entityManager.createQuery(
  		    		"select o.acUsersKnlT from LinkGroupUsersUsersKnlT o " +
  		      		"where o.pk.groupUser = :sessionId ")
  		      		.setParameter("sessionId", new Long(sessionId))
  				 .getResultList();
  	    	//}
      	 }
      	}catch(Exception e){
  			log.error("ugroupManager:getUsrSelectListForView:error:"+e);
  		}
         	return this.usrSelectListForView;
      }
    
    public List<BaseItem> getRoleList(){
 	   if(this.roleList==null){
 		   String idArm = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("idArm");
 		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
 		   String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("remoteAudit");
 		   log.error("UsrManager:getRoleList:idArm:"+idArm);
 		   log.error("UsrManager:getRoleList:sessionId:"+sessionId);
 		   log.error("UsrManager:getRoleList:remoteAudit:"+remoteAudit);
 		   
 		   if(idArm==null||sessionId==null){
 			   return this.roleList;
 		   }
 		   
 		   this.roleList = entityManager.createQuery("select o from AcRole o where o.acApplication= :idArm order by o.roleTitle ")
 				   .setParameter("idArm", new Long(idArm))
                    .getResultList();
 		 
 		   if(remoteAudit!=null&&remoteAudit.equals("armSelectFact")){
 		   
 		//	 List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o JOIN o.acLinkUserToRoleToRaions o1 where o1.pk.acUser = :acUser ")
 		     List<AcRole> listUsrRol=entityManager.createQuery(
 		    		 "select o from AcRole o,  LinkGroupUsersRolesKnlT o1 " +
 		    		 "where o1.pk.acRole = o.idRol " +
 		    		 "and o1.pk.groupUser = :groupUser ")
 					 .setParameter("groupUser", new Long(sessionId))
 		      		 .getResultList();
 		   
 		     for(BaseItem role :this.roleList){
 	           if (listUsrRol.contains((AcRole)role)){  
 	        	  ((AcRole)role).setUsrChecked(true);
 			   }
 	         } 
 		  }
 	   }
 	   return this.roleList;
    }
    
    public void setRoleList(List<BaseItem> roleList){
 	   this.roleList=roleList;
    }
    
    public List<AcApplication> getListGroupArmForView() throws Exception{
	    log.info("GroupManager:getListUGroupArmForView:01");
	   
	    String sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    
	    log.info("GroupManager:getListGroupArmForView:sessionId:"+sessionId);
	    
	    try {
	    	
	    	if(listGroupArmForView==null && sessionId!=null){
	      	
	    	//	listUsrArmForView=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
	    		listGroupArmForView=entityManager.createQuery(
	    				"select oo from AcApplication oo where oo.idArm IN " +
	    				"(select o.idArm from AcApplication o, AcRole o1, LinkGroupUsersRolesKnlT o2 " +
	    				"where o1.acApplication2 = o and o2.acRolesBssT = o1 and o2.pk.groupUser = :groupUser " +
	    				"group by o.idArm) ")
	    				 .setParameter("groupUser", new Long(sessionId))
	    				.getResultList();

	    		
	    		/*
	    		listUsrArmForView=entityManager.createQuery(
	    				"select o from AcApplication o, AcRole o1, AcLinkUserToRoleToRaion o2 " +
	    				"where o1.acApplication2 = o and o2.acRole = o1 and o2.pk.acUser = :acUser " +
	    				"group by o.idArm, o.name, o.description ")
	    				 .setParameter("acUser", new Long(sessionId))
	    				.getResultList();
		      	*/
	    		log.info("GroupManager:getListGroupArmForView:listUsrArmForView.size:"+listGroupArmForView.size());
	    		
	    		//List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o JOIN o.acLinkUserToRoleToRaions o1 where o1.pk.acUser = :acUser")
	    		List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o, LinkGroupUsersRolesKnlT o1 " +
	    				"where o1.acRolesBssT = o and o1.pk.groupUser = :groupUser")
	    				.setParameter("groupUser", new Long(sessionId))
			      		.getResultList();
			    
	    		//log.info("UsrManager:getListUsrArmForView:listUsrRol.size:"+listUsrRol.size());
	    		
		        for(AcApplication arm :listGroupArmForView){
		          List<AcRole> listUsrRolLocal=new ArrayList<AcRole>();	
			      for(AcRole rol :arm.getAcRoles()){
			    	  if (listUsrRol.contains(rol)){  
			    		  listUsrRolLocal.add(rol);
			    	  }
			    	}
			      arm.setRolList(listUsrRolLocal);
          	      } 
	    	   }
			} catch (Exception e) {
	    	 log.error("GroupManager:getListGroupArmForView:ERROR:"+e);
	         throw e;
	     }
	    return listGroupArmForView;
   }
    
    
    public int getConnectError(){
	   return connectError;
    }
    
  /* 
   public List<AcApplication> getListArm() throws Exception{
	    log.info("armManager::getListArm:01");
	    try {
	    	if(listArm==null){
	       		listArm=entityManager.createQuery("select o from AcApplication o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("armManager::getListArm:ERROR:"+e);
	         throw e;
	     }
	    return listArm;
  }*/
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    UgroupContext ac= new UgroupContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("full"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("description"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   UgroupContext ac= new UgroupContext();
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
	    ArrayList<String> selRecArm = (ArrayList<String>)
				  Component.getInstance("selRecUgroup",ScopeType.SESSION);
	    
	    if(selRecArm==null){
	       selRecArm = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    // AcApplication aa = searchBean(sessionId);
	    GroupUsersKnlT aa = new GroupUsersKnlT();
  	    // � getAuditList : else{it.setSelected(false);}
	    
	    if(aa!=null){
	     if(selRecArm.contains(sessionId)){
	    	selRecArm.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecArm.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecUgroup", selRecArm);	
	    
	    Contexts.getEventContext().set("ugroupBean", aa);
	   }
	}
   
  
   
   public String getDellMessage() {
	   return dellMessage;
   }
   public void setDellMessage(String dellMessage) {
	   this.dellMessage = dellMessage;
   } 
   
    public Boolean getEvaluteForList() {
	
   	log.info("armManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("augroupanager:evaluteForList:remoteAudit:"+remoteAudit);
     	
    	if(remoteAudit!=null&&
    	 
    	   !remoteAudit.equals("OpenCrtFact")&&	
    	   !remoteAudit.equals("OpenUpdFact")&&
    	   !remoteAudit.equals("OpenDelFact")&&
   	       !remoteAudit.equals("onSelColFact")&&
   	       !remoteAudit.equals("refreshPdFact")){
    		log.info("ugroupManager:evaluteForList!!!");
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
		   log.info("ugroupManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("ugroupManager:evaluteForListFooter!!!");
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
			    log.info("ugroupManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("ugroupManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("ugroupManager:evaluteForBean!!!");
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
