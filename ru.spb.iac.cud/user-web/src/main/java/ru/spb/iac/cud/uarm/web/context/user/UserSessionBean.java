package ru.spb.iac.cud.uarm.web.context.user;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ru.spb.iac.cud.uarm.ejb.context.auth.AuthEJB;
import ru.spb.iac.cud.uarm.ejb.context.user.UserManagerEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcIsBssT;
import ru.spb.iac.cud.uarm.ejb.entity.AcRolesBssT;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import test.ejb.HomeBean;
 
@ManagedBean(name="userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {
 
    private static final long serialVersionUID = 1L;
 
    private Map<Long, String> sumRoles;

    private Map<Long, String> sumGroups;
    
    private String userEmailReg;
    
    public Map<Long, String> getSumRoles() {
		return sumRoles;
	}

	public void setSumRoles(Map<Long, String> sumRoles) {
		this.sumRoles = sumRoles;
	}

	public String getUserEmailReg() {
		return userEmailReg;
	}

	public void setUserEmailReg(String userEmailReg) {
		this.userEmailReg = userEmailReg;
	}

	public Map<Long, String> getSumGroups() {
		return sumGroups;
	}

	public void setSumGroups(Map<Long, String> sumGroups) {
		this.sumGroups = sumGroups;
	}

	
   
    
}
