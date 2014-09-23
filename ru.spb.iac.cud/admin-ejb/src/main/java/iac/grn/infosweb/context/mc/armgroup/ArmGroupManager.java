package iac.grn.infosweb.context.mc.armgroup;

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
import iac.cud.infosweb.dataitems.UserItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.GroupSystemsKnlT;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.GroupUsersKnlT;
import iac.cud.infosweb.entity.LinkGroupSysSysKnlT;
import iac.cud.infosweb.entity.LinkGroupUsersUsersKnlT;
import iac.grn.infosweb.context.mc.usr.UsrGroupStateHolder;
import iac.grn.infosweb.context.mc.usr.UsrStateHolder;
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

import ru.spb.iac.cud.core.util.CUDConstants;

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
@Name("armGroupManager")
public class ArmGroupManager {
	
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
	
	private List<GroupSystemsKnlT> listArmGroup = null;
	
	private boolean armGroupCodeExist=false;
	
	private List<BaseItem> ISList;
	
	private List<BaseItem> ISSelectListForView;
	
	private Long idArmGroupCrt;
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
	  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("auditManager:getAuditList:remoteAudit:"+remoteAudit);
	  
	  
	  log.info("getAuditList:firstRow:"+firstRow);
	  log.info("getAuditList:numberOfRows:"+numberOfRows);
	  
