package iac.grn.infosweb.context.mc.audit.report;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.dataitems.ReportDownloadItem;
import iac.cud.infosweb.dataitems.UserItem;
import iac.cud.infosweb.entity.AcApplication;
import iac.cud.infosweb.entity.AcLinkUserToRoleToRaion;
import iac.cud.infosweb.entity.AcRole;
import iac.cud.infosweb.entity.AcUser;
import iac.cud.infosweb.entity.GroupUsersKnlT;
import iac.cud.infosweb.entity.LinkAdminUserSys;
import iac.cud.infosweb.entity.LinkGroupUsersUsersKnlT;
import iac.cud.infosweb.entity.ReportsBssT;
import iac.grn.infosweb.context.mc.usr.UsrContext;
import iac.grn.infosweb.context.mc.usr.UsrDataModel;
import iac.grn.infosweb.context.mc.usr.UsrStateHolder;
import iac.grn.infosweb.session.audit.export.ActionsMap;
import iac.grn.infosweb.session.audit.export.ResourcesMap;
import iac.grn.serviceitems.BaseTableItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.ELException;
import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

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
//import org.ajax4jsf.model.DataComponentState;









import ru.spb.iac.cud.reports.ReportsManagerLocal;

@Name("reportsManager")
public class ReportsManager {
	
	@Logger private Log log;
	
	@In 
	EntityManager entityManager;
	
	private List<ReportsBssT> reportsList;
	private int runReportFlag; 
	
	private Date reportDate1; 
	
	private Date reportDate2; 
	
	public void create_report(){
		  
		log.info("reportsManager:create_report:01:"+this.reportDate1);
		log.info("reportsManager:create_report:02:"+this.reportDate2);
		
		
		try{
			String reportId = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("reportId");
			
			if(reportId==null){
				return;
			}
			
			ReportsBssT rep = entityManager.find(ReportsBssT.class, new Long(reportId));
			
			
			
			Context ctx = new InitialContext();
			
			ReportsManagerLocal aml = (ReportsManagerLocal) ctx.lookup("java:global/ReportsServices/ReportsManager!ru.spb.iac.cud.reports.ReportsManagerLocal");
			
			BaseParamItem paramMap = new BaseParamItem();
			paramMap.put("reportCode", rep.getReportCode());
			paramMap.put("reportDate1", reportDate1);
			paramMap.put("reportDate2", reportDate2);
			paramMap.put("reportName", rep.getReportName());
			
			this.runReportFlag = aml.create_report(paramMap);
			
			log.info("reportsManager:create_report:02");
			
		}catch(Exception e){
			log.error("reportsManager:create_report:error:"+e);
			
		}
			 	
	}

	public void download_report(String reportType){
		  
		
		try{
			String reportCode = FacesContext.getCurrentInstance().getExternalContext()
			             .getRequestParameterMap()
			             .get("reportCode");
			
			OutputStream os = null;
			 
			Context ctx = new InitialContext();
			
			ReportsManagerLocal aml = (ReportsManagerLocal) ctx.lookup("java:global/ReportsServices/ReportsManager!ru.spb.iac.cud.reports.ReportsManagerLocal");
			
			ReportDownloadItem report = aml.download_report(reportCode, reportType);
			
			byte[] content = report.getContent();
			int flagExec = 	report.getFlagExec();
			
			HttpServletResponse response = (HttpServletResponse)
					  FacesContext.getCurrentInstance().getExternalContext().getResponse();
			
			os = response.getOutputStream();
			
			//1 - �������
			//0 - ��� �������
			//-1 - ������������ ��� �� ���������
			//-2 - ����������� ������
			
			if(flagExec==1){
			
			  response.setHeader("Content-disposition", "attachment; filename="+reportCode+".xls");
		      response.setContentType("application/xls");
			 
		      os.write(content);
	          
	      		
			}else if(flagExec==0){
				 response.setContentType("text/html; charset=utf-8");
				 
				 os.write("������������� ������ �� ������!".getBytes("utf-8"));
				 os.write("<br/><br/><a href=\"javascript:window.close();\" style=\"color:black;font-size:18px!important;\"> ������� </a>".getBytes("utf-8"));;
				         
		   	}else if(flagExec==-1){
				 response.setContentType("text/html; charset=utf-8");
				 
				 os.write("������������ ������ ��� �� ���������!".getBytes("utf-8"));
				 os.write("<br/><br/><a href=\"javascript:window.close();\" style=\"color:black;font-size:18px!important;\"> ������� </a>".getBytes("utf-8"));;
				         
		   	}else if(flagExec==-1){
				 response.setContentType("text/html; charset=utf-8");
				 
				 os.write("��� ���������� ��������� ������ !".getBytes("utf-8"));
				 os.write("<br/><br/><a href=\"javascript:window.close();\" style=\"color:black;font-size:18px!important;\"> ������� </a>".getBytes("utf-8"));;
				         
		   	}
			
			  os.flush();
	          os.close();
			  FacesContext.getCurrentInstance().responseComplete();
		    
			log.info("reportsManager:download_report:03");
		}catch(Exception e){
			log.error("reportsManager:download_report:error:"+e);
			
		}
			 	
	}
	/*
	public void downDictionary(){
	    try {
	    	System.out.println("===downDictionary-01==");
	        FacesContext context = FacesContext.getCurrentInstance();
		    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
			  
		  //  response.setHeader("Content-disposition", "attachment; filename=" +"dictionary_of_terms_of_intellectual_property.pdf");
		    response.setContentType("application/pdf"); 
		    byte[] buffer = new byte[2048];
		   	OutputStream os = response.getOutputStream();
            
		   	FileInputStream fis=null;
		    fis=new FileInputStream("/devel/1.pdf");
		   	int readBytes;
    		    while ((readBytes = fis.read(buffer)) != -1) {
    		     os.write(buffer);
	        } 
       	
    		os.flush();
			os.close();
			fis.close();
			context.responseComplete();
			System.out.println("===downDictionary-02==");
			
		} catch (Exception e) {
			System.out.println("===downDictionary-ERROR="+e);
		}
	}*/
	
	public List<ReportsBssT> getReportsList() {
		if(this.reportsList==null){
			this.reportsList = entityManager
					.createQuery("SELECT r FROM ReportsBssT r order by orderNum ")
					.getResultList();
			
		}
		
		return reportsList;
	}

	public void setReportsList(List<ReportsBssT> reportsList) {
		this.reportsList = reportsList;
	}

	public int getRunReportFlag() {
		return runReportFlag;
	}

	public void setRunReportFlag(int runReportFlag) {
		this.runReportFlag = runReportFlag;
	}

	public Date getReportDate1() {
		return reportDate1;
	}

	public void setReportDate1(Date reportDate1) {
		this.reportDate1 = reportDate1;
	}

	public Date getReportDate2() {
		return reportDate2;
	}

	public void setReportDate2(Date reportDate2) {
		this.reportDate2 = reportDate2;
	}

	
			
}
		
