package iac.grn.infosweb.session.audit.export;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.infosweb.dataitems.NavigItem;
import iac.cud.infosweb.entity.AcAppPage;
import iac.cud.infosweb.entity.AcUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import iac.grn.infosweb.session.navig.LinksMap;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.items.AuditFunction;


/**
 * @author bubnov
 *
 */
@Name("auditExportData")
//@Scope(ScopeType.SESSION)
public class AuditExportData {

	 final static Logger LOGGER = LoggerFactory.getLogger(AuditExportData.class);
	 
//	 private List<AuditFunction> funcList = new ArrayList<AuditFunction>();
	 
/*	 public List<AuditFunction> getFuncList(){
		 
		 return this.funcList;
	 }
	 
    public void setFuncList(List<AuditFunction> funcList){
		 this.funcList=funcList;
	 }*/
	 
	 public synchronized void addFunc(String idFunction){
		
		 LOGGER.debug("AuditExportData:addFunc:01");
		 
		 try{
			 
			 AcUser cau = (AcUser) Component.getInstance("currentUser",ScopeType.SESSION);
			 
			 
		 if( FacesContext.getCurrentInstance()!=null) {
			 HttpSession session = (HttpSession)  FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			
			 List<AuditFunction> funcList =  (List<AuditFunction>) session.getAttribute("auditExportFuncList");
			 
		 if(funcList==null){
			 
			 
			 funcList = new ArrayList<AuditFunction>();
			 session.setAttribute("auditExportFuncList", funcList);
		 }
		 
		 
		 AuditFunction func = new AuditFunction();
		 func.setDateFunction(new Date());
		 func.setCodeFunction(idFunction);
		 
		 funcList.add(func);
		 
			 }
			 
		 LOGGER.info(cau.getFio()+":"+idFunction);
		 
		 }catch(Exception e){
			 LOGGER.error("AuditExportData:addFunc:ERROR:"+e);
		 }
	 }
}