	  List<BaseItem> armGroupListCached = (List<BaseItem>)
			  Component.getInstance("armGroupListCached",ScopeType.SESSION);
	  if(auditList==null){
		  log.info("getAuditList:01");
		 	if((remoteAudit.equals("rowSelectFact")||
			    remoteAudit.equals("selRecAllFact")||
			    remoteAudit.equals("clRecAllFact")||
			    remoteAudit.equals("clSelOneFact")||
			    remoteAudit.equals("onSelColSaveFact"))&&
			    armGroupListCached!=null){
		 	//	log.info("getAuditList:02:"+orgListCached.size());
			    	this.auditList=armGroupListCached;
			}else{
				log.info("getAuditList:03");
		    	invokeLocal("list", firstRow, numberOfRows, null);
			    Contexts.getSessionContext().set("armGroupListCached", this.auditList);
			    log.info("getAuditList:03:"+this.auditList.size());
			}
		 	
		 	ArrayList<String> selRecArmGroup = (ArrayList<String>)
					  Component.getInstance("selRecArmGroup",ScopeType.SESSION);
		 	if(this.auditList!=null && selRecArmGroup!=null) {
		 		 for(BaseItem it:this.auditList){
				   if(selRecArmGroup.contains(it.getBaseId().toString())){
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
			 
			 ArmGroupStateHolder orgStateHolder = (ArmGroupStateHolder)
					  Component.getInstance("armGroupStateHolder",ScopeType.SESSION);
			 
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
    	   		      /* if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{*/
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	                 // }
    	              }
    	    	   }
                 log.info("invokeLocal:list:filterQuery:"+st);
                 
				// auditList = new ArrayList<BaseItem>();
				 auditList = entityManager.createQuery("select o from GroupSystemsKnlT o "+
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
    	   		     /*  if(me.getKey().equals("acIsBssTLong")){  
    	   		    	  st=(st!=null?st+" and " :" ")+me.getKey()+"='"+me.getValue()+"' ";
    	    	       }else{*/
    	        		// st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('%"+me.getValue()+"%') ";
    	        		//делаем фильтр на начало
    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
    	              // }
    	              }
    	    	   }
                 log.info("invokeLocal:count:filterQuery:"+st);
				 
				 
				 auditCount = (Long)entityManager.createQuery(
						 "select count(o) " +  
				         "from GroupSystemsKnlT o "+
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
			if(modelType.equals("armGroupDataModel")){
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
		 GroupSystemsKnlT ar = searchBean(sessionId);
		 
		 if(ar==null){
			 ar = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
		 }
		 
		 if(!isAllowedSys(new Long(sessionId))){
			 log.info("armGroupManager:forView:02");
			 ar.setIsAllowedSys(false);
		 }		 
		 
		 Contexts.getEventContext().set("armGroupBean", ar);
		 
		 forViewCert();
	  }
   }
   
   public boolean isAllowedSys(Long idGr){
	   log.info("armGroupManager:isAllowedSys:01:"+idGr);
	 
	   boolean result = false;
	   
	   try {
	   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
  		 
   		  if(au.getAllowedSys()!=null){
   		   
   			//список ИС в группе, которых нет в привязке к пользователю  
		    List<Object> list= entityManager.createNativeQuery(
                        "SELECT LGS.UP_SYSTEMS " + 
                        "   FROM GROUP_SYSTEMS_KNL_T gr, LINK_GROUP_SYS_SYS_KNL_T lgs " + 
                        "   WHERE     GR.ID_SRV = LGS.UP_GROUP_SYSTEMS " + 
                        "         AND GR.ID_SRV = :idGr " + 
                        "         AND LGS.UP_SYSTEMS NOT IN (:idsArm) " + 
                        "GROUP BY LGS.UP_SYSTEMS ")
                      .setParameter("idGr", idGr)
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
	       log.error("armGroupManager:isAllowedSys:ERROR:"+e);
	    }
	   
	   log.info("armGroupManager:isAllowedSys:02:"+result);
	   
	   return result;
	}
   
   
   private GroupSystemsKnlT searchBean(String sessionId){
    	
      if(sessionId!=null){
    	 List<GroupSystemsKnlT> armGroupListCached = (List<GroupSystemsKnlT>)
				  Component.getInstance("armGroupListCached",ScopeType.SESSION);
		if(armGroupListCached!=null){
			for(GroupSystemsKnlT it : armGroupListCached){
				 
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
   
   public void addArmGroup(){
	   log.info("armGroupManager:addOrg:01");
	   
	   GroupSystemsKnlT armGroupBeanCrt = (GroupSystemsKnlT)
				  Component.getInstance("armGroupBeanCrt",ScopeType.CONVERSATION);
	   
	   if(armGroupBeanCrt==null){
		   return;
	   }
	 
	   try {
		  
		 if(!armGroupCodeExistCrt(/*armGroupBeanCrt.getAcIsBssTLong(),*/ 
				 CUDConstants.groupArmPrefix+armGroupBeanCrt.getGroupCode().trim())){
		   
		  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
		  
		  armGroupBeanCrt.setGroupName(armGroupBeanCrt.getGroupName().trim());
		  armGroupBeanCrt.setGroupCode(CUDConstants.groupArmPrefix+armGroupBeanCrt.getGroupCode().trim());
		  /*
		  if(armSubBeanCrt.getCertAlias()!=null&&!armSubBeanCrt.getCertAlias().trim().equals("")){
			  armSubBeanCrt.setCertAlias(armSubBeanCrt.getCertAlias().trim());
		  }else{
			  armSubBeanCrt.setCertAlias(null);
		  }*/
		  
		  if(armGroupBeanCrt.getDescription()!=null&&!armGroupBeanCrt.getDescription().trim().equals("")){
			  armGroupBeanCrt.setDescription(armGroupBeanCrt.getDescription().trim());
		  }else{
			  armGroupBeanCrt.setDescription(null);
		  }
		  
		  armGroupBeanCrt.setCreator(au.getIdUser());
		  armGroupBeanCrt.setCreated(new Date());
	      entityManager.persist(armGroupBeanCrt);
	    
	      entityManager.flush();
 	  	  entityManager.refresh(armGroupBeanCrt);
	    
	      idArmGroupCrt=armGroupBeanCrt.getBaseId();
	     // audit(ResourcesMap.IS, ActionsMap.CREATE); 
	      
	    }
	      
	    }catch (Exception e) {
	       log.error("armGroupManager:addArmGroup:ERROR:"+e);
	    }
	   
   }
   
   public void updArmGroup(){
	   
	   log.info("armGroupManager:updArmGroup:01");
	   
	   GroupSystemsKnlT armGroupBean = (GroupSystemsKnlT)
				  Component.getInstance("armGroupBean",ScopeType.CONVERSATION);
	   
	   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("armGroupManager:updArmGroup:sessionId:"+sessionId);
	
	   if(armGroupBean==null || sessionId==null){
		   return;
	   }
	
	   try {
		   
		 if(!armGroupCodeExistUpd(/*armGroupBean.getAcIsBssTLong(),*/ 
				 armGroupBean.getGroupCode().trim(), new Long(sessionId))){  
		
		 AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		   
		 GroupSystemsKnlT aam = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
		  
		  aam.setGroupName(armGroupBean.getGroupName().trim());
		  aam.setGroupCode(/*CUDConstants.groupArmPrefix+*/armGroupBean.getGroupCode().trim());
		  
		  if(armGroupBean.getDescription()!=null&&!armGroupBean.getDescription().trim().equals("")){
			  aam.setDescription(armGroupBean.getDescription().trim());
		  }else{
			  aam.setDescription(null);
		  }
		 // aam.setAcIsBssTLong(armGroupBean.getAcIsBssTLong());
			  
		  aam.setModificator(au.getIdUser());
		  aam.setModified(new Date());
		  
		  entityManager.flush();
	      entityManager.refresh(aam);
	    	  
	    	//  usrBean = entityManager.find(AcUser.class, new Long(sessionId)/*usrBean.getIdUser()*/);
	      Contexts.getEventContext().set("armGroupBean", aam);
		
	    //  audit(ResourcesMap.IS, ActionsMap.UPDATE); 
	      
		 }
		 
	     }catch (Exception e) {
           log.error("armGroupManager:updSrm:ERROR:"+e);
         }
   }
   
    public void updIS(){
	   
	   log.info("armGroupManager:updIS:01");
	   
	  // List<LinkGroupUsersUsersKnlT> arList = new ArrayList<LinkGroupUsersUsersKnlT>();
	  // List<LinkGroupUsersUsersKnlT> arRemovedList = new ArrayList<LinkGroupUsersUsersKnlT>();
	   
	   //AcUser usrBean = (AcUser)
		//		  Component.getInstance("usrBean",ScopeType.CONVERSATION);
	   
	   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
		        .getRequestParameterMap()
		        .get("sessionId");
	   log.info("armGroupManager:updIS:sessionId:"+sessionId);
	
	   if(/*usrBean==null ||*/ sessionId==null){
		   return;
	   }
	
	   AcUser currentUser = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
	   
	   try {
		   
		   GroupSystemsKnlT aum = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
	/*
		 
		  for(LinkGroupUsersUsersKnlT apl : aum.getLinkGroupUsersUsersKnlTs()){
			  
			   entityManager.remove(apl);
			 //  arRemovedList.add(apl);
		  }

		  aum.setLinkGroupUsersUsersKnlTs(null);
		  
		  entityManager.flush();
		 */ 
		//  log.info("usrManager:updUsrRole:size2:"+aum.getAcLinkUserToRoleToRaions().size());
		
		  List<LinkGroupSysSysKnlT> guuExistList =  aum.getLinkGroupSysSysKnlTs();
		  log.info("armGroupManager:updIS:size1:"+guuExistList.size());
		  
		  
		  //log.info("armGroupManager:updIS:size2:"+this.ISList.size());
		  
		  for(BaseItem is:this.ISList){
	    			  log.info("UsrManager:editUsr:updUsrGroup:"+((AcApplication)is).getName());
	    			  log.info("UsrManager:editUsr:updUsrGroup:"+((AcApplication)is).getUsrChecked());
	    			  
	    			  if(((AcApplication)is).getUsrChecked().booleanValue()){// отмечен
	    				  
	    				  LinkGroupSysSysKnlT au = new LinkGroupSysSysKnlT( ((AcApplication)is).getIdArm(), new Long(sessionId));
	    			            au.setCreated(new Date());
	    			            au.setCreator(currentUser.getBaseId());
	    			            
	    			         if(guuExistList.contains(au)){//есть в базе
	    			        	 //ничего не делаем. и так есть в базе
	    			        	 log.info("armGroupManager:updIS:02");
	    			         }else{
	    			           // arList.add(au);
	    			            guuExistList.add(au);
	    			            log.info("armGroupManager:updIS:03");
	    			         }
	    			  }else{ // не отмечен
	    				  LinkGroupSysSysKnlT au = new LinkGroupSysSysKnlT( ((AcApplication)is).getIdArm(), new Long(sessionId));
 		            
 			                if(guuExistList.contains(au)){//есть в базе
 			                	
 			                	guuExistList.remove(au);
 			                	
 			                	entityManager.createQuery(
 			                		"delete from LinkGroupSysSysKnlT lgu " +
 			                		"where lgu.pk.acIsBssT = :acIsBssT " +
 			                		"and lgu.pk.groupSystemsKnlT = :groupSystemsKnlT ")
 			                	.setParameter("acIsBssT", ((AcApplication)is).getIdArm())
 			                	.setParameter("groupSystemsKnlT", new Long(sessionId))
 			                	.executeUpdate();
 			                	
 			                	 log.info("armGroupManager:updIS:04");
 			                }else{
 			                //ничего не делаем. и так нет в базе
 			                	 log.info("armGroupManager:updIS:05");
 			                }
	    			  }
	    		  }
	     	  
	     	/*if(arList.size()>0){
	    	 	//  @OneToMany(mappedBy="acHost", cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	    		//  aum.getAcLinkUserToRoleToRaions().addAll(arList) ;
	    		  aum.setLinkGroupUsersUsersKnlTs(arList) ;
	    	}*/
	     	
	     	
	   	log.info("armGroupManager:updIS:size3:"+aum.getLinkGroupSysSysKnlTs().size());
	    	  
	        entityManager.flush();
	    	 
	        entityManager.refresh(aum);
	    	  
	    	//Contexts.getEventContext().set("usrBean", aum);
	    	
        
	    	 
         GroupSystemsKnlT ar = searchBean(sessionId);
         if(ar==null){
			 ar = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
		 }
         
		 Contexts.getEventContext().set("armGroupBean", ar);
		 
		 forViewCert();
		 
	    /*	//как сделано в UsrManager
	     UserItem au = (UserItem) searchBean (sessionId);
	     if(au!=null){
	    		Contexts.getEventContext().set("usrBeanView", au); 
	    	}else{
	    		 au = getUserItem(new Long(sessionId));
	    		 if(au!=null){
	    		   Contexts.getEventContext().set("usrBeanView", au); 
	    		 }
	    	 }
            */
 
	    	ISReset();
	    	
	    	audit(ResourcesMap.USER, ActionsMap.UPDATE_GROUP); 
	    	
	     }catch (Exception e) {
        log.error("armGroupManager:updIS:ERROR:"+e);
      }
}

    public void saveArmGroupCertificate(byte[] file_byte, Long id_sys){
	   
	   log.info("armGroupManager:saveArmGroupCertificate:01:"+(file_byte!=null));
	   log.info("armGroupManager:saveArmGroupCertificate:02:"+id_sys);
	   
	   try {
		   
		  
		   
		   CertificateFactory user_cf = CertificateFactory.getInstance("X.509");
           X509Certificate user_cert = (X509Certificate)
        		   user_cf.generateCertificate(new  ByteArrayInputStream(file_byte));
          // String x509Cert = org.picketlink.common.util.Base64.encodeBytes(user_cert.getEncoded());
           String x509Cert = Base64.encode(user_cert.getEncoded());
           
           log.info("armGroupManager:saveArmGroupCertificate:03:"+x509Cert);
           
           Transaction.instance().begin();
		   
		   Transaction.instance().enlist(entityManager);
		   
		   entityManager.createNativeQuery("update GROUP_SYSTEMS_KNL_T t1 " + 
		   		                           "set T1.CERT_DATA=? " + 
		   		                           "where t1.ID_SRV=? ")
		   .setParameter(1, x509Cert)
		   .setParameter(2, id_sys)
		   .executeUpdate();  
			 
		   Transaction.instance().commit();
		   
	     }catch (Exception e) {
           log.error("armGroupManager:saveArmGroupCertificate:ERROR:"+e);
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
 		   log.info("armGroupManager:removeCert:sessionId:"+sessionId);
 		
 		   if(sessionId==null||sessionId.trim().equals("")){
 			   return;
 		   }
 			   
 		   entityManager.createNativeQuery("update GROUP_SYSTEMS_KNL_T t1 " + 
 		   		                           "set T1.CERT_DATA=null " + 
 		   		                           "where t1.ID_SRV=? ")
 		   .setParameter(1, new Long(sessionId))
 		   .executeUpdate();  
 			 
 			   
 	     }catch (Exception e) {
            log.error("armGroupManager:removeCert:ERROR:"+e);
          }
   }
    
   public void delArmGroup(){
	 try{
		log.info("armGroupManager:delArmGroup:01");  
		
		GroupSystemsKnlT armGroupBean = (GroupSystemsKnlT)
				  Component.getInstance("armGroupBean",ScopeType.CONVERSATION);
		// <h:inputHidden value="#{armGroupBean.idArmGroup}"/>
		
		if(armGroupBean==null){
			return;
		}
		 
		log.info("armGroupManager:delArmGroup:IdArmGroup:"+armGroupBean.getBaseId());
		
		GroupSystemsKnlT aom = entityManager.find(GroupSystemsKnlT.class, armGroupBean.getBaseId());
		  
		entityManager.remove(aom);
		
		audit(ResourcesMap.IS, ActionsMap.DELETE); 
		
	 }catch(Exception e){
		 log.error("armGroupManager:delArmGroup:error:"+e); 
	 }
    }
 
    public void forViewUpdDel() {
	   try{
	     String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
	     log.info("forViewUpdDel:sessionId:"+sessionId);
	     if(sessionId!=null){
	    	 GroupSystemsKnlT ao = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
	    	 Contexts.getEventContext().set("armGroupBean", ao);
	    	 
	    	//устанавливаем на 1 страницу пагинатор в модальном окне
	    	 ArmGroupStateHolder armGroupStateHolder = (ArmGroupStateHolder)
					  Component.getInstance("armGroupStateHolder",ScopeType.SESSION);
	    	 armGroupStateHolder.resetPageNumber();
	    	 
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
			  
			  GroupSystemsKnlT aa = entityManager.find(GroupSystemsKnlT.class, new Long(sessionId));
			
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
			 
			 Contexts.getEventContext().set("armGroupBean", aa);
			 
			 
			 
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
 	    			 "select to_char(T1.CERT_DATA) " + 
 	    	 		 "from GROUP_SYSTEMS_KNL_T t1 " + 
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
 	          
 	           
 	           log.info("armGroupManager:saveArmGroupCertificate:02:"+user_cert);
 	         
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
   
   public List<GroupSystemsKnlT> getListArmGroup() throws Exception{
	    log.info("armGroupManager::getListArmGroup:01");
	    try {
	    	if(listArmGroup==null){
	       		listArmGroup=entityManager.createQuery("select o from AcSubsystemCertBssT o").getResultList();
	    	}
	     } catch (Exception e) {
	    	 log.error("armGroupManager::getListArmGroup:ERROR:"+e);
	         throw e;
	     }
	    return listArmGroup;
  }
   
   public List <BaseTableItem> getAuditItemsListSelect() {
		 log.info("getAuditItemsListSelect:01");
	
	    ArmGroupContext ac= new ArmGroupContext();
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
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("groupName"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("groupCode"));
			   //auditItemsListSelect.add(ac.getAuditItemsMap().get("armName"));
		   }
	       return this.auditItemsListSelect;
   }
   
   public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
   }
   
   public List <BaseTableItem> getAuditItemsListContext() {
	   log.info("orgManager:getAuditItemsListContext");
	   if(auditItemsListContext==null){
		   ArmGroupContext ac= new ArmGroupContext();
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
	    ArrayList<String> selRecArmGroup = (ArrayList<String>)
				  Component.getInstance("selRecArmGroup",ScopeType.SESSION);
	    
	    if(selRecArmGroup==null){
	       selRecArmGroup = new ArrayList<String>();
	       log.info("selectRecord:01");
	    }
	    
	    // AcApplication aa = searchBean(sessionId);
	    GroupSystemsKnlT aa = new GroupSystemsKnlT();
  	    // в getAuditList : else{it.setSelected(false);}
	    
	    if(aa!=null){
	     if(selRecArmGroup.contains(sessionId)){
	    	selRecArmGroup.remove(sessionId);
	    	aa.setSelected(false);
	    	log.info("selectRecord:02");
	     }else{
	    	selRecArmGroup.add(sessionId);
	    	aa.setSelected(true);
	    	log.info("selectRecord:03");
	     }
	    Contexts.getSessionContext().set("selRecArmGroup", selRecArmGroup);	
	    
	    Contexts.getEventContext().set("armGroupBean", aa);
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
   public boolean getArmGroupCodeExist() {
	    return armGroupCodeExist;
   }
   private boolean armGroupCodeExistCrt(String armGroupCode) throws Exception {
		log.info("ArmGroupManager:ArmGroupcodeExistCrt:armGroupCode="+armGroupCode);
		if(armGroupCode!=null){
		  try{
			  List<Object> lo=entityManager.createNativeQuery(
					  "select 1 "+
                      "from GROUP_SYSTEMS_KNL_T ass "+
                      "where ASS.GROUP_CODE = ? ")
	  				.setParameter(1, armGroupCode)
	  				.getResultList();
	  
	          if(!lo.isEmpty()){
		        armGroupCodeExist=true;
		        return this.armGroupCodeExist;
	          }
	          
	      	  log.info("ArmGroupManager:ArmGroupCodeExistCrt:addLoginExist!");	
			  
		  }catch(Exception e){
	           log.error("ArmGroupManager:ArmGroupCodeExistCrt:Error:"+e);
	           throw e;
       }
		}
		return this.armGroupCodeExist;
 }
  
  private boolean armGroupCodeExistUpd(String armGroupCode, Long idArmGroup) throws Exception {
	   
		log.info("ArmGroupManager:ArmGroupcodeExistUpd:armGroupCode:"+armGroupCode);
		log.info("ArmGroupManager:ArmGroupcodeExistUpd:idArmGroup:"+idArmGroup);
			
		if(armGroupCode!=null){
		  try{
			  List<Object> lo=entityManager.createNativeQuery(
					          "select 1 "+
		                      "from GROUP_SYSTEMS_KNL_T ass "+
		                      "where ASS.GROUP_CODE = ? " +
		                      "and ASS.ID_SRV!= ? ")
			  				.setParameter(1, armGroupCode)
			  				.setParameter(2, idArmGroup)
			  				.getResultList();
			  
			  if(!lo.isEmpty()){
				  armGroupCodeExist=true;
				  return this.armGroupCodeExist;
			  }
			  
			  if(!lo.isEmpty()){
		        armGroupCodeExist=true;
	          }
			  
			  log.info("ArmGroupManager:armGroupCodeExistUpd:addLoginExist!");		     
		    }catch(Exception e){
	           log.error("ArmGroupManager:armGroupCodeExistUpd:Error:"+e);
	           throw e;
         }
		}
		return this.armGroupCodeExist;
  }
  
  public List<BaseItem> getISList(){
		 
	   //в updUsrGroup вызывается usrGroupReset() - чтобы
	   // если перед сохранением изменить фильтр на фильтр_2, то в APPLY_REQUEST ИСПОЛЬЗУЮТСЯ
	   //ЕЩЁ ПРЕЖНИЕ ФИЛЬТРЫ - так и надо, но эти фильтры затем переустанавливаются на фильтр_2,
	   //и в commandButton есть reRender="..., outCBdtUpdArmGroup_dtbl, ..."
	   //а значит нам надо обновить groupList с учётом новых фильтров.
	   
	   if(this.ISList==null){
		  String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("sessionId");
		   String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("remoteAudit");
		   log.info("ArmGroupManager:getISList:sessionId:"+sessionId);
		   log.info("ArmGroupManager:getISList:remoteAudit:"+remoteAudit);
		   
		   if(sessionId==null){
			   return this.ISList;
		   }
		   
		   String st=null;
		 
		   ArmGroupISStateHolder armGroupISStateHolder = (ArmGroupISStateHolder)
					  Component.getInstance("armGroupISStateHolder",ScopeType.SESSION);
		   HashMap<String, String> filterMap = armGroupISStateHolder.getColumnFilterValues();
		   
		   
		   if(filterMap!=null){
	    		 Set<Map.Entry<String, String>> set_filter = filterMap.entrySet();
	              for (Map.Entry<String, String> me : set_filter) {
	            	  log.info("me.getKey+:"+me.getKey());
	            	  log.info("me.getValue:"+me.getValue());
	   		    
	             //аналог
	            if(me.getValue()!=null&&!me.getValue().equals("")){
	            	
	   		    /* if(me.getKey().equals("o.idSrv")){  
	        	   //делаем фильтр на начало  
	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+")) like lower('"+me.getValue()+"%') ";
	    	     }else{*/
	         		//делаем фильтр на начало
	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
	        	 // }
	             }
	           }
	      }
		   log.info("ArmGroupManager:getGroupList:st:"+st);
		   
		   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION); 
  		 
   		  if(au.getAllowedSys()!=null){
		   
		   this.ISList = entityManager.createQuery(
				   "select o from AcApplication o " +
				   "where o.idArm in (:idsArm) "+
					(st!=null ? " and "+st :" ")+
				   " order by o.name ")
				   .setParameter("idsArm", au.getAllowedSys())
				   .getResultList();
		 
   		  }else{
   			 this.ISList = entityManager.createQuery(
  				   "select o from AcApplication o " +
  					(st!=null ? " where "+st :" ")+
  				   " order by o.name ")
  				   .getResultList();
   		  }
		 //  log.info("ArmGroupManager:getGroupList:size:"+this.ISList.size());
		   
		  // if(remoteAudit!=null&&remoteAudit.equals("armSelectFact")){
		   
		     List<AcApplication> listIS=entityManager.createQuery(
		    		 "select o from AcApplication o JOIN o.linkGroupSysSysKnlTs o1 where o1.pk.groupSystemsKnlT = :acGroup ")
					 .setParameter("acGroup", new Long(sessionId))
		      		 .getResultList();
		   
		     for(BaseItem group :this.ISList){
	           if (listIS.contains((AcApplication)group)){  
	        	  ((AcApplication)group).setUsrChecked(true);
			   }
	         } 
		//  }
	   }
	   return this.ISList;
  }
  
  public void setISList(List<BaseItem> groupList){
	   this.ISList=groupList;
  }
  
  public void ISReset(){
	   if(this.ISList!=null){
		   this.ISList.clear();
		   this.ISList=null;  
	   }
  }
  public List<BaseItem> getISSelectListForView(){
  	
	   List<Object[]> lo=null;
      try{
     		
     	if(this.ISSelectListForView==null){
     		String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
 	             .getRequestParameterMap()
 	             .get("remoteAudit");
     		String sessionId = FacesContext.getCurrentInstance().getExternalContext()
    	             .getRequestParameterMap()
    	             .get("sessionId");
     		
     		if(remoteAudit==null||sessionId==null){
     			return null;
     		}
     		
 	        log.info("ArmGroupManager:getISSelectListForView:remoteAudit:"+remoteAudit);
 	        log.info("ArmGroupManager:getISSelectListForView:sessionId:"+sessionId);
 	        
 	        //if(!remoteAudit.equals("UpdFact")){
 	      /*   lo=entityManager.createNativeQuery(
 		    		"select t1.t1_id, t1.t1_login, t1.t1_fio, t1.t1_status "+
                   "from (select USR.ID_SRV t1_id, USR.UP_ISP_USER t1_flag, USR.LOGIN t1_login, "+ 
                   "IBT.SIGN_OBJECT t1_usr_code, "+
                    "decode(USR.UP_ISP_USER, null, USR.SURNAME||' '||USR.NAME_ ||' '|| USR.PATRONYMIC,  IBT.FIO ) t1_fio, "+
                     "USR.STATUS t1_status "+
                     "from "+
                     "AC_USERS_KNL_T usr, "+
                     "LINK_GROUP_USERS_USERS_KNL_T uul, "+
                     "ISP_BSS_T ibt "+ 
                     "where "+  
                     "USR.UP_ISP_USER=IBT.ID_SRV(+) "+
                     "and UUL.UP_USERS=USR.ID_SRV "+
                     "and UUL.UP_GROUP_USERS=? "+
                     "order by t1_fio "+
                     ") t1")
 		      		.setParameter(1, new Long(sessionId))
 				 .getResultList();*/
 	        
 	       lo=entityManager.createNativeQuery(
		    		"select ARM.FULL_, ARM.SIGN_OBJECT "+
                    "from AC_IS_BSS_T arm, "+
                    "GROUP_SYSTEMS_KNL_T garm, "+
                    "LINK_GROUP_SYS_SYS_KNL_T lga "+
                    "where ARM.ID_SRV=LGA.UP_SYSTEMS "+
                    "and GARM.ID_SRV=LGA.UP_GROUP_SYSTEMS "+
                    "and GARM.ID_SRV = ? ")
		      		.setParameter(1, new Long(sessionId))
				 .getResultList();
 	    	//}
 	         
 	       this.ISSelectListForView=new ArrayList<BaseItem>();
 	       
 	       for(Object[] objectArray :lo){
 	    	 AcApplication au = new AcApplication();
 	    	 
 	    	 this.ISSelectListForView.add(au);
 	    	 
 	    	 //не используется при только отображении
 	    	 //au.setIdUser(new Long(objectArray[0].toString()));
 	    	 au.setName(objectArray[0]!=null?objectArray[0].toString():"");
 	    	 au.setCode(objectArray[1]!=null?objectArray[1].toString():"");
 	       }
 	         
     	 }
     	}catch(Exception e){
 			log.error("ArmGroupManager:getISSelectListForView:error:"+e);
 		}
        	return this.ISSelectListForView;
     }
  
  public Long getIdArmGroupCrt() {
	return idArmGroupCrt;
  }
  public void setIdArmGroupCrt(Long idArmGroupCrt) {
	this.idArmGroupCrt = idArmGroupCrt;
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
	
   	log.info("armGroupManager:evaluteForList:01");
   	if(evaluteForList==null){
   		evaluteForList=false;
    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
	             .getRequestParameterMap()
	             .get("remoteAudit");
	   log.info("armGroupManager:evaluteForList:remoteAudit:"+remoteAudit);
     	
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
		   log.info("armGroupManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
	     
	    	if(getEvaluteForList()&&
	    	   //new-1-	
	    	   !remoteAudit.equals("protBeanWord")&&	
	    	   //new-2-	
	   	       !remoteAudit.equals("selRecAllFact")&&
	   	       !remoteAudit.equals("clRecAllFact")&&
	   	      // !remoteAudit.equals("clSelOneFact")&&
	   	       !remoteAudit.equals("onSelColSaveFact")){
	    		  log.info("armGroupManager:evaluteForListFooter!!!");
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
			    log.info("armGroupManager:evaluteForBean:remoteAudit:"+remoteAudit);
				String sessionId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("sessionId");
			    log.info("armGroupManager:evaluteForBean:sessionId:"+sessionId);
		    	if(sessionId!=null && remoteAudit!=null &&
		    	   (remoteAudit.equals("rowSelectFact")||	
		    	    remoteAudit.equals("UpdFact"))){
		    	      log.info("armGroupManager:evaluteForBean!!!");
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
