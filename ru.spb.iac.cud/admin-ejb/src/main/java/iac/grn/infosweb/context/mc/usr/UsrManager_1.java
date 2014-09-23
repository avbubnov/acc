package iac.grn.infosweb.context.mc.usr;

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
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.IspBssT;
import iac.grn.infosweb.session.Authenticator;
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
 * ����������� ���
 * @author bubnov
 *
 */
//@Name("usrManager")
public class UsrManager_1 {//implements OrgManagerInterface{
	
	 @Logger private Log log;
	
	 @In 
	 EntityManager entityManager;
	 
	/**
     * �������������� �������� 
     * ��� �����������
     */
	//private BaseItem usrBean;             !!! ��������� !!!
	
		
	private List<BaseItem> auditList;//= new ArrayList<VAuditReport>();
	
	private Long auditCount;
	
	private List <BaseTableItem> auditItemsListSelect;
	
	private List <BaseTableItem> auditItemsListContext;
	
	private int connectError=0;
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	private Boolean evaluteForBean;
	
	//private String pidUsr=null;
	  
	private boolean addLoginExist=false;
	
	private List<AcApplication> listUsrArm = null;
	private List<AcApplication> listUsrArmEdit = null;
	private List<AcApplication> listUsrArmForView = null;
	
	private LinksMap linksMap = null;
	private AcUser currentUser = null;
	
	
	
   public boolean getAddLoginExist() {
	    return addLoginExist;
	}
	
	//@In
    //private AuditGlobal auditGlobal;
	
