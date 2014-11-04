package iac.grn.infosweb.context.mc.ugroup;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.UserItem;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.GroupUsersKnlT;
import iac.cud.infosweb.entity.LinkGroupUsersUsersKnlT;
import iac.grn.infosweb.context.mc.rol.RolUsrManager;
import iac.grn.infosweb.context.mc.usr.UsrContext;
import iac.grn.infosweb.context.mc.usr.UsrDataModel;
import iac.grn.infosweb.context.mc.usr.UsrStateHolder;
import iac.grn.infosweb.session.audit.actions.ActionsMap;
import iac.grn.infosweb.session.audit.actions.ResourcesMap;
import iac.grn.serviceitems.BaseTableItem;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELException;
import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.richfaces.model.ExtendedFilterField;
import org.richfaces.model.FilterField;
import org.richfaces.model.Modifiable;
import org.richfaces.model.SortField2;
 

@Name("ugroupUsrManager")
public class UgroupUsrManager {
	
	@Logger private Log log;
	
	@In 
	EntityManager entityManager;
	
	private List <BaseTableItem> auditItemsListSelect;
	
    private List<BaseItem> auditList; 
	
	private Long auditCount;
	
	private Boolean evaluteForList;
	private Boolean evaluteForListFooter;  
	
	public List<BaseItem> getAuditList(int firstRow, int numberOfRows){
		  
		  String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   String sessionId = FacesContext.getCurrentInstance().getExternalContext()
 			        .getRequestParameterMap()
 			        .get("sessionId");
		   
		   log.info("ugroupUsrManager:getAuditList:remoteAudit:"+remoteAudit);
		   log.info("ugroupUsrManager:getAuditList:sessionId:"+sessionId);
		   
		  
		  log.info("ugroupUsrManager:getAuditList:firstRow:"+firstRow);
		  log.info("ugroupUsrManager:getAuditList:numberOfRows:"+numberOfRows);
		  
		  List<BaseItem> ugroupUsrListCached = (List<BaseItem>)
				  Component.getInstance("ugroupUsrListCached",ScopeType.SESSION);
		  if(auditList==null){
			  log.info("ugroupUsrManager:getAuditList:01");
			 	if(("rowSelectFact".equals(remoteAudit)||
				    "selRecAllFact".equals(remoteAudit)||
				    "clRecAllFact".equals(remoteAudit)||
				    "clSelOneFact".equals(remoteAudit)||
				    "onSelColSaveFact".equals(remoteAudit))&&
				    ugroupUsrListCached!=null){
			 		log.info("ugroupUsrManager:getAuditList:02:"+ugroupUsrListCached.size());
				    	this.auditList=ugroupUsrListCached;
				}else{
					log.info("ugroupUsrManager:getAuditList:03");
			    	invokeLocal("list", firstRow, numberOfRows, null);
				    Contexts.getSessionContext().set("ugroupUsrListCached", this.auditList);
				    log.info("ugroupUsrManager:getAuditList:03:"+this.auditList.size());
				}
			 	
			 	
			 	
			 	try{
			 		 List<Long> listUsr=entityManager.createQuery(
	   	 		    		 "select o.idUser from AcUser o,  LinkGroupUsersUsersKnlT o1 " +
	   	 		    		 "where o1.pk.acUser = o.idUser " +
	   	 		    		 "and o1.pk.groupUser = :groupUser ")
	   	 					 .setParameter("groupUser", new Long(sessionId))
	   	 		      		 .getResultList();
	   	 		 
	   	    	     log.info("UsrManager:getUsrAlfList:size:"+listUsr.size());
	   	    	     
	   	 		     for(BaseItem user :this.auditList){
	   	 	           if (listUsr.contains(user.getBaseId())){  
	   	 	        	  ((UserItem)user).setUsrChecked(true);
	   	 			   }
	   	 	         } 
			 		
			 	}catch(Exception e){
			 		
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
				 log.info("ugroupUsrManager:invokeLocal");
				 
				 UgroupUsrStateHolder ugroupUsrStateHolder = (UgroupUsrStateHolder)
						  Component.getInstance("ugroupUsrStateHolder",ScopeType.SESSION);
				 
				 Map<String, String> filterMap = ugroupUsrStateHolder.getColumnFilterValues();
				 String st=null;
				 
				  RolUsrManager rolUsrManager = (RolUsrManager)
	     		          Component.getInstance("rolUsrManager");
				  
				 if("list".equals(type)){
					 log.info("ugroupUsrManager:list:01");
					 
					 Set<Map.Entry<String, String>> set = ugroupUsrStateHolder.getSortOrders().entrySet();
	                 for (Map.Entry<String, String> me : set) {
	      		        
	      		       if(orderQuery==null){
	      		    	 orderQuery="order by "+me.getKey()+" "+me.getValue();
	      		       }else{
	      		    	 orderQuery=orderQuery+", "+me.getKey()+" "+me.getValue();  
	      		       }
	      		     }
	                 log.info("ugroupUsrManager:invokeLocal:list:orderQuery:"+orderQuery);
	                 
	                 if(filterMap!=null){
	    	    		 Set<Map.Entry<String, String>> setFilterUgroupUsr = filterMap.entrySet();
	    	              for (Map.Entry<String, String> me : setFilterUgroupUsr) {
	    	            	 
	    	   		     if("t1_crt_date".equals(me.getKey())){  
	    	        	   
	    	        	   //������ ������ �� ������  
	    	        	     st=(st!=null?st+" and " :"")+" lower(to_char("+me.getKey()+",'DD.MM.YY HH24:MI:SS')) like lower('"+me.getValue()+"%') ";
	    	    	     }else{
	    	        		//������ ������ �� ������
	    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
	    	        	  }
	    	              }
	    	    	   }
	                 log.info("ugroupUsrManager:invokeLocal:list:filterQuery:"+st);
	                 
	 	        
	               AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		      		 
	               if(cau.getIsAccOrgManagerValue()){
	          	 	   st=(st!=null?st+" and ":" ")+" t1_org_code = '"+cau.getUpSign()+"' ";
	               }
	               
	             
	               
	               auditList = rolUsrManager.getSharedUserList( orderQuery, st, firstRow, numberOfRows);
	               
	               
	               
	             log.info("ugroupUsrManager:invokeLocal:list:02");
	             
				 } else if("count".equals(type)){
					 log.info("ugroupUsrManager:count:01");
					 
	                 
	                 if(filterMap!=null){
	    	    		 Set<Map.Entry<String, String>> setFilterUgroupUsr = filterMap.entrySet();
	    	              for (Map.Entry<String, String> me : setFilterUgroupUsr) {
	    	               
	    	   		   		//������ ������ �� ������
	    	            	  st=(st!=null?st+" and " :"")+" lower("+me.getKey()+") like lower('"+me.getValue()+"%') ";
	    	        	
	    	              }
	    	    	   }
					 
	                 AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
		      		 
		               if(cau.getIsAccOrgManagerValue()){
		          	 	   st=(st!=null?st+" and ":" ")+" t1_org_code = '"+cau.getUpSign()+"' ";
		               }
					
		               //!!!
		              auditCount = rolUsrManager.getSharedUserCount (st);
			              
				      
	                 
	               log.info("ugroupUsrManager:invokeLocal:count:02:"+auditCount);
	           	 } else if("bean".equals(type)){
					 
				 }
			}catch(Exception e){
				  log.error("invokeLocal:error:"+e);
				  evaluteForList=false;
				  FacesMessages.instance().add("������!");
			}
		}
	
		 public Long getAuditCount(){
			   log.info("getAuditCount");
			 
			   invokeLocal("count",0,0,null);
			  
			   return auditCount;
			  
		   }	
		
		 public void updUgroupUserAlf(){
			   
			   log.info("ugroupUsrManager:updUgroupUserAlf:01");
			   
			   LinkGroupUsersUsersKnlT lguu=null;
			   
			   
			   
			   GroupUsersKnlT ugroupBean = (GroupUsersKnlT)
						  Component.getInstance("ugroupBean",ScopeType.CONVERSATION);
			   
				   String  sessionId = FacesContext.getCurrentInstance().getExternalContext()
				        .getRequestParameterMap()
				        .get("sessionId");
			   log.info("ugroupUsrManager:updUgroupUserAlf:sessionId:"+sessionId);
			  
			   
			   if(ugroupBean==null || sessionId==null){
				   return;
			   }
			
			 	   
			   try {
				   AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
				   
				   GroupUsersKnlT aum = entityManager.find(GroupUsersKnlT.class, new Long(sessionId));
				   
				   List<LinkGroupUsersUsersKnlT> oldLinkList = aum.getLinkGroupUsersUsersKnlTs();
				   
				   log.info("ugroupUsrManager:updUgroupUserAlf:size1:"+oldLinkList.size());
				   log.info("ugroupUsrManager:updUgroupUserAlf:size2:"+(this.auditList!=null?this.auditList.size():"is null"));
				   
				   for(BaseItem user:this.auditList){
					  log.info("ugroupManager:updUgroupUserAlf:Login:"+((UserItem)user).getLogin());
					  log.info("ugroupManager:updUgroupUserAlf:UsrChecked:"+((UserItem)user).getUsrChecked());
					  
					  if(((UserItem)user).getUsrChecked().booleanValue()){ //�������
						
						 
						 lguu=new LinkGroupUsersUsersKnlT(user.getBaseId(), new Long(sessionId));
						 if(oldLinkList.contains(lguu)){  
						 
						 }else{//��� � ����
							 lguu.setCreated(new Date()); 
							 lguu.setCreator(au.getIdUser());
					         
					         oldLinkList.add(lguu);
					         
						  }
						  
					  }else{//�� �������
						 //���� � ����
						 lguu=new LinkGroupUsersUsersKnlT(user.getBaseId(), new Long(sessionId));
						 if(oldLinkList.contains(lguu)){ 
							oldLinkList.remove(lguu);
							entityManager.createQuery("DELETE FROM LinkGroupUsersUsersKnlT gu " +
									                  "WHERE gu.pk.groupUser=:groupUser " +
									                  "and gu.pk.acUser=:acUser ")
							    .setParameter("groupUser", new Long(sessionId))
							    .setParameter("acUser", user.getBaseId())
							    .executeUpdate();
						  }else{//� ���� � ��� ���
						 
						  }
					  }
				  }

				   
			        entityManager.flush();
			    	
				    entityManager.refresh(aum);
			    	  
			    	Contexts.getEventContext().set("ugroupBean", aum);
			    	 
			    	//�����!!!
			    	UgroupManager ugroupManager = (UgroupManager)Component.getInstance("ugroupManager", ScopeType.EVENT);
				    ugroupManager.audit(ResourcesMap.UGROUP, ActionsMap.UPDATE_USER); 
			    	
			    	
			    	
			     }catch (Exception e) {
		       log.error("ugroupUsrManager:updUgroupUserAlf:ERROR:"+e);
		     }
		}
		 
		 
	public List <BaseTableItem> getAuditItemsListSelect() {
		   
	
	    UsrContext ac= new UsrContext();
		   if( auditItemsListSelect==null){
			   log.info("getAuditItemsListSelect:02");
			   auditItemsListSelect = new ArrayList<BaseTableItem>();
			  
			   auditItemsListSelect.add(new BaseTableItem("", "", "usrChecked"));
			   
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("fio"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("login"));
			   auditItemsListSelect.add(ac.getAuditItemsMap().get("orgName"));
			  
			 
			   
			   
				   }
	       return this.auditItemsListSelect;
 }
 
 public void setAuditItemsListSelect(List <BaseTableItem> auditItemsListSelect) {
		    this.auditItemsListSelect=auditItemsListSelect;
 }
 
 public Boolean getEvaluteForList() {
		
	   	log.info("ugroupUsrManager:evaluteForList:01");
	   	if(evaluteForList==null){
	   		evaluteForList=false;
	    	String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		             .getRequestParameterMap()
		             .get("remoteAudit");
		   log.info("ugroupUsrManager:evaluteForList:remoteAudit:"+remoteAudit);
	     	
	    	if(remoteAudit!=null&&
	    	 
	    	   !"OpenCrtFact".equals(remoteAudit)&&	
	    	   !"OpenUpdFact".equals(remoteAudit)&&
	    	   !"OpenDelFact".equals(remoteAudit)&&
	   	       !"onSelColFact".equals(remoteAudit)&&
	   	       !"refreshPdFact".equals(remoteAudit)){
	    		log.info("ugroupUsrManager:evaluteForList!!!");
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
			   log.info("ugroupUsrManager:evaluteForListFooter:remoteAudit:"+remoteAudit);
		     
		    	if(getEvaluteForList()&&
		    	   //new-1-	
		    	   !"protBeanWord".equals(remoteAudit)&&	
		    	   //new-2-	
		   	       !"selRecAllFact".equals(remoteAudit)&&
		   	       !"clRecAllFact".equals(remoteAudit)&&
		   	      // !remoteAudit equals "clSelOneFact"
		   	       !"onSelColSaveFact".equals(remoteAudit)){
		    		log.info("ugroupUsrManager:evaluteForListFooter!!!");
		   		    evaluteForListFooter=true;
		    	}
		   	 }
		       return evaluteForListFooter;
		   }
 
	
}
