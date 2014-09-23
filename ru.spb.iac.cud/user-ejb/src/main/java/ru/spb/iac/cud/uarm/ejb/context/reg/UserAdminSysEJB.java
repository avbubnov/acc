package ru.spb.iac.cud.uarm.ejb.context.reg;

import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAdminUserSysBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userAdminSysEJB")
@LocalBean
public class UserAdminSysEJB {

	final static Logger logger = LoggerFactory.getLogger(UserAdminSysEJB.class);
	
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
    public UserAdminSysEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppAdminUserSysBssT user) {

       logger.info("userAdminSysEJB:save:01");
       logger.info("userAdminSysEJB:save:02:"+user.getAcIsBssTLong());
       try{
    	  /*List<JournAppUserBssT>  app_user_list = entityManager
    			  .createQuery("select t1 from JournAppUserBssT t1 ")
    			  .getResultList();
    	  
    	  logger.info("UserRegEJB:save:03:"+app_user_list.size());
    	  */
    	   
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   logger.error("userAdminSysEJB:save:error:"+e);
       }
       
       
     }
}
