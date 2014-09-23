package ru.spb.iac.cud.services.web.init;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Logger logger = LoggerFactory.getLogger(StartupServlet.class);

	public StartupServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		try {

			/*
			 * <!--context-param> <param-name>JBOSS_JNDI_PORT</param-name>
			 * <param-value>1099</param-value> </context-param--> <!--servlet>
			 * <display-name>StartupServlet</display-name> ...
			 * </servlet-mapping-->
			 */

			/*
			 * try{ ServletContext context = config.getServletContext(); String
			 * jboss_jndi_port = context.getInitParameter("JBOSS_JNDI_PORT");
			 * ContextAccessManager.initContext(jboss_jndi_port);
			 * ContextSyncManager.initContext(jboss_jndi_port);
			 * ContextUtilManager.initContext(jboss_jndi_port); }catch(Exception
			 * e){
			 * System.out.println("CudServices:StartupServlet:init:error:"+e); }
			 */

			ServletContext context = config.getServletContext();
			String jboss_jndi_port = context
					.getInitParameter("JBOSS_JNDI_PORT");
			// ContextIDPUtilManager.initContext(jboss_jndi_port);

            String signRequired = context.getInitParameter("SIGN_REQUIRED"); 
			
			Configuration.setSignRequired(
					!"FALSE".equals(signRequired)&&!"false".equals(signRequired));
	

		} catch (Exception e) {
			logger.error("init:error:" + e);
		}
	}

}
