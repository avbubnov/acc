package ru.spb.iac.cud.uarm.web.context.forgot;

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
 
@WebServlet(value="/userForgotServlet")
public class UserForgotServlet extends HttpServlet {
 
   private static final long serialVersionUID = 1L;
 
   public UserForgotServlet() {
        super();
   }

   public void init(ServletConfig config) throws ServletException {
   }

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		System.out.println("UserForgotServlet:service:01");
		boolean success = false;
		String email = null;
		String login = null;
		
		try{
		
			email = request.getParameter("email");
			login = request.getParameter("login");
			
			String validationKey = request.getParameter("validationKey");
			
			if(email!=null&&!email.trim().isEmpty()&&
			   validationKey!=null&&!validationKey.trim().isEmpty()){
				
				validationKey = URLDecoder.decode(validationKey, "UTF-8");
				email = URLDecoder.decode(email, "UTF-8");
				
				
				String validationKeyTrue = (new BigInteger((email+login).getBytes("utf-8"))).toString(16);
			
				System.out.println("UserForgotServlet:service:02:"+validationKeyTrue);
				
				if(validationKey.equals(validationKeyTrue)){
					success = true;
				}
			}
			
	    }catch(Exception e){
	    	System.out.println("UserForgotServlet:service:error:"+e);
	    }
		
		if(success){
			//!!!
			HttpSession hs = (HttpSession) request.getSession(true); 
			System.out.println("UserForgotServlet:service:03:"+hs.getId());
			hs.setAttribute(CUDUserConsoleConstants.userLoginForgot, login);
		
			response.sendRedirect(request.getContextPath()+"/context/forgot/pass_step2.xhtml");
		}else{
			response.sendRedirect(request.getContextPath()+"/welcome.xhtml");
		}
	
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	
}
