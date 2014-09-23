package ru.spb.iac.cud.sp.valve;

import org.apache.catalina.LifecycleException;
import org.picketlink.identity.federation.bindings.tomcat.sp.AbstractSPFormAuthenticator;

public class CUDServiceProviderAuthenticator extends CUDAbstractSPFormAuthenticator {
  
	@Override
   public void start() throws LifecycleException {
       super.start();
       startPicketLink(); 
   }
   
   @Override
   protected String getContextPath() { 
       return getContext().getServletContext().getContextPath();
   }

}
