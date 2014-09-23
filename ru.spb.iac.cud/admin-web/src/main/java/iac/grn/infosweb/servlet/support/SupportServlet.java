package iac.grn.infosweb.servlet.support;

import iac.grn.infosweb.session.support.SupportManager;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.servlet.ContextualHttpServletRequest;

public class SupportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
     
	private String help_fio=null, help_post=null, help_mail=null, help_text=null, help_tel=null;
	
    public SupportServlet() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
		System.out.println("SupportServlet:init:01");
	}

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("SupportServlet:service:01");  
		
		final HttpServletRequest f_request = request;
		final HttpServletResponse f_response = response;
		
		try{
	    response.setContentType("text/html;charset=UTF-8");
	     PrintWriter out = response.getWriter();
	     out.println("<html>");
	     out.println("<head>");
	     out.println("<title>SupportServlet</title>");
	     out.println("</head>");
	     out.println("<body>");
	     out.println("<h1>SupportServlet</h1>");
	     out.println("</body>");
	     out.println("<script>");
	     out.println("window.close();");
	     out.println("</script>");
	     out.println("</html>");
	    
	     help_fio = request.getParameter("help_fio");
	     help_post = request.getParameter("help_post");
	     help_mail = request.getParameter("help_mail");
	     help_text = request.getParameter("help_text");
	     help_tel = request.getParameter("help_tel");
	     
	     System.out.println("SupportServlet:service:help_fio:"+help_fio);
	     System.out.println("SupportServlet:service:help_post:"+help_post);
	     System.out.println("SupportServlet:service:help_mail:"+help_mail);
	     System.out.println("SupportServlet:service:help_text:"+help_text);
	     System.out.println("SupportServlet:service:help_texl:"+help_tel);
	     
	     new ContextualHttpServletRequest(request) {
            @Override
            public void process() throws Exception {
                doWork(f_request, f_response);
            }
         }.run();
		
         
		}catch(Exception e){
		  System.out.println("SupportServlet:service:ERROR:"+e);
		}
	    System.out.println("SupportServlet:service:02");
	}  
	
	private void doWork(HttpServletRequest request, HttpServletResponse response) {
		SupportManager sm = (SupportManager)
				Component.getInstance("supportManager", ScopeType.EVENT);
		sm.sendMail(help_fio, help_post, help_mail, help_text, help_tel);
        
    }
/*	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
*/
}
