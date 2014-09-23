package ru.spb.iac.cud.uarm.web.context.auth;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ru.spb.iac.cud.uarm.ejb.context.reg.UserRegEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import ru.spb.iac.cud.uarm.web.context.user.UserSessionBean;
import test.ejb.HomeBean;
 
@WebServlet(value="/authServlet")
public class AuthServlet extends HttpServlet {
 
   private static final long serialVersionUID = 1L;
 
   public AuthServlet() {
        super();
   }

   public void init(ServletConfig config) throws ServletException {
   }

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		System.out.println("AuthServlet:service:01");
		boolean success = false;
		String email = null;
				
		try{
		
					
	    }catch(Exception e){
	    	System.out.println("AuthServlet:service:error:"+e);
	    }
		
		if(success){
			//!!!
			HttpSession hs = (HttpSession) request.getSession(true); 
			System.out.println("AuthServlet:service:03:"+hs.getId());
			hs.setAttribute(CUDUserConsoleConstants.userEmailReg, email);
		
			response.sendRedirect(request.getContextPath()+"/context/registr/reg_user_step2.xhtml");
		}else{
			response.sendRedirect(request.getContextPath()+"/welcome.xhtml");
		}
	
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	
}
