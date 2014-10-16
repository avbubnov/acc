package iac.grn.infosweb.servlet.support;

import iac.grn.infosweb.context.mc.arm.ArmManager;
import iac.grn.infosweb.context.mc.armgroup.ArmGroupManager;
import iac.grn.infosweb.context.mc.armsub.ArmSubManager;
import iac.grn.infosweb.context.mc.usr.UsrManager;
import iac.grn.infosweb.session.support.SupportManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.servlet.ContextualHttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
     
	public FileUploadServlet() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
		System.out.println("FileUploadServlet:init:01");
	}

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("FileUploadServlet:service:01");  
		
		//final HttpServletRequest f_request = request;
		//final HttpServletResponse f_response = response;
		
		try{
	    response.setContentType("text/html;charset=UTF-8");
	    
	    
	     boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
			if (!isMultipartContent) {
				//out.println("You are not trying to upload<br/>");
				return;
			}
	     
          	String id_sys_param = request.getParameter("sessionId");
			
          	String type_sys_param = request.getParameter("typeSystem");
          	
			System.out.println("FileUploadServlet:service:02:"+id_sys_param);  
			System.out.println("FileUploadServlet:service:02_1:"+type_sys_param);  
			
			if(id_sys_param==null||id_sys_param.isEmpty()||
			   type_sys_param==null||type_sys_param.isEmpty()){
				System.out.println("FileUploadServlet:service:02_2:return"); 
				return;
			}
			
			Long id_sys = new Long(id_sys_param);
			
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			List<FileItem> fields = upload.parseRequest(request);
			//out.println("Number of fields: " + fields.size() + "<br/><br/>");
			Iterator<FileItem> it = fields.iterator();
			if (!it.hasNext()) {
				//out.println("No fields found");
				System.out.println("FileUploadServlet:service:03");
				return;
			}
			//out.println("<table border=\"1\">");
			while (it.hasNext()) {
				System.out.println("FileUploadServlet:service:04"); 
				
				//out.println("<tr>");
				FileItem fileItem = it.next();
				boolean isFormField = fileItem.isFormField();
				if (isFormField) {
				/*	out.println("<td>regular form field</td><td>FIELD NAME: " + fileItem.getFieldName() + 
							"<br/>STRING: " + fileItem.getString()
							);
					out.println("</td>");*/
				} else {
					System.out.println("FileUploadServlet:service:05"); 
					
					//saveToFS(fileItem);
					saveToBD(fileItem, request, id_sys, type_sys_param, response);
					
				/*	out.println("<td>file form field</td><td>FIELD NAME: " + fileItem.getFieldName() +
							"<br/>STRING: " + fileItem.getString() +
							"<br/>NAME: " + fileItem.getName() +
							"<br/>CONTENT TYPE: " + fileItem.getContentType() +
							"<br/>SIZE (BYTES): " + fileItem.getSize() +
							"<br/>TO STRING: " + fileItem.toString()
							);*/
					//out.println("</td>");
				}
				//out.println("</tr>");
			}
			//out.println("</table>");
			
	    /* InputStreamReader reader = new InputStreamReader(request.getInputStream());
	     int c;
	     while ((c=reader.read())>=0) {
	    	 System.out.println("FileUploadServlet:service:02");  
	    	 out.print((char)c);
	     }*/
	  /*  
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>FileUploadServlet</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>FileUploadServlet</h1>");
		out.println("<div>");
	    out.println("</div>");
	    out.println("</body>");
	    out.println("<script>");
	    out.println("alert(12345);");
	    out.println("</script>");
	    out.println("</html>");
		*/
         
		}catch(Exception e){
		  System.out.println("FileUploadServlet:service:ERROR:"+e);
		}
	    System.out.println("FileUploadServlet:service:06");
	}  
	/*
	private void saveToFS(FileItem item) throws Exception {
		
		System.out.println("FileUploadServlet:processUploadedFile:01:"+item.getName());
		
		Random random = new Random();
		
		try{
         File uploadetFile = new File("/Development/test/upload_cert.crt");
         item.write(uploadetFile);

		}catch(Exception e){
			  System.out.println("FileUploadServlet:processUploadedFile:ERROR:"+e);
		}
     }*/
	private void saveToBD(FileItem item, final HttpServletRequest f_request, final Long id_sys, final String type_sys,
			final HttpServletResponse response) throws Exception {
		
		System.out.println("FileUploadServlet:saveToBD:01:"+item.getName());
		
		Random random = new Random();
		
		final byte[] file_byte = item.get();
		try{
			 new ContextualHttpServletRequest(f_request) {
		            @Override
		            public void process() throws Exception {
		                doWork(file_byte, id_sys, type_sys, response);
		            }
		         }.run();

		}catch(Exception e){
			  System.out.println("FileUploadServlet:saveToBD:ERROR:"+e);
		}
    }
	
	private void doWork(byte[] file_byte, Long id_sys, String type_sys, HttpServletResponse response) {
		
		System.out.println("FileUploadServlet:doWork:01:"+type_sys);
		
		if(type_sys.equals("system")){
		   ArmManager sm = (ArmManager) Component.getInstance("armManager", ScopeType.EVENT);
	       sm.saveArmCertificate(file_byte, id_sys);
		}else if(type_sys.equals("subsystem")){
		   ArmSubManager sm = (ArmSubManager) Component.getInstance("armSubManager", ScopeType.EVENT);
		   sm.saveArmSubCertificate(file_byte, id_sys);
		}else if(type_sys.equals("groupsystem")){
			ArmGroupManager sm = (ArmGroupManager) Component.getInstance("armGroupManager", ScopeType.EVENT);
			sm.saveArmGroupCertificate(file_byte, id_sys);
		}else if(type_sys.equals("user")){
			UsrManager um = (UsrManager) Component.getInstance("usrManager", ScopeType.EVENT);
			boolean result = um.saveUserCertificate(file_byte, id_sys);
			
			if(!result){
			//����� ���������� ��� ������������!	
				try{
					PrintWriter out = response.getWriter();
					out.println("<html>");
					out.println("<head>");
					out.println("<title>FileUploadServlet</title>");
					out.println("</head>");
					out.println("<body>");
					out.println("<h1>FileUploadServlet</h1>");
					out.println("<div>");
				    out.println("</div>");
				    out.println("</body>");
				    out.println("<script>");
				    out.println("window.top.iframeCertExistFlag=1;");
				   // out.println("alert(12345);alert(window.top.iframeCertExistFlag)");
				    out.println("</script>");
				    out.println("</html>");
					}catch(Exception e){
						 System.out.println("FileUploadServlet:doWork:ERROR:"+e);
					}
			}
		}
			
    }
/*	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
*/
}
