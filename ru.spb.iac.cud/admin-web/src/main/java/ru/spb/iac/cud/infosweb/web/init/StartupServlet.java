package ru.spb.iac.cud.infosweb.web.init;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import mypackage.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Servlet implementation class StartupServlet
 */
public class StartupServlet extends HttpServlet {

	final static Logger logger = LoggerFactory.getLogger(StartupServlet.class);

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StartupServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		
		
		try {
			ServletContext context = config.getServletContext();
		
			String signRequired = context.getInitParameter("SIGN_REQUIRED"); 
			
			String samlRequestLogin = context.getInitParameter("SAML_REQUEST_LOGIN"); 
			
			String samlRequestLogout = context.getInitParameter("SAML_REQUEST_LOGOUT"); 
			
            String samlAssertion = context.getInitParameter("SAML_ASSERTION"); 
			
			String storePath = context.getInitParameter("STORE_PATH"); 
			
			String archiveAuditFunc = context.getInitParameter("ARHIVE_AUDIT_FUNC"); 
			
			String archiveAuditSys = context.getInitParameter("ARHIVE_AUDIT_SYS"); 
			
			String archiveToken = context.getInitParameter("ARHIVE_TOKEN");
			
			String uccert = context.getInitParameter("UCCERT");
			
			String auditService = context.getInitParameter("AUDIT_SERVICE");
			
			String stsOboService = context.getInitParameter("STS_OBO_SERVICE");
			
			String stsService = context.getInitParameter("STS_SERVICE");
			
			String ucCertReestr = context.getInitParameter("UC_CERT_REESTR");
			
			String classifService = context.getInitParameter("CLASSIF_SERVICE");
			
			
			Configuration.setSignRequired(
						!"FALSE".equals(signRequired)&&!"false".equals(signRequired));
		
		
			Configuration.setSamlRequestLogin(samlRequestLogin);
			Configuration.setSamlRequestLogout(samlRequestLogout);
			Configuration.setSamlAssertion(samlAssertion);
			Configuration.setStorePath(storePath);
			Configuration.setArchiveAuditFunc(archiveAuditFunc);
			Configuration.setArchiveAuditSys(archiveAuditSys);;
			Configuration.setArchiveToken(archiveToken);
			Configuration.setUccert(uccert);
			Configuration.setAuditService(auditService);
			Configuration.setStsOboService(stsOboService);
			Configuration.setStsService(stsService);
			Configuration.setUcCertReestr(ucCertReestr);
			Configuration.setClassifService(classifService);
			
		} catch (Exception e) {
			logger.error("init:error:" + e);
		}
	}

}