	/*
	private static final String[] CSV_FIELDS = {
         "extendedTimestamp", "sessionId", "objectName", "osUser", "osHost"
    };
	public String[] getCsvFields() {
        return CSV_FIELDS;
    }*/
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	   
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> usrListCached = (List<BaseItem>)
			  Component.getInstance("usrListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    usrListCached!=null){
		 		log.info("getAuditList:02:"+usrListCached.size());
			    	this.auditList=usrListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("usrListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecUsr = (ArrayList<String>)
					  Component.getInstance("selRecUsr",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecUsr!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecUsr.contains(it.getBaseId().toString())){
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
				 
				 UsrStateHolder usrStateHolder = (UsrStateHolder)
						  Component.getInstance("usrStateHolder",ScopeType.SESSION);
				 Set<Map.Entry<String, String>> set = usrStateHolder.getSortOrders().entrySet();
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
                 
				// auditList = new ArrayList<BaseItem>();
				 auditList = entityManager.createQuery("select o from AcUser o "+(orderQuery!=null ? orderQuery : ""))
                       .setFirstResult(firstRow)
                       .setMaxResults(numberOfRows)
                       .getResultList();
             log.info("invokeLocal:list:02");
  
			 } else if(type.equals("count")){
				 log.info("IHReposList:count:01");
				 auditCount = (Long)entityManager.createQuery(
						 "select count(au) " +
				         "from AcUser au ")
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
			if(modelType.equals("usrDataModel")){
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
		 AcUser au = searchBean(sessionId);
		 
		 Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
			
    	 
	    	List<AcRole> rlist = entityManager.createQuery(
	    			"select ar from AcRole ar, AcLinkUserToRoleToRaion alur " +
	    	 		"where alur.acRole = ar and alur.pk.acUser = :acUser " +
	    	 		"and ar.acApplication= :acApplication ")
	    	 		 .setParameter("acUser", au.getIdUser())
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
	    		
	    	}
		 
		 Contexts.getEventContext().set("usrBean", au);
	  }
   }
   
   private AcUser searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<AcUser> usrListCached = (List<AcUser>)
				  Component.getInstance("usrListCached",ScopeType.SESSION);
		if(usrListCached!=null){
			for(AcUser it : usrListCached){
				 
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
   
   public void addUsr(){
	   log.info("usrManager:addUsr:01");
	   
	   List<AcLinkUserToRoleToRaion> arList = new ArrayList<AcLinkUserToRoleToRaion>();
	   AcUser usrBeanCrt = (AcUser)
				  Component.getInstance("usrBeanCrt",ScopeType.CONVERSATION);
	   
	   IspBssT clUsrBean = (IspBssT)
				  Component.getInstance("clUsrBean",ScopeType.CONVERSATION);
	 
	   IspBssT clOrgBean = (IspBssT)
				  Component.getInstance("clOrgBean",ScopeType.CONVERSATION);
	   
	   if(usrBeanCrt==null){
		   return;
	   }
	 
	   try {
	    	
		   log.info("hostsManager:addUsr:clUsrBean:SignObject:"+clUsrBean.getSignObject());
		   log.info("hostsManager:addUsr:clOrgBean:SignObject:"+clOrgBean.getSignObject());
		   
		   
		  
			 
		   
	       if(!loginExist(usrBeanCrt.getLogin())) {
	    	   
	    	 //  usrBeanCrt.setAcOrganization(1L);
	    	   
	    	   if(clUsrBean.getSignObject()!=null){
	    	   
	    		  
	    		   usrBeanCrt.setName1(" ");
	    		   usrBeanCrt.setName2(" ");
	    		   usrBeanCrt.setSurname(" ");
	    		   
	    	  IspBssT ibt_usr = (IspBssT)entityManager.createQuery(
	    				"select o from IspBssT o where o.status='A' " +
	    				"and o.signObject = :signObject ")
	    		    	.setParameter("signObject", clUsrBean.getSignObject())
	    		    	.getSingleResult();
	    	  
	    	  //!!! ��� ���������� �� �������
	    	  String fio = ibt_usr.getFio();
	    	  String family = null;
	    	  
	    	  if(fio!=null){
	    		  family =fio.split(" ")[0];
	    	  }
	    	  if(family!=null&&!family.equals("")){
	    	     usrBeanCrt.setSurname(family);
	    	  }else{
	    		 usrBeanCrt.setSurname(" ");
	    	  }
	    	  
	    	  log.info("hostsManager:addUsr:ibt_usr:IdSrv:"+ibt_usr.getIdSrv());
	    	  
	    	  //20_03_13 usrBeanCrt.setAcClUser(ibt_usr.getIdSrv());
	    	  
	    	  IspBssT ibt_org = (IspBssT)entityManager.createQuery(
	    				"select o from IspBssT o where o.status='A' " +
	    				"and o.signObject = :signObject ")
	    		    	.setParameter("signObject", clUsrBean.getSignObject().substring(0,3)+"00000")
	    		    	.getSingleResult();
	    	 
	    	  log.info("hostsManager:addUsr:ibt_org:IdSrv:"+ibt_org.getIdSrv());
	    	  
	    	  //20_03_13   usrBeanCrt.setAcClOrganization(ibt_org.getIdSrv()); 
	    	 
	       }else{
	    	   IspBssT ibt_org = (IspBssT)entityManager.createQuery(
	    				"select o from IspBssT o where o.status='A' " +
	    				"and o.signObject = :signObject ")
	    		    	.setParameter("signObject", clOrgBean.getSignObject())
	    		    	.getSingleResult();
	    	 
	    	  log.info("hostsManager:addUsr:ibt_org:IdSrv:"+ibt_org.getIdSrv());
	    	  
	    	  //20_03_13  usrBeanCrt.setAcClOrganization(ibt_org.getIdSrv()); 
	       }
	    	  
	    	  AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	    	   
	    	  usrBeanCrt.setCreator(cau.getIdUser());
	    	  usrBeanCrt.setCreated(new Date());
	    	  entityManager.persist(usrBeanCrt);
	    	      
	    	     for(AcApplication arm:listUsrArm){
		    		  log.info("UsrManager:addUsr:Arm:"+arm.getName());
		    		  for(AcRole rol:arm.getAcRoles()){
		    			  log.info("UsrManager:addUsr:RolTitle:"+rol.getRoleTitle());
		    			  log.info("UsrManager:addUsr:RolChecked:"+rol.getUsrChecked());
		    			  
		    			  if(rol.getUsrChecked().booleanValue()){
		    				  
		    				/* if(rol.getRaions()!=null&&!rol.getRaions().isEmpty()){
		    					 for(Object idRai: rol.getRaions()){
		    					   AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(new Long(idRai.toString()), rol.getIdRol(), usrBeanCrt.getIdUser());
			    			       au.setCreated(new Date());
			    			       au.setCreator(new Long(1));
			    			       //entityManager.persist(au);
			    			       arList.add(au);
			    			      }
		    				 }else{*/
		    			       AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(rol.getIdRol(), usrBeanCrt.getIdUser());
		    			       au.setCreated(new Date());
		    			       au.setCreator(new Long(1));
		    			      // entityManager.persist(au);
		    			       arList.add(au);
		    			   //   }
		    			  }
		    		  }
		    	  }
	    	     
	    	     if(arList.size()>0){
	    	 		//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    	 	     usrBeanCrt.setAcLinkUserToRoleToRaions(arList);
	    	 	 }
	    	 	   
	    	 	 entityManager.flush();
	    	  	 entityManager.refresh(usrBeanCrt);
	    	     
	    	   }   
	          }catch (Exception e) {
	             log.error("UsrManager:addUsr:ERROR="+e);
	             e.printStackTrace(System.out);
	          }
	   
   }
   
   public void updUsr(){
	   
	   log.info("hostsManager:updHosts:01");
	   
	   List<AcLinkUserToRoleToRaion> arList = new ArrayList<AcLinkUserToRoleToRaion>();
	   AcUser usrBean = (AcUser)
				  Component.getInstance("usrBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("hostsManager:updUsr:sessionId:"+sessionId);
	
	   if(usrBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   
		  AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		  
		 //	entityManager.merge(acUsrBean);
		  AcUser aum = entityManager.find(AcUser.class, new Long(sessionId));
		  
		  
		  //20_03_13   if(aum.getAcClUser()==null){
		   aum.setName1(usrBean.getName1());
		   aum.setName2(usrBean.getName2());
		   aum.setSurname(usrBean.getSurname());
		   aum.setEmail(usrBean.getEmail());
		   aum.setPhone(usrBean.getPhone());
		   //20_03_13   }
		   
		//  aum.setAcClOrganization(usrBean.getAcClOrganization());
		  
		  aum.setPassword(usrBean.getPassword());
		  aum.setStart(usrBean.getStart());
		  aum.setFinish(usrBean.getFinish());
		  
		  aum.setModificator(cau.getIdUser());
		  aum.setModified(new Date());
		  
		  for(AcLinkUserToRoleToRaion apl : aum.getAcLinkUserToRoleToRaions()){
			   entityManager.remove(apl);
		  }
		  aum.setAcLinkUserToRoleToRaions(null);
		   
		  entityManager.flush();
		  
	    /*	  entityManager.createQuery("UPDATE AcUser r SET " +
 	      		         "r.name1= :name1, " +
 	      		         "r.name2= :name2, " +
 	      		         "r.surname= :surname, " +
 	      		         "r.email= :email, " +
 	      		         "r.phone= :phone, " +
 	      		        // "r.login= :login, " +
 	      		         "r.password= :password, " +
 	      		         "r.acOrganization= :acOrganization, " +
 	      		         "r.start= :start, " +
 	      		         "r.finish= :finish, " +
 	      		         "r.modificator= :modificator, " +
 	      		         "r.modified= :modified " +
 	      		         "WHERE r.idUser= :idUser")
 	      		         .setParameter("name1", usrBean.getName1())
 	      		         .setParameter("name2",  usrBean.getName2())
 	      		         .setParameter("surname", usrBean.getSurname())
 	      		         .setParameter("email", usrBean.getEmail())
 	      		         .setParameter("phone",  usrBean.getPhone())
 	      		        // .setParameter("login", acUsrBean.getLogin())
 	      		         .setParameter("password", usrBean.getPassword())
 	      		         .setParameter("acOrganization", usrBean.getAcOrganization())
 	      		         .setParameter("start",  usrBean.getStart())
 	      		         .setParameter("finish", usrBean.getFinish())
 	      		         .setParameter("modificator", new Long(1))
 	      		         .setParameter("modified", new Date())
 	      		      //  .setParameter("idUser", usrBean.getIdUser())
 	      		        .setParameter("idUser", new Long(sessionId))
 	      		        .executeUpdate();
	      
	    	  entityManager.createQuery("DELETE FROM AcLinkUserToRoleToRaion r " +
	      		         "WHERE r.pk.acUser= :acUser ")
	      		       //  .setParameter("acUser", usrBean.getIdUser())
	      		         .setParameter("acUser", new Long(sessionId))
	      		         .executeUpdate();
	    	  
	    	  //!!!!!!!!!!!!!!!!!!!
	    	  entityManager.clear();
	    	  //!!!!!!!!!!!!!!!!!!!!
	    	  */
	    	  //pidUsr - global ����������!!!
	    	  
	    	  for(AcApplication arm:listUsrArmEdit){
	    		  log.info("UsrManager:editUsr:Arm:"+arm.getName());
	    		  for(AcRole rol:arm.getAcRoles()){
	    			  log.info("UsrManager:editUsr:RolTitle:"+rol.getRoleTitle());
	    			  log.info("UsrManager:editUsr:RolChecked:"+rol.getUsrChecked());
	    			  
	    			  if(rol.getUsrChecked().booleanValue()){
	    				  
	    				/*  if(rol.getRaions()!=null&&!rol.getRaions().isEmpty()){
		    					 for(Object idRai: rol.getRaions()){
		    					   AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(new Long(idRai.toString()), rol.getIdRol(), new Long(sessionId));
			    			       au.setCreated(new Date());
			    			       au.setCreator(new Long(1));
			    			     //  entityManager.persist(au);
			    			       arList.add(au);
			    			      }
		    				 }else{	*/  
	    			            AcLinkUserToRoleToRaion au = new AcLinkUserToRoleToRaion(rol.getIdRol(), new Long(sessionId)/*usrBean.getIdUser()*/);
	    			            au.setCreated(new Date());
	    			            au.setCreator(new Long(1));
	    			          //  entityManager.persist(au);
	    			            arList.add(au);
	    			  // }
	    			  }
	    		  }
	    	  }
	    	  
	    	  if(arList.size()>0){
	    	 	//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    		  aum.setAcLinkUserToRoleToRaions(arList);
	    	  }
	    	 	   
	    	 entityManager.flush();
	    	 entityManager.refresh(aum);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	    	 Contexts.getEventContext().set("usrBean", aum);
	    	  
	     }catch (Exception e) {
           log.error("UsrManager:editUsr:ERROR:"+e);
         }
   }
   
   public void delUsr(){
	 try{
		log.info("usrManager:delHosts:01");  
		
		AcUser usrBean = (AcUser)
				  Component.getInstance("usrBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{usrBean.idUser}"/>
		
		if(usrBean==null){
			return;
		}
		 
		log.info("usrManager:delHosts:IdUsr:"+usrBean.getIdUser());
		
		AcUser aum = entityManager.find(AcUser.class, usrBean.getIdUser());
		  
		entityManager.remove(aum);
		
	 }catch(Exception e){
		 log.error("hostsManager:delHosts:error:"+e); 
	 }
   }
  /* 
   public void updHosts(){
	   log.info("hostsManager:updHosts:01");
	   
	   List<AcProtList> apList = new ArrayList<AcProtList>();
	   AcHost hostsBean = (AcHost)
				  Component.getInstance("hostsBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("hostsManager:updHosts:sessionId:"+sessionId);
	
	   if(hostsBean==null || sessionId==null){
		   return;
	   }
	  
      entityManager.createQuery("UPDATE AcHost r SET " +
		         "r.dnsName= :dnsName, " +
		         "r.ipAddress= :ipAddress, " +
		         "r.modificator= :modificator, " +
		         "r.modified= :modified " +
		         "WHERE r.idHost= :idHost")
		         .setParameter("dnsName", hostsBean.getDnsName())
		         .setParameter("ipAddress",  hostsBean.getIpAddress())
		         .setParameter("modificator", new Long(1))
		         .setParameter("modified", new Date())
		         .setParameter("idHost", new Long(sessionId))
		         .executeUpdate();
	   
	   entityManager.createQuery("DELETE FROM AcProtList r " +
		         "WHERE r.acHostId= :acHostId ")
		         .setParameter("acHostId", new Long(sessionId))
		         .executeUpdate();
	   
	   for(AcClProtocl acp: protocols){
		   log.info("hostsManager:updHosts:Name:"+acp.getName());
		   log.info("hostsManager:updHosts:PortHost:"+acp.getPortHost());
		   log.info("hostsManager:updHosts:UsrChecked:"+acp.getUsrChecked());
		   
		   if(acp.getUsrChecked() && acp.getPortHost()!=null){
			     AcProtList ap = new  AcProtList(hostsBean.getIdHost(), acp.getId());
			     ap.setPort(new Long(acp.getPortHost())); 
			     ap.setCreator(new Long(1));
			     ap.setCreated(new Date());
			     
			     apList.add(ap);
		   }
	   }
	   if(apList.size()>0){
			//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
		  hostsBean.setAcProtLists(apList);
	   }
		   
	   entityManager.flush();
	   entityManager.refresh(hostsBean);
	   
	   log.info("hostsManager:updHosts:02");
   }*/
   
   public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 AcUser ah = entityManager.find(AcUser.class, new Long(sessionId));
	     	 Contexts.getEventContext().set("usrBean", ah);
	     }
	   }catch(Exception e){
		   log.error("forViewUpdDel:Error:"+e);
	   }
   } 
   
   public void forViewCrt() {
	   try{
	     log.info("forViewCrt");
	     AcUser au = new AcUser();
	     au.setPassword(password());
	     Contexts.getEventContext().set("usrBeanCrt", au);
	   }catch(Exception e){
		 log.error("forViewCrt:Error:"+e);
	   }
   } 
   
   private String password(){
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Random rnd = new Random();

		int len=8; 
		
	    StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < len; i++) {
		   sb.append(AB.charAt(rnd.nextInt(AB.length())));
		}
		log.info("password:"+sb.toString());
		return sb.toString();
  
   }
   
   public List<AcApplication> getListUsrArm() throws Exception{
	    log.info("UsrManager:getListUsrArm:01");
	    try {
	    	if(listUsrArm==null){
	    		//listUsrArm=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
	      		String query="select o from AcApplication o where o.acRoles IS NOT EMPTY ";
	    		
	      		AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
	      		
	      		LinksMap lm = (LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION);
	      		Long appCode = lm.getAppCode();
			
	      		
	      		if(!cau.getIsSysAdmin().equals(1L)){ //���� �� � ����� ���� �����
	    			query+="and o.idArm!="+appCode;
	    		}/*else if(cau.getIdUser()!=lm.getSuperUserCode()){ //���� ������� ������������ � ����� ���� �����, �� �� ������ ������������������
	    			query+="and o.idArm!="+appCode;
	    		}*/
	    		listUsrArm=entityManager
	    				.createQuery(query)
	    				.getResultList();
				
	    	}
	     } catch (Exception e) {
	    	 log.error("UsrManager:getListUsrArm:ERROR:"+e);
	         throw e;
	     }
	    return listUsrArm;
   }
   public void setListUsrArm(List<AcApplication> listUsrArm){
	   this.listUsrArm=listUsrArm;
   }
   
   public List<AcApplication> getListUsrArmEdit() throws Exception{
	    log.info("UsrManager:getListUsrArmEdit:01");
	   
	    String  idUsr = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("UsrManager:getListUsrArmEdit:sessionId:"+idUsr);
	
	    try {
	
	    	String saveEditFlag;
	    	if(listUsrArmEdit==null){
	      		//listUsrArmEdit=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
	      		
                String query="select o from AcApplication o where o.acRoles IS NOT EMPTY ";
	    		
                //��������� �� disabled chekbox
	      	/*	Long appCode = ((LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION)).getAppCode();
				Authenticator authenticator = (Authenticator)
	  				  Component.getInstance("authenticator",ScopeType.CONVERSATION);
	    		if(!authenticator.accessPerm("0052","1")){
	    			query+="and o.idArm!="+appCode;
	    		}
	    		*/
	    		
	    		listUsrArmEdit=entityManager
	    				.createQuery(query)
	    				.getResultList();
	      		
	      		
	      		saveEditFlag= FacesContext.getCurrentInstance().getExternalContext()
		 			        .getRequestParameterMap()
		 			        .get("saveEditFlag");
		    	 log.info("UsrManager:getListUsrArmEdit:saveEditFlag:"+saveEditFlag);
		    		 
		    	/* for(AcApplication arm :listUsrArmEdit){
		    		 log.info("UsrManager:getListUsrArmEdit:arm:"+arm.getName());
		    		  for(AcRole rol :arm.getAcRoles()){
				    	  log.info("UsrManager:getListUsrArmEdit:rol:"+rol.getRoleTitle());
		    		  }	 
		    	 }	*/ 
		    	if(saveEditFlag==null){	
		    		
		    	// List<Long> listUsrRol=em.createQuery("select o.idRol from AcRole o JOIN o.acLinkUserToRoleToRaions o1 where o1.pk.acUser = :acUser")
		    	  List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o JOIN o.acLinkUserToRoleToRaions o1 where o1.pk.acUser = :acUser")
						 .setParameter("acUser", new Long(idUsr))
			      				.getResultList();
			     
		    	
		    	  
	      	    for(AcApplication arm :listUsrArmEdit){
			        	
			      for(AcRole rol :arm.getAcRoles()){
			    	  List<Long> ls =new ArrayList<Long>();
			    	  log.info("UsrManager:getListUsrArmEdit:rol.getIdRol()"+rol.getIdRol());
			    	
			    	  //if (listUsrRol.contains(rol.getIdRol())){
			    	  if (listUsrRol.contains(rol)){  
			    		  rol.setUsrChecked(true);
			    	  }
			    	  
			    	/*  for(AcLinkUserToRoleToRaion alu :rol.getAcLinkUserToRoleToRaions()){
			    	   if(alu.getAcUser().getIdUser().equals(new Long(idUsr))&&alu.getAcRole().getIdRol().equals(rol.getIdRol())){
			    		   ls.add(alu.getAcRaion().getIdRai());
			    	   }
			    	  }
			    	  rol.setRaions(ls);*/
			   	     }
          	        } 
	      	    }
	    	 }
		//	 }
			} catch (Exception e) {
	    	 log.error("UsrManager:getListUsrArmEdit:ERROR:"+e);
	         throw e;
	     }
	    return listUsrArmEdit;
   }
   
   public void setListUsrArmEdit(List<AcApplication> listUsrArmEdit){
	   this.listUsrArmEdit=listUsrArmEdit;
   }
   
   public List<AcApplication> getListUsrArmForView() throws Exception{
	    log.info("UsrManager:getListUsrArmForView:01");
	   
	    String sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    
	    log.info("UsrManager:getListUsrArmForView:sessionId:"+sessionId);
	    
	    try {
	    	
	    	if(listUsrArmForView==null && sessionId!=null){
	      	
	    	//	listUsrArmForView=entityManager.createQuery("select o from AcApplication o where o.acRoles IS NOT EMPTY").getResultList();
	    		listUsrArmForView=entityManager.createQuery(
	    				"select oo from AcApplication oo where oo.idArm IN " +
	    				"(select o.idArm from AcApplication o, AcRole o1, AcLinkUserToRoleToRaion o2 " +
	    				"where o1.acApplication2 = o and o2.acRole = o1 and o2.pk.acUser = :acUser " +
	    				"group by o.idArm) ")
	    				 .setParameter("acUser", new Long(sessionId))
	    				.getResultList();

	    		
	    		/*
	    		listUsrArmForView=entityManager.createQuery(
	    				"select o from AcApplication o, AcRole o1, AcLinkUserToRoleToRaion o2 " +
	    				"where o1.acApplication2 = o and o2.acRole = o1 and o2.pk.acUser = :acUser " +
	    				"group by o.idArm, o.name, o.description ")
	    				 .setParameter("acUser", new Long(sessionId))
	    				.getResultList();
		      	*/
	    		log.info("UsrManager:getListUsrArmForView:listUsrArmForView.size:"+listUsrArmForView.size());
	    		
	    		//List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o JOIN o.acLinkUserToRoleToRaions o1 where o1.pk.acUser = :acUser")
	    		List<AcRole> listUsrRol=entityManager.createQuery("select o from AcRole o, AcLinkUserToRoleToRaion o1 " +
	    				"where o1.acRole = o and o1.pk.acUser = :acUser")
	    				.setParameter("acUser", new Long(sessionId))
			      		.getResultList();
			    
	    		//log.info("UsrManager:getListUsrArmForView:listUsrRol.size:"+listUsrRol.size());
	    		
		        for(AcApplication arm :listUsrArmForView){
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
	    	 log.error("UsrManager:getListUsrArmForView:ERROR:"+e);
	         throw e;
	     }
	    return listUsrArmForView;
   }
   
   
   public int getConnectError(){
	   return connectError;
   }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		  // log.info("getAuditItemsListSelect:01");
	
	    UsrContext ac= new UsrContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("fio"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("orgName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("phone"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("email"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("usrManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   UsrContext ac= new UsrContext();
		   auditItemsListContext = new ArrayList<BaseTableItem>();
		   //auditItemsListContext.addAll(ac.getAuditItemsMap().values());
		   //auditItemsListContext.addAll(ac.getAuditItemsCollection());
		   auditItemsListContext=ac.getAuditItemsCollection();
	   }
	   return this.auditItemsListContext;
   }
      
   private boolean loginExist(String login) throws Exception {
		log.info("UsrManager:loginExist:login="+login);
		if(login!=null){
		  try{
			  AcUser au= (AcUser) entityManager.createQuery("select au from AcUser au " +
			 		                               "where au.login = :login")
			 		     .setParameter("login", login)
			 		     .getSingleResult();
			  addLoginExist=true;
			  log.info("UsrManager:loginExist:addLoginExist!");		     
		    }catch (NoResultException ex){
              log.error("UsrManager:loginExist:NoResultException");
           }catch(Exception e){
	           log.error("UsrManager:loginExist:Error:"+e);
	           throw e;
         }
		}
		return this.addLoginExist;
   }
   
   public void selectRecord(){
	    String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	    log.info("selectRecord:sessionId="+sessionId);
	    
	   //  forView(); //!!!
	    ArrayList<String> selRecUsr = (ArrayList<String>)
				  Component.getInstance("selRecUsr",ScopeType.SESSION);
	    
	    if(selRecUsr==null){
	       selRecUsr = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	   // AcUser au = searchBean(sessionId);
	    AcUser au = new AcUser();
	  // � getAuditList : else{it.setSelected(false);}
	    
	    if(au!=null){ 
	     if(selRecUsr.contains(sessionId)){
	    	selRecUsr.remove(sessionId);
	    	au.setSelected(false);
	    	 log.info("selectRecord:02");
	     }else{
	    	selRecUsr.add(sessionId);
	    	au.setSelected(true);
	    	log.info("selectRecord:03");
	    }
	    Contexts.getSessionContext().set("selRecUsr", selRecUsr);	
	    
	    Contexts.getEventContext().set("usrBean", au);
	    }
    }
  
   public boolean getDisabled(String idArm, String roleSign, String usrBeanIdUser) throws Exception {
		boolean result = true;
	   
	   log.info("UsrManager:getDisabled:idArm:"+idArm);
	   log.info("UsrManager:getDisabled:roleSign:"+roleSign);
	   log.info("UsrManager:getDisabled:usrBeanIdUser:"+usrBeanIdUser);
	   
		if(idArm!=null && roleSign!=null){
		  try{
			 
			//  LinksMap linksMap= (LinksMap)Component.getInstance("linksMap",ScopeType.APPLICATION);
			//  AcUser currentUser = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
			  
			  LinksMap linksMap= getLinksMap();
			  AcUser currentUser = getCurrentUser();
			  
			  result=!(currentUser.getIdUser().intValue()!=linksMap.getSuperUserCode().intValue() || new Long(usrBeanIdUser).intValue()!=linksMap.getSuperUserCode().intValue()) 
                      || !(new Long(idArm).intValue()!=linksMap.getAppCode().intValue() || !roleSign.equals("role:urn:sys_admin_cud") || currentUser.getIdUser().intValue()==linksMap.getSuperUserCode().intValue())
                      || (currentUser.getIsSysAdmin().intValue()!=1 && new Long(idArm).intValue()==linksMap.getAppCode().intValue());
			  
			  log.info("UsrManager:getDisabled:result:"+result);		     
		    }catch(Exception e){
	           log.error("UsrManager:getDisabled:Error:"+e);
	           throw e;
          }
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
   

   
   public Boolean getEvaluteForList() {
	
   	log.info("usrManager:evaluteForList:01");
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
    		log.info("usrManager:evaluteForList!!!");
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
