package ru.spb.iac.cud.uarm.ejb.context.forgot;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ru.spb.iac.cud.exceptions.web.BaseError;
import ru.spb.iac.cud.items.CodesErrors;
import ru.spb.iac.cud.uarm.ejb.context.user.UserManagerEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userForgotEJB")
@LocalBean
public class UserForgotEJB {

   
	@Resource(mappedName="java:jboss/mail/Default")
	private Session mailSession;
	
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
	
	@EJB(beanName = "CUDUserConsole-ejb.jar#UserManagerEJB")
	private UserManagerEJB userManagerEJB;
	
    public UserForgotEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppUserBssT user) {

       System.out.println("UserForgotEJB:save:01");
       System.out.println("UserForgotEJB:save:02:"+user.getNameUser());
       try{
    	  /*List<JournAppUserBssT>  app_user_list = entityManager
    			  .createQuery("select t1 from JournAppUserBssT t1 ")
    			  .getResultList();
    	  
    	  System.out.println("UserForgotEJB:save:03:"+app_user_list.size());
    	  */
    	   
    	   user.setStatus(0L);
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   System.out.println("UserForgotEJB:save:error:"+e);
       }
     }
    
    public List<String> step1(String login, String email_up_to_date, String context_url) throws Exception{

    	//email_up_to_date - ���������� email
    	//����� � ������������ ��������� email, � �� �������� ���� �� ���
    	
        System.out.println("UserForgotEJB:step1:01:"+login);
        System.out.println("UserForgotEJB:step1:01_2:"+context_url);
        
        List<String> result = null;
        		
        try{
        	
        if(email_up_to_date==null){
        	//����� - ������ ���
        	
        	AcUsersKnlT user = userManagerEJB.getUserItemFromLogin(login);
        	
        	if(user==null){
        	 throw new BaseError("������������ �� ������", CodesErrors.NOT_FOUND);	
        	}
        	
        		 
        	if(user.getUserItem().getEmail()==null&&
        	   user.getUserItem().getEmailSecond()==null){
        		
        		//!!!
        		return result;
        		
        	}else if(user.getUserItem().getEmail()!=null&&
             	     user.getUserItem().getEmailSecond()!=null){
        		
        		result = new ArrayList<String>();
        		
        		if(user.getUserItem().getEmail().equals(
        				user.getUserItem().getEmailSecond())){
        			
        			result.add(user.getUserItem().getEmail());
            		
        		}else{
        			result.add(user.getUserItem().getEmail());
        			result.add(user.getUserItem().getEmailSecond());
        			
        			//!!!
        			return result;
        		}
        		
        	}else {
        		result = new ArrayList<String>();
        		result.add((user.getUserItem().getEmail()!=null?
        				    user.getUserItem().getEmail():
        					user.getUserItem().getEmailSecond()));
        	}
        	
         }else{
        	//����� - ������������ ������� ���� email
        	
        	result = new ArrayList<String>();
        	result.add(email_up_to_date);
        }
        
        	String email = result.get(0);
        	
        	MimeMessage m = new MimeMessage(mailSession);
        	Address from = new InternetAddress("do-not-reply@iac.spb.ru");
        	Address[] to = new InternetAddress[] {
        			new InternetAddress(email) 
        			};
        	//Address[] to = new InternetAddress[] {new InternetAddress("bubnov@iac.spb.ru") };
        	
        	
        	m.setFrom(from);
        	m.setRecipients(Message.RecipientType.TO, to);
        	m.setSubject("�������� ������ ��. �����", "utf-8");
        	m.setSentDate(new java.util.Date());
        	
        	String validationKey = (new BigInteger((email+login).getBytes("utf-8"))).toString(16);
        	
        	 //String link = "http://localhost:8080/uarm/userForgotServlet?email=" +
        	 String link = context_url+"/userForgotServlet?email=" +
           	 URLEncoder.encode(email, "UTF-8")+"&validationKey=" +
           	 URLEncoder.encode(validationKey, "UTF-8")+"&login="+
           	 URLEncoder.encode(login, "UTF-8");
        	
        	System.out.println("UserForgotEJB:step1:02:"+link);
        	 
        	String content = "������ ����!<br/>"+
        	 "�� �������� ������ �� ����� ������ ������ � ��� ����.<br/>" +
        	 "���� �� ����������� ����� ������ ������, ����������, �������� ����������� ����. "+ 
        	 "���� ��� ��� �� ���������, ������ �������������� ��� ���������. <br/>"+
        	 "����� �������� ������ ��������� �� ��������� ������: <br/>" +
        	 "<a href=\""+link+"\">" + link +"</a> <br/><br/>" +
        	 "---<br/>" +
        	 "� ���������,<br/> " +
        	 "���";
        	
        	m.setContent(content, "text/html; charset=utf-8");
        	//m.setContent(content, "text/plain");
        	
        	Transport.send(m);
        	
        	System.out.println("UserForgotEJB:step1:03");
        
        }catch(BaseError be){
        	System.out.println("UserForgotEJB:step1:berror:"+be);
        	throw be;
        	
        }catch(Exception e){
     	   System.out.println("UserForgotEJB:step1:error:"+e);
        }
        
        return result;
     }
    
    public void changePassword(String loginUser, String newPassword) throws Exception{
    	
   	 System.out.println("UserForgotEJB:changePassword:01");
   	 
   	 try{
   		 
   		     entityManager.createNativeQuery(
         		   "update AC_USERS_KNL_T t1 " +
         		   "set t1.PASSWORD_ = ? "+
         		   "where t1.LOGIN = ? ")
         		   .setParameter(1, newPassword)
         		   .setParameter(2, loginUser)
                 .executeUpdate();
           
        }catch(Exception e){
 		  System.out.println("UserForgotEJB:changePassword:error:"+e);
 		 // e.printStackTrace(System.out);
 		  throw e;
 	   }
   	 
    }
}
