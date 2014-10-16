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
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessGroupsBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;

/**
 * Session Bean implementation class HomeBean
 */
@Stateless(mappedName = "userAccessEJB")
@LocalBean
public class UserAccessEJB {

	final static Logger LOGGER = LoggerFactory.getLogger(UserAccessEJB.class);
	
   
	@PersistenceContext(unitName = "CUDUserConsolePU")
    private EntityManager entityManager;
	
    public UserAccessEJB() {
        // TODO Auto-generated constructor stub
    }

    public void save(JournAppAccessBssT user) {

       LOGGER.debug("userAccessEJB:save:01");
       LOGGER.debug("userAccessEJB:save:02:"+user.getCodeSystem());
       try{
    	 
    	   
    	   user.setCreated(new Date());
    	   entityManager.persist(user);
    	   
    	   
       }catch(Exception e){
    	   LOGGER.error("userAccessEJB:save:error:"+e);
       }
     }
    
    public void saveGroup(JournAppAccessGroupsBssT user) {

        LOGGER.debug("userAccessEJB:saveGroup:01");
      
        try{
     	 
     	   
     	   user.setCreated(new Date());
     	   entityManager.persist(user);
     	   
     	   
        }catch(Exception e){
     	   LOGGER.error("userAccessEJB:saveGroup:error:"+e);
        }
      }
}
