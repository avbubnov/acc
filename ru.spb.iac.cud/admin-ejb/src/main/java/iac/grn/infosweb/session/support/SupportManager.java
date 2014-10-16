package iac.grn.infosweb.session.support;


import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import iac.cud.infosweb.entity.AcUser;
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
public class SupportManager {
	
	@Logger private Log log;
	
	@In 
	EntityManager entityManager;
	
	public void sendMail(String help_fio, String help_post, String help_mail, String help_text,  String help_tel){
		log.info("supportManager:sendMail:01");
		SupportMail sm = (SupportMail)
				Component.getInstance("supportMail", ScopeType.EVENT);
		
		sm.init(help_fio, help_post, help_mail, help_text, help_tel);
		
		Thread t = new Thread(sm);
        t.start();
		
        logMail(help_fio, help_post, help_mail, help_text, help_tel);
        
		log.info("supportManager:sendMail:02");
	}
	private void logMail(String help_fio, String help_post, String help_mail, String help_text,  String help_tel){
		  log.info("supportManager:logMail:01");
		  
		  try{
			AcUser au = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);   
			
			Transaction.instance().begin();
			
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
		
	
	
}

