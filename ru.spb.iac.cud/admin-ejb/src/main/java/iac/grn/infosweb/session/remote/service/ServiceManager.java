package iac.grn.infosweb.session.remote.service;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("serviceManager")
public class ServiceManager {

	@In 
	EntityManager entityManager;
	
	Context ctx=null;
	iac.grn.ramodule.session.audit.ufms.frontage.InvokeFrontageRemote ifUfms;
	iac.grn.ramodule.session.audit.zags.frontage.InvokeFrontageRemote ifZags;
	
	public Object getServiceStub(String idRai, String serviceName) throws Exception{
		try{
			return lookup(getUrl(idRai),serviceName);
	   }catch(Exception e){
			System.out.println("serviceManager:getServiceStub:"+e);
	    	throw e;
		}
	}
	public iac.grn.ramodule.session.audit.ufms.frontage.InvokeFrontageRemote getFrontageServiceUfms(String idRai) throws Exception{
		try{
			if(this.ifUfms==null){
			  lookupFacadeUfms(getUrl(idRai));
			}
			return this.ifUfms;
	   }catch(Exception e){
			System.out.println("serviceManager:getFrontageStubUfms:"+e);
	    	throw e;
		}
	}
	public iac.grn.ramodule.session.audit.zags.frontage.InvokeFrontageRemote getFrontageServiceZags(String idRai) throws Exception{
		try{
			if(this.ifZags==null){
			  lookupFacadeZags(getUrl(idRai));
			}
			return this.ifZags;
	   }catch(Exception e){
			System.out.println("serviceManager:getFrontageStubZags:"+e);
	    	throw e;
		}
	}
	public String getUrl (String idRai) throws Exception{
		Object[] oa=null;
		System.out.println("serviceManager:getUrl:01");
		try{
		 oa= (Object[])	entityManager
				.createNativeQuery("select to_char(AH.DNS_NAME) , to_char(APL.PORT) "+
                       "from AC_PROT_LIST apl, "+
                       "AC_HOSTS ah "+
                       "where APL.ID_HOST=AH.ID_HOST "+
                       "and AH.ID_RAION=? "+
                       "and APL.ID_PROTOCOL=3 ")
                       .setParameter(1, new Long(idRai))
                       .getSingleResult();
		if(oa!=null){
			System.out.println("serviceManager:getUrl:02:"+(oa[0]+":"+oa[1]));
			return oa[0]+":"+oa[1];
		}else{
			System.out.println("serviceManager:getUrl:03");
			throw new NoResultException();
		}
		 
		}catch(Exception e){
			System.out.println("serviceManager:getUrl:Error:"+e);
			throw e;
		}
	} 
	public Object lookup(String url, String serviceName)throws Exception{
		try{
			if(ctx==null){
			  Properties env = new Properties();
			  env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			  env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming");
			  env.setProperty(Context.PROVIDER_URL, url);
	          System.out.println("serviceManager:lookup:01");		
		      ctx = new InitialContext(env);
		     }
			 System.out.println("serviceManager:lookup:02");
		    return ctx.lookup(serviceName);
	    }catch(Exception e){
			System.out.println("serviceManager:lookup:error:"+e);
	//		FacesMessages.instance().add("Ошибка доступа к серверу "+url+"!");
			throw e;
		}
	  }
	 public void lookupFacadeUfms(String url)throws Exception{
			try{
				if(this.ctx==null){
				  setInitialContext(url);
				 }
				 System.out.println("serviceManager:lookupFacadeUfms:02");
				 this.ifUfms=(iac.grn.ramodule.session.audit.ufms.frontage.InvokeFrontageRemote)ctx.lookup("ufms.frontage.InvokeFrontage.remote");
			 }catch(Exception e){
				System.out.println("serviceManager:lookupFacadeUfms:error:"+e);
		//		FacesMessages.instance().add("Ошибка доступа к серверу "+url+"!");
				throw e;
			}
	}
	public void lookupFacadeZags(String url) throws Exception{
		try{
			if(this.ctx==null){
			  setInitialContext(url);
		     }
			 System.out.println("serviceManager:lookupFacadeZags:02");
			 this.ifZags=(iac.grn.ramodule.session.audit.zags.frontage.InvokeFrontageRemote)ctx.lookup("zags.frontage.InvokeFrontage.remote");
		  }catch(Exception e){
			System.out.println("serviceManager:lookupFacadeZags:error:"+e);
	//		FacesMessages.instance().add("Ошибка доступа к серверу "+url+"!");
			throw e;
		}
  }
	private void setInitialContext(String url) throws Exception{
		try{	
		  Properties env = new Properties();
		  env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		  env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming");
		  env.setProperty(Context.PROVIDER_URL, url);
		  //  env.setProperty(Context.PROVIDER_URL, "localhost:1199");
          System.out.println("serviceManager:setInitialContext:01");		
          this.ctx = new InitialContext(env);
		}catch(Exception e){
			System.out.println("serviceManager:setInitialContext:error:"+e);
	//		FacesMessages.instance().add("Ошибка доступа к серверу "+url+"!");
			throw e;
		}   
	}
		
}
