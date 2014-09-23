package iac.grn.infosweb.session.audit.export;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.infosweb.dataitems.NavigItem;
import iac.cud.infosweb.entity.AcAppPage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import ru.spb.iac.cud.items.AuditFunction;


/**
 * @author bubnov
 *
 */
@Name("auditExportData")
//@Scope(ScopeType.SESSION)
public class AuditExportData {

	 @Logger private Log log;
	 
//	 private List<AuditFunction> funcList = new ArrayList<AuditFunction>();
	 
/*	 public List<AuditFunction> getFuncList(){
		 
		 return this.funcList;
	 }
	 
    public void setFuncList(List<AuditFunction> funcList){
		 this.funcList=funcList;
	 }*/
	 
	 public synchronized void addFunc(String idFunction){
		
		 log.info("AuditExportData:addFunc:01");
		 
		 try{
			 HttpSession session = (HttpSession)  FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			
			 List<AuditFunction> funcList =  (List<AuditFunction>) session.getAttribute("auditExportFuncList");
			 
		 if(funcList==null){
			// log.info("AuditExportData:addFunc:02");
			 
			 funcList = new ArrayList<AuditFunction>();
			 session.setAttribute("auditExportFuncList", funcList);
		 }
		 
		// log.info("AuditExportData:addFunc:03");
		 AuditFunction func = new AuditFunction();
		 func.setDateFunction(new Date());
		 func.setCodeFunction(idFunction);
		 
		 funcList.add(func);
		 
		 }catch(Exception e){
			 log.error("AuditExportData:addFunc:ERROR:"+e);
		 }
	 }
}

