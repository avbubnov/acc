package ru.spb.iac.cud.uarm.web.context.forgot;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ru.spb.iac.cud.exceptions.web.BaseError;
import ru.spb.iac.cud.items.CodesErrors;
import ru.spb.iac.cud.uarm.ejb.context.forgot.UserForgotEJB;
import ru.spb.iac.cud.uarm.ejb.context.reg.UserRegEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import ru.spb.iac.cud.uarm.web.context.user.UserSessionBean;
import test.ejb.HomeBean;
 
@ManagedBean(name="userForgotBean")
@RequestScoped
public class UserForgotBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	   
    @EJB(beanName = "CUDUserConsole-ejb.jar#UserForgotEJB")
	private UserForgotEJB userForgotEJB;
	
    private String userLogin;
 
    private String userEmail;
    
    private static final String userEmailReg = "userEmailReg";
    private static final String userEmailsList = "userEmailsList";
    
    private String newPass;
    private String reNewPass;
    
    public void action() {
        
    	try{
        
    	System.out.println("UserForgotBean:action:01");
        
    	 String userNewPassword = newPass;
         
         String userReNewPassword = reNewPass;
		 
         System.out.println("UserForgotBean:changePassword:03:"+userNewPassword);
         System.out.println("UserForgotBean:changePassword:04:"+userReNewPassword);
         
         
         if(userNewPassword==null||userNewPassword.isEmpty()||
       	  userReNewPassword==null||userReNewPassword.isEmpty()){
       	  
       	   System.out.println("UserForgotBean:changePassword:05");
       	  
       	  FacesContext.getCurrentInstance().addMessage(null, 
	        			new FacesMessage("����������� ��� ����!"));
       	   return;
         }
         
         
         Boolean latin = Pattern.matches("[^�-��-�]*", userNewPassword);
       
         if(!latin){
       	  
       	  System.out.println("UserForgotBean:changePassword:06");
       	  
       	  FacesContext.getCurrentInstance().addMessage(null, 
	        			new FacesMessage("� ������ �� ��������� ���������!"));
       	   return;
         }
         
         if(!userNewPassword.equals(userReNewPassword)){
       	  
       	  System.out.println("UserForgotBean:changePassword:07");
       	  
		      	FacesContext.getCurrentInstance().addMessage(null, 
	        			new FacesMessage("������ �� ���������!"));
	        	//FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
		 }else{
         
			HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
			String userLogin = (String) hs.getAttribute(CUDUserConsoleConstants.userLoginForgot);
				
			System.out.println("UserForgotBean:changePassword:08:"+userLogin);
			
			userForgotEJB.changePassword(userLogin, userNewPassword);
			
			FacesContext
			.getCurrentInstance()
			.getExternalContext()
			.redirect(
					((HttpServletRequest) FacesContext
							.getCurrentInstance().getExternalContext()
							.getRequest()).getContextPath()
							+ "/welcome.xhtml");
         }
		 
         System.out.println("UserForgotBean:changePassword:09");
       /*  
        HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
		String email = (String) hs.getAttribute(CUDUserConsoleConstants.userEmailReg);
	
        
        JournAppUserBssT t1 = new JournAppUserBssT();
        
        t1.setSurnameUser(userFam);
        t1.setNameUser(userName);
 	    t1.setPatronymicUser(userOtch);
 	   // t1.setEmailUser(userEmail);
 	    t1.setEmailUser(email);
 	    t1.setPhoneUser(userPhone);
 	    t1.setNameOrg(orgName);
 	    t1.setNameDepartament(depName);
 	    t1.setPositionUser(userPos);
        
        userRegEJB.save(t1);
      
        
        //return "home?faces-redirect=true";
        FacesContext.getCurrentInstance().getExternalContext().redirect(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest())
        		.getContextPath()+"/context/registr/reg_user_step2_message.xhtml");
       */
    	}catch(Exception e){
    		System.out.println("UserForgotBean:action:error:"+e);
    	}
   }

   public void step1() {
        
	   //����� � 2-� �������:
	   //1) �� ������ ���� (this.userEmail = null)
	   //2) ������������ ������� ���� email (this.userEmail = ��������� email)
     try{
        
    	System.out.println("UserForgotBean:step1:01");
        
    	HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
 		
    	//this.userLogin!=null ��� ����1
    	//this.userLogin==null ��� ��������� ������������� email
    	String userLoginFact=(this.userLogin!=null?this.userLogin:(String)hs.getAttribute(CUDUserConsoleConstants.userLoginForgot));
    	
    	HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	String context_url=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
			
    	List<String> emails = userForgotEJB.step1(userLoginFact, this.userEmail, context_url);
        //��� ��������� email, emails ����� = ����� ������������ email  
    	
    	if(emails==null||emails.isEmpty()){
    		//� ������������ ��� ������������ email
    		
    		System.out.println("UserForgotBean:step1:02");
    		
    		  FacesContext.getCurrentInstance().getExternalContext().redirect(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest())
    	        		.getContextPath()+"/context/forgot/pass_step1_message_not_email.xhtml");
    	   
    	}else if(emails.size()>1){
    		//� ������������ �� ���� email
    		//��� ������������ ������� email
    		
    		System.out.println("UserForgotBean:step1:03:"+emails.size());
    		
    		//FacesContext.getCurrentInstance().getExternalContext().getFlash()
            //.put(userEmailsList, emails);
    		
    		 hs.setAttribute(userEmailsList, emails);
    		 
    		 hs.setAttribute(CUDUserConsoleConstants.userLoginForgot, this.userLogin);
    			
    		  FacesContext.getCurrentInstance().getExternalContext().redirect(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest())
  	        		.getContextPath()+"/context/forgot/pass_step1_message_more_email.xhtml");
  	
    		  
    	}else {
    		//���������� ������� - ���� email
    		
    		System.out.println("UserForgotBean:step1:04");
    		
    	//FacesContext.getCurrentInstance().getExternalContext().getFlash()
       //      .put(userEmailReg, emails.get(0));
        
        //return "home?faces-redirect=true";
        FacesContext.getCurrentInstance().getExternalContext().redirect(((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest())
        		.getContextPath()+"/context/forgot/pass_step1_message.xhtml");
   
    	}
        
      }catch(BaseError be){
 		System.out.println("UserForgotBean:step1:berror:"+be);
 		
 		if(CodesErrors.NOT_FOUND.equals(be.getCodeError())){
 			//������������ �� ��������
 			 try{
 				 
 			   FacesContext.getCurrentInstance().addMessage(null, 
 	        			new FacesMessage("������������ �� ��������!"));
 	      	
 			 }catch(Exception e){
 	    		System.out.println("UserForgotBean:step1:error2:"+e);
 	          }
 			
 		}
 		
 	  }catch(Exception e){
    		System.out.println("UserForgotBean:step1:error:"+e);
      }
     }



	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public String getReNewPass() {
		return reNewPass;
	}

	public void setReNewPass(String reNewPass) {
		this.reNewPass = reNewPass;
	}



}
