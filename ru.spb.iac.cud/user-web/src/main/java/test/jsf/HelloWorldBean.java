package test.jsf;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import test.ejb.HomeBean;
 
@ManagedBean
@RequestScoped
public class HelloWorldBean implements Serializable {
 

	@EJB(beanName = "CUDUserConsole-ejb.jar#HomeBean")
	private HomeBean homeBean;
	
    private static final long serialVersionUID = 1L;
 
    private String firstName;
 
    private String lastName;
 
    public String getFirstName() {
        return firstName;
    }
 
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        
       // System.out.println("HelloWorldBean:setFirstName:01");
       // 
        String version = FacesContext.class.getPackage().getImplementationVersion();
        
       // System.out.println("HelloWorldBean:setFirstName:02:"+version);
        
       // System.out.println("HelloWorldBean:setFirstName:03:"+homeBean.test());
        
    }
 
    public String getLastName() {
        return lastName;
    }
 
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
