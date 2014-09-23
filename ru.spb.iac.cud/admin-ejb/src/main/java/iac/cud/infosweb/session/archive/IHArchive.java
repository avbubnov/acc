package iac.cud.infosweb.session.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;


import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;

@Stateless
//@LocalBinding(jndiBinding=ServiceReestr.Archive)
public class IHArchive extends IHArchiveBase implements IHLocal {
  
	@PersistenceContext(unitName="InfoSCUD-web")
    EntityManager em;
	
/*
    public BaseParamItem clean(BaseParamItem paramMap, Date clearDate1, Date clearDate2)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		try{
			 
			String modelType = (String) paramMap.get("modelType");
			 
			if(clearDate1==null || clearDate2==null || modelType==null){
				return null;
			}
			System.out.println("IHJourn:gclear:modelType:"+modelType);
			
			em.createNativeQuery(
					 "delete from journ "+
                     "where JOURN.CREATED>=? "+
                     "and JOURN.CREATED<=? ")
      	  	 .setParameter(1, clearDate1, TemporalType.TIMESTAMP)
      	     .setParameter(2, clearDate2, TemporalType.TIMESTAMP)
             .executeUpdate();
	    }catch(Exception e){
		   System.out.println("IHJourn:gclear:error"+e);
		   throw e;
	    }
		
		return jpi;
   }*/
 }
