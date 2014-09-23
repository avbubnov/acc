package iac.grn.infosweb.session.support;


import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.HelpDeskPrototype;
import iac.grn.infosweb.session.navig.LinksMap;

import java.util.*;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;


/**
 * ”правл€ющий Ѕин аудита
 * @author bubnov
 *
 */
@Name("supportManager")
public class SupportManager {//implements OrgManagerInterface{
	
	@Logger private Log log;
	
	@In 
	EntityManager entityManager;
	
	private List<HelpDeskPrototype> supportUserList;
	
	public void sendMail(String help_fio, String help_post, String help_mail, String help_text,  String help_tel){
		log.info("supportManager:sendMail:01");
		SupportMail sm = (SupportMail)
				Component.getInstance("supportMail", ScopeType.EVENT);
		
		sm.init(help_fio, help_post, help_mail, help_text, help_tel);
		
		Thread t = new Thread(sm);
        t.start();
		
        logMail(help_fio, help_post, help_mail, help_text, help_tel);
        
		//sm.send(help_fio, help_mail, help_text);
		log.info("supportManager:sendMail:02");
	}
	private void logMail(String help_fio, String help_post, String help_mail, String help_text,  String help_tel){
		  log.info("supportManager:logMail:01");
		  
		  try{
			AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);   
			
			Transaction.instance().begin();
			
			/*Session session = (Session)entityManager.getDelegate();
			
			 session.createSQLQuery("insert into HELP_DESK_PROTOTYPE( "+
	          "ID_HELP, AUTHOR, POSITION,  PHONE,  EMAIL, MESSAGE, CREATOR) values( "+
	          "SQNC_HELP_DESK_PROTOTYPE.nextval, :p1, :p2, :p3, :p4, :p5, :p6)")
	          .setParameter("p1", help_fio)
	          .setParameter("p2", help_post)
	          .setParameter("p3", help_tel)
	          .setParameter("p4", help_mail)
	          .setParameter("p5", help_text)
	         .setParameter("p6", au.getIdUser())
	        .executeUpdate() ;*/
			
			 Transaction.instance().enlist(entityManager);
			 
			 LinksMap linksMap = (LinksMap)
					  Component.getInstance("linksMap",ScopeType.APPLICATION);
			 
			 entityManager.createNativeQuery("insert into HELP_DESK_PROTOTYPE( "+
	          "ID_HELP, AUTHOR, POSITION,  PHONE,  EMAIL, MESSAGE, CREATOR, ID_APP) values( "+
	          "SQNC_HELP_DESK_PROTOTYPE.nextval, :p1, :p2, :p3, :p4, :p5, :p6, :p7)")
	          .setParameter("p1", help_fio)
	          .setParameter("p2", help_post)
	          .setParameter("p3", help_tel)
	          .setParameter("p4", help_mail)
	          .setParameter("p5", help_text)
	         .setParameter("p6", au.getIdUser())
	         .setParameter("p7", linksMap.getAppCode())
	        .executeUpdate() ;
			 
			Transaction.instance().commit();
			 
		  }catch(Exception e){
			  log.error("supportManager:logMail:error:"+e); 
			  try{
			   Transaction.instance().rollback();
			  }catch(Exception et){}
		  }
		}
		
	public List<HelpDeskPrototype> getSupportUserList(){
		log.info("SupportManager:getSupportUserList:01");
	    if(this.supportUserList==null){
	    	
	    	log.info("SupportManager:getSupportUserList:02");
	    	
	      try{
	    	  
	    //  AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);    
	    	
	    	SupportStateHolder supportStateHolder = (SupportStateHolder)
					  Component.getInstance("supportStateHolder",ScopeType.SESSION);
	    	
	    	
	    	HashMap<String, String> filterMap=supportStateHolder.getColumnFilterValues().get("supportList");
	    	
	    	String author=null;
	    	
	    	if(filterMap!=null){
	    		 Set<Map.Entry<String, String>> set = filterMap.entrySet();
	              for (Map.Entry<String, String> me : set) {
	   		       System.out.println("me.getKey+:"+me.getKey());
	   		       System.out.println("me.getValue:"+me.getValue());
	   		      
	   		       author=me.getValue();
	   		      }
	    	   }
	    	
	    	
	    	LinksMap linksMap = (LinksMap)
					  Component.getInstance("linksMap",ScopeType.APPLICATION);
	    	
	    	this.supportUserList = entityManager
	    	  .createQuery("select hdp from HelpDeskPrototype hdp " +
	    	               "where hdp.idApp = :idApp "+
	    		 	       (author!=null && !author.equals("#-1#")?"and hdp.creator="+author:"") +
	    			       "order by hdp.created desc ")
	    	 //.setParameter("creator", au.getIdUser())
	    	.setParameter("idApp", linksMap.getAppCode())
	      	.getResultList();
	      }catch(Exception e){
	    	  log.error("SupportManager:getSupportUserList:Error:"+e);
	      }
	    }
		return this.supportUserList;
	}
		
		public void statusChange(){
			try{
			 String idMess = FacesContext.getCurrentInstance().getExternalContext()
		           .getRequestParameterMap()
		           .get("idMess");
			 log.info("supportManager:statusChange:idMess:"+idMess);
			 
			 if(idMess==null){
				 return;
			 }
			 HelpDeskPrototype hdp = entityManager.find(HelpDeskPrototype.class, new Long(idMess));
		//	 Contexts.getEventContext().set("supportMessBean", dn);
			 
			 if(hdp==null){
				 return;
			 }
			 
			 if(hdp.getIsDone()!=null && hdp.getIsDone().equals(1L)){
				hdp.setIsDone(0L);
				hdp.setDoneDate(null);
			 }else{
				hdp.setIsDone(1L); 
				hdp.setDoneDate(new Date());
			 }
			
			 
			 entityManager.flush();
			 entityManager.refresh(hdp);
			 
			}catch(Exception e){
				log.error("supportManager:statusChange:ERROR:"+e);
			}
		}
		
		public void filterAction() {
			 log.info("filterAction");
			 
			 this.supportUserList=null;
			 
			 SupportStateHolder supportStateHolder = (SupportStateHolder)
					  Component.getInstance("supportStateHolder",ScopeType.SESSION);
			 
			 supportStateHolder.clearFilters("supportList");
		}

}

