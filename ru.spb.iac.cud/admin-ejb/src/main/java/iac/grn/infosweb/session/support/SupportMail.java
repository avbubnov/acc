package iac.grn.infosweb.session.support;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;

import java.util.*;
import javax.persistence.EntityManager;



/**
 * ”правл€ющий Ѕин аудита
 * @author bubnov
 *
 */
@Name("supportMail")
public class SupportMail implements Runnable {//implements OrgManagerInterface{
	
	@Logger private Log log;
	
	@In 
	EntityManager entityManager;
	
	@In(create=true)
	private Renderer renderer;

	private String etoMail="bubnov@iac.spb.ru";
	
	private String helpFio;
	
	private String helpPost;
	
	private String helpMail;
	
	private String helpText;
	
	private String helpTel;
	
	public void init(String help_fio, String help_post, String help_mail, String help_text, String help_tel) {
		
		log.info("supportMail:init:01");
	       
	    helpFio=help_fio;
	    helpPost=help_post;
	    helpMail=help_mail;
	    helpText=help_text;
	    helpTel=help_tel;
	    
	    log.info("supportMail:init:02");
	}
	public void run() {
		log.info("supportMail:run:01");
		send();
		log.info("supportMail:run:02");
	}
	
	
	public void send(/*String help_fio, String help_mail, String help_text*/) {
		//((IssBean)Component.getInstance("issBean")).setSprtqstRendered(false);
	    try {
	       log.info("supportMail:send:01");
	       
	    /*   helpFio=help_fio;
	       helpMail=help_mail;
	       helpText=help_text;*/
	       
	       renderer.render("/services/support/mail.xhtml");
	       
	       log.info("supportMail:send:02");
	   	//  FacesMessages.instance().addToControl("info", "ѕисьмо отправлено!");
	  	   
	    } 
	   catch (Exception e) {
		   log.error("supportMail:send:ERROR="+e);
		   e.printStackTrace(System.out);
		//   FacesMessages.instance().addToControl("info_mail", "ѕисьмо не отправлено!");
	     }
	}
   public String getHelpFio(){
	   return this.helpFio;
   }
   public void setHelpFio(String helpFio){
	   this.helpFio=helpFio;
   }
   
   public String getHelpPost(){
	   return this.helpPost;
   }
   public void setHelpPost(String helpPost){
	   this.helpPost=helpPost;
   }
   
   public String getHelpMail(){
	   return this.helpMail;
   }
   public void setHelpMail(String helpMail){
	   this.helpMail=helpMail;
   }
   
   public String getHelpText(){
	   return this.helpText;
   }
   public void setHelpText(String helpText){
	   this.helpText=helpText;
   }
   
   public String getEtoMail(){
	   return this.etoMail;
   }
   public void setEtoMail(String etoMail){
	   this.etoMail=etoMail;
   }
   
   public String getHelpTel(){
	   return this.helpTel;
   }
   public void setHelpTel(String helpTel){
	   this.helpTel=helpTel;
   }
}

