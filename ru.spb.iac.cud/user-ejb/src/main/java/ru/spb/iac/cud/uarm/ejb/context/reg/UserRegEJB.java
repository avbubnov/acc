package ru.spb.iac.cud.uarm.ejb.context.reg;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userRegEJB")
@LocalBean
public class UserRegEJB {

	final static Logger logger = LoggerFactory.getLogger(UserRegEJB.class);

	
	@Resource(mappedName="java:jboss/mail/Default")
	private Session mailSession;
	
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
    public UserRegEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppUserBssT user) {

       logger.info("UserRegEJB:save:01");
       logger.info("UserRegEJB:save:02:"+user.getNameUser());
       try{
    	  /*List<JournAppUserBssT>  app_user_list = entityManager
    			  .createQuery("select t1 from JournAppUserBssT t1 ")
    			  .getResultList();
    	  
    	  logger.info("UserRegEJB:save:03:"+app_user_list.size());
    	  */
    	   
    	   user.setStatus(0L);
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   logger.error("UserRegEJB:save:error:"+e);
       }
     }
    
    public void step1(String email, String context_url) {

        logger.info("UserRegEJB:step1:01:"+email);
        logger.info("UserRegEJB:step1:01_2:"+context_url);
        
        try{
        	MimeMessage m = new MimeMessage(mailSession);
        	Address from = new InternetAddress("do-not-reply@iac.spb.ru");
        	Address[] to = new InternetAddress[] {
        			new InternetAddress(email) 
        			};
        	//Address[] to = new InternetAddress[] {new InternetAddress("bubnov@iac.spb.ru") };
        	
        	
        	m.setFrom(from);
        	m.setRecipients(Message.RecipientType.TO, to);
        	m.setSubject("Проверка адреса эл. почты", "utf-8");
        	m.setSentDate(new java.util.Date());
        	
        	String validationKey = (new BigInteger(email.getBytes("utf-8"))).toString(16);
        	
        	 //String link = "http://localhost:8080/uarm/userRegServlet?email=" +
        	 String link = context_url+"/userRegServlet?email=" +
           	 URLEncoder.encode(email, "UTF-8")+"&validationKey=" +
           	 URLEncoder.encode(validationKey, "UTF-8");
        	
        	logger.info("UserRegEJB:step1:02:"+link);
        	 
        	String content = "Добрый день!<br/>"+
        	 "Вы интересовались запросом на регистрацию пользователя в ИАЦ ПААА.<br/>" +
        	 "Пожалуйста перейдите по ссылке ниже, чтобы подтвердить " +
        	 "Ваш адрес эл. почты: <br/>" +
        	 "<a href=\""+link+"\">" + link +"</a> <br/><br/>" +
        	 "---<br/>" +
        	 "С уважением,<br/> " +
        	 "ИАЦ";
        	
        	m.setContent(content, "text/html; charset=utf-8");
        	//m.setContent(content, "text/plain");
        	
        	Transport.send(m);
        	
        	logger.info("UserRegEJB:step1:03");
        	
        }catch(Exception e){
     	   logger.error("UserRegEJB:step1:error:"+e);
        }
     }
}
